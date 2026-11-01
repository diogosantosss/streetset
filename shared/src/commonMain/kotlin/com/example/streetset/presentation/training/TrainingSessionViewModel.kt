package com.example.streetset.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.audio.AppAudio
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.data.entities.SessionLog
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.buildBlocksWithSets
import com.example.streetset.presentation.training.components.EmomUiState
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.restAfterSeconds
import com.example.streetset.presentation.training.timer.EMOMTimer
import com.example.streetset.presentation.training.timer.HoldTimer
import com.example.streetset.util.Time
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class TrainingSessionViewModel(
    private val sessionId: Int,
) : ViewModel(), KoinComponent {

    private val blockDao: BlockDao by inject()
    private val plannedSetDao: PlannedSetDao by inject()
    private val exerciseDao: ExerciseDao by inject()
    private val sessionLogDao: SessionLogDao by inject()

    private val stopwatch = Stopwatch()
    private val restTimer = RestTimer()
    private val emomTimer = EMOMTimer(viewModelScope)
    private val holdTimer = HoldTimer(viewModelScope)

    private var timerJob: Job? = null
    private var restJob: Job? = null
    private var insertLogJob: Job? = null
    private var notesUpdateJob: Job? = null

    /** Wall-clock instant when the last exercise was completed. */
    private var finishedAtMillis: Long = 0L

    /** Id of the persisted [SessionLog] for this workout, once inserted. */
    private var sessionLogId: Int? = null

    private val _uiState = MutableStateFlow(
        TrainingSessionUiState(startedAtMillis = Time.nowMillis())
    )

    /**
     * Public UI state.
     *
     * The active protocol is derived from the base state + the running timer so
     * the UI never has to combine flows itself. `stateIn` keeps the flow hot
     * only while there are subscribers, saving cycles when the screen is hidden.
     */
    val uiState: StateFlow<TrainingSessionUiState> =
        combine(_uiState, emomTimer.state, holdTimer.state) { base, emomState, holdState ->
            base.copy(protocol = resolveProtocol(base, emomState, holdState))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrainingSessionUiState(),
        )

    init {
        AppAudio.init()
        startSessionTimer()
        loadQueue()
        emomTimer.onAllRoundsCompleted = ::onEmomCompleted
        holdTimer.onCompleted = ::onHoldCompleted
    }

    // region State helpers

    /** Applies [transform] to the current state and publishes the result. */
    private inline fun updateState(
        transform: (TrainingSessionUiState) -> TrainingSessionUiState
    ) { _uiState.value = transform(_uiState.value) }

    // endregion

    // region Active protocol

    /**
     * Groups (circuit / superset) are derived from the current step. Other
     * protocols come from the active timer. This keeps the protocol in sync the
     * moment the step changes, even before the timer emits a new state.
     */
    private fun resolveProtocol(
        base: TrainingSessionUiState,
        emomState: ActiveProtocolState,
        holdState: ActiveProtocolState,
    ): ActiveProtocolState {
        val step = (base.phase as? TrainingPhase.Working)?.step ?: return ActiveProtocolState.Idle
        return when {
            step.protocol.isGroup -> ActiveProtocolState.Group.from(step)
            step.isEmom -> emomState
            step.isHold -> holdState
            else -> ActiveProtocolState.Idle
        }
    }

    // endregion

    // region Session timer

    private fun startSessionTimer() {
        if (timerJob?.isActive == true) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            try {
                stopwatch.start().collect { (minutes, seconds) ->
                    updateState {
                        it.copy(
                            sessionTimer = formatTime(minutes, seconds),
                            elapsedSeconds = minutes * 60 + seconds,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    // endregion

    // region Queue loading

    private fun loadQueue() {
        viewModelScope.launch {
            // Only advance to the first step on the FIRST DB emission.
            // Without this flag, any later DAO update would reset progress.
            var initialised = false

            combine(
                blockDao.getBySession(sessionId),
                plannedSetDao.getBySession(sessionId),
                exerciseDao.getAll(),
            ) { blocks, sets, exercises ->
                buildQueue(buildBlocksWithSets(blocks, sets, exercises))
            }
                .catch { throwable ->
                    Napier.e("Failed to load training queue", throwable, TAG)
                    updateState { it.copy(phase = TrainingPhase.Finished) }
                }
                .collect { queue ->
                    updateState { it.copy(queue = queue) }

                    if (initialised) return@collect
                    initialised = true

                    if (queue.isEmpty()) {
                        updateState { it.copy(phase = TrainingPhase.Finished) }
                    } else {
                        goToWorking(0)
                    }
                }
        }
    }

    /**
     * Builds the linear list of [WorkoutStep]s from the loaded blocks.
     *
     * Group blocks produce one step per round; all other blocks produce one
     * step per `PlannedSet`.
     */
    private fun buildQueue(built: List<BlockWithSets>): List<WorkoutStep> =
        built.flatMap { blockWithSets ->
            val block = blockWithSets.block

            if (block.protocol.isGroup) {
                // One step per round: each round performs every exercise in the group.
                val rounds = (block.rounds ?: 1).coerceAtLeast(1)
                List(rounds) { index ->
                    WorkoutStep(
                        blockWithSets = blockWithSets,
                        set = PlannedSet(blockId = block.id ?: 0, setNumber = index + 1),
                        setIndex = index + 1,
                        totalSetsInBlock = rounds,
                    )
                }
            } else {
                blockWithSets.sets
                    .sortedBy { it.setNumber }
                    .mapIndexed { index, plannedSet ->
                        WorkoutStep(
                            blockWithSets = blockWithSets,
                            set = plannedSet,
                            setIndex = index + 1,
                            totalSetsInBlock = blockWithSets.sets.size,
                        )
                    }
            }
        }

    // endregion

    // region Phase transitions

    /**
     * Enters [TrainingPhase.Working] for [index], or finishes the session when
     * the index is out of bounds.
     */
    private fun goToWorking(index: Int) {
        restJob?.cancel()
        restJob = null
        emomTimer.reset()
        holdTimer.reset()

        val step = _uiState.value.queue.getOrNull(index)
        if (step == null) {
            updateState {
                it.copy(
                    phase = TrainingPhase.Finished,
                    currentIndex = index,
                    restSecondsLeft = 0L,
                    restEndsAtMillis = 0L,
                )
            }
            return
        }

        if (step.isEmom) emomTimer.configure(step)
        if (step.isHold) holdTimer.configure(step)

        updateState {
            it.copy(
                phase = TrainingPhase.Working(step),
                currentIndex = index,
                restSecondsLeft = 0L,
                restEndsAtMillis = 0L,
            )
        }
    }

    /**
     * Advances past the current step: either starts a rest (if configured) or
     * jumps straight to the next step. Centralises the logic that used to be
     * duplicated between [onEmomCompleted] and [onPrimaryAction].
     */
    private fun advanceAfterStep(step: WorkoutStep) {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        val rest = step.restAfterSeconds()

        when {
            nextIndex >= state.queue.size -> awaitFinish()
            rest > 0 -> startRest(step, nextIndex, rest)
            else -> goToWorking(nextIndex)
        }
    }

    /**
     * The last step was completed: freeze the clock and wait for the user to
     * confirm the end of the workout, instead of jumping straight to
     * [TrainingPhase.Finished].
     */
    private fun awaitFinish() {
        updateState { it.copy(awaitingFinish = true) }
    }

    /** Stops the session clock and returns the final elapsed time in millis. */
    private fun stopSessionTimer(): Long {
        stopwatch.stop()
        timerJob?.cancel()
        timerJob = null
        return stopwatch.elapsedMillis()
    }

    /** Confirms the end of the workout and moves to [TrainingPhase.Finished]. */
    private fun finishSession() {
        finishedAtMillis = Time.nowMillis()
        val elapsedSeconds = stopSessionTimer() / 1000

        restJob?.cancel()
        restJob = null
        emomTimer.reset()
        holdTimer.reset()

        updateState {
            it.copy(
                phase = TrainingPhase.Finished,
                awaitingFinish = false,
                sessionPaused = false,
                finishedAtMillis = finishedAtMillis,
                elapsedSeconds = elapsedSeconds,
                sessionTimer = formatTime(elapsedSeconds / 60, elapsedSeconds % 60),
            )
        }

        insertSessionLog()
    }
    // region Session log persistence

    /** Inserts the [SessionLog] for this workout once the user confirms it. */
    private fun insertSessionLog() {
        if (sessionLogId != null || insertLogJob?.isActive == true) return

        val state = _uiState.value
        insertLogJob = viewModelScope.launch {
            try {
                sessionLogId = sessionLogDao.insert(
                    SessionLog(
                        sessionId = sessionId,
                        completedAt = finishedAt(),
                        durationSeconds = state.elapsedSeconds.toInt(),
                        notes = state.notes.ifBlank { null },
                        startedAt = startedAt(),
                    )
                ).toInt()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to insert session log", e, TAG)
            }
        }
    }

    /** Persists the latest notes, waiting for the initial insert if needed. */
    private suspend fun persistNotes() {
        insertLogJob?.join()

        val logId = sessionLogId ?: return
        val state = _uiState.value

        try {
            sessionLogDao.update(
                SessionLog(
                    id = logId,
                    sessionId = sessionId,
                    completedAt = finishedAt(),
                    durationSeconds = state.elapsedSeconds.toInt(),
                    notes = state.notes.ifBlank { null },
                    startedAt = startedAt(),
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e("Failed to update session log notes", e, TAG)
        }
    }

    private fun scheduleNotesUpdate() {
        notesUpdateJob?.cancel()
        notesUpdateJob = viewModelScope.launch {
            delay(NOTES_UPDATE_DELAY_MILLIS.milliseconds)
            persistNotes()
        }
    }

    /**
     * Flushes the notes and only leaves the screen after the write has been
     * attempted, so the last keystrokes are not lost on navigation.
     */
    fun saveAndExit(onExit: () -> Unit) {
        notesUpdateJob?.cancel()
        notesUpdateJob = null

        viewModelScope.launch {
            try {
                persistNotes()
            } finally {
                onExit()
            }
        }
    }

    private fun finishedAt() = Instant.fromEpochMilliseconds(finishedAtMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    private fun startedAt() = _uiState.value.startedAtMillis
        .takeIf { it > 0L }
        ?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        }

    // endregion

    private fun startRest(step: WorkoutStep, nextIndex: Int, restSeconds: Int) {
        val endsAt = Time.nowMillis() + restSeconds * 1000L

        updateState {
            it.copy(
                phase = TrainingPhase.Rest(step, endsAt),
                restEndsAtMillis = endsAt,
                restSecondsLeft = restSeconds.toLong(),
            )
        }

        restJob?.cancel()
        restJob = viewModelScope.launch {
            try {
                restTimer.countdown(endsAt).collect { left ->
                    updateState { it.copy(restSecondsLeft = left) }
                    if (left <= 0) goToWorking(nextIndex)
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    // endregion

    // region User actions

    /** Primary action (complete set/round, skip rest or finish the workout). */
    fun onPrimaryAction() {
        val state = _uiState.value
        if (state.awaitingFinish) {
            finishSession()
            return
        }

        when (val phase = state.phase) {
            is TrainingPhase.Rest -> skipRest()

            is TrainingPhase.Working -> {
                val protocol = resolveProtocol(state, emomTimer.state.value, holdTimer.state.value)
                if (!protocol.canComplete) return
                advanceAfterStep(phase.step)
            }

            else -> Unit
        }
    }

    /** Skips the current rest, if any. */
    fun skipRest() {
        if (_uiState.value.phase !is TrainingPhase.Rest) return
        restJob?.cancel()
        restJob = null
        goToWorking(_uiState.value.currentIndex + 1)
    }

    /** Updates the notes the user is writing for the finished session. */
    fun onNotesChange(notes: String) {
        updateState { it.copy(notes = notes) }
        scheduleNotesUpdate()
    }

    /**
     * Pauses/resumes the session.
     *
     * Note: only the session stopwatch is paused. The rest and EMOM timers keep
     * running — if they should also be frozen, that's the place to add the
     * logic.
     */
    fun togglePause() {
        if (_uiState.value.phase is TrainingPhase.Finished) return

        val paused = !_uiState.value.sessionPaused
        updateState { it.copy(sessionPaused = paused) }

        if (paused) {
            stopwatch.stop()
            timerJob?.cancel()
            timerJob = null
        } else {
            startSessionTimer()
        }
    }

    /**
     * Starts the active protocol. Currently only EMOM has a startable timer;
     * the other protocols are advanced manually.
     */
    fun startActiveProtocol() {
        val protocol = resolveProtocol(
            _uiState.value,
            emomTimer.state.value,
            holdTimer.state.value,
        )
        when (protocol) {
            is ActiveProtocolState.Emom -> emomTimer.start()
            is ActiveProtocolState.Hold -> holdTimer.start()
            else -> Napier.w("startActiveProtocol called for unsupported protocol: $protocol", tag = TAG)
        }
    }

    // endregion

    // region Callbacks

    private fun onEmomCompleted() {
        val step = (_uiState.value.phase as? TrainingPhase.Working)?.step ?: return
        advanceAfterStep(step)
    }

    private fun onHoldCompleted() {
        val step = (_uiState.value.phase as? TrainingPhase.Working)?.step ?: return
        advanceAfterStep(step)
    }

    // endregion

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restJob?.cancel()
        emomTimer.cancel()
        holdTimer.cancel()
    }

    private fun formatTime(minutes: Long, seconds: Long): String {
        val m = minutes.toString().padStart(2, '0')
        val s = seconds.toString().padStart(2, '0')
        return "$m:$s"
    }

    companion object {
        private const val TAG = "TrainingSessionViewModel"
        private const val NOTES_UPDATE_DELAY_MILLIS = 400L
    }
}