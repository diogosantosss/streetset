package com.example.streetset.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.audio.AppAudio
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.buildBlocksWithSets
import com.example.streetset.presentation.training.components.EmomUiState
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.restAfterSeconds
import com.example.streetset.presentation.training.timer.EMOMTimer
import com.example.streetset.util.Time
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

class TrainingSessionViewModel(
    private val sessionId: Int,
) : ViewModel(), KoinComponent {

    private val blockDao: BlockDao by inject()
    private val plannedSetDao: PlannedSetDao by inject()
    private val exerciseDao: ExerciseDao by inject()

    private val stopwatch = Stopwatch()
    private val restTimer = RestTimer()
    private val emomTimer = EMOMTimer(viewModelScope)

    private var timerJob: Job? = null
    private var restJob: Job? = null

    private val _uiState = MutableStateFlow(TrainingSessionUiState())

    /**
     * Public UI state.
     *
     * The active protocol is derived from the base state + the running timer so
     * the UI never has to combine flows itself. `stateIn` keeps the flow hot
     * only while there are subscribers, saving cycles when the screen is hidden.
     */
    val uiState: StateFlow<TrainingSessionUiState> =
        combine(_uiState, emomTimer.state) { base, timerState ->
            base.copy(protocol = resolveProtocol(base, timerState))
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
    }

    // region State helpers

    /** Applies [transform] to the current state and publishes the result. */
    private inline fun updateState(transform: (TrainingSessionUiState) -> TrainingSessionUiState) {
        _uiState.value = transform(_uiState.value)
    }

    // endregion

    // region Active protocol

    /**
     * Groups (circuit / superset) are derived from the current step. Other
     * protocols come from the active timer. This keeps the protocol in sync the
     * moment the step changes, even before the timer emits a new state.
     */
    private fun resolveProtocol(
        base: TrainingSessionUiState,
        timerState: ActiveProtocolState,
    ): ActiveProtocolState {
        val step = (base.phase as? TrainingPhase.Working)?.step
        return if (step != null && step.protocol.isGroup) {
            ActiveProtocolState.Group.from(step)
        } else {
            timerState
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
                    updateState { it.copy(sessionTimer = formatTime(minutes, seconds)) }
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
            nextIndex >= state.queue.size -> updateState {
                it.copy(phase = TrainingPhase.Finished, currentIndex = nextIndex)
            }
            rest > 0 -> startRest(step, nextIndex, rest)
            else -> goToWorking(nextIndex)
        }
    }

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

    /** Primary action (complete set/round or skip rest). */
    fun onPrimaryAction() {
        val state = _uiState.value
        when (val phase = state.phase) {
            is TrainingPhase.Rest -> skipRest()

            is TrainingPhase.Working -> {
                val protocol = resolveProtocol(state, emomTimer.state.value)
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

    /**
     * Pauses/resumes the session.
     *
     * Note: only the session stopwatch is paused. The rest and EMOM timers keep
     * running — if they should also be frozen, that's the place to add the
     * logic.
     */
    fun togglePause() {
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
        when (val protocol = emomTimer.state.value) {
            is ActiveProtocolState.Emom -> emomTimer.start()
            else -> Napier.w("startActiveProtocol called for unsupported protocol: $protocol", tag = TAG)
        }
    }

    // endregion

    // region Callbacks

    private fun onEmomCompleted() {
        val step = (_uiState.value.phase as? TrainingPhase.Working)?.step ?: return
        advanceAfterStep(step)
    }

    // endregion

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restJob?.cancel()
        emomTimer.cancel()
    }

    private fun formatTime(minutes: Long, seconds: Long): String {
        val m = minutes.toString().padStart(2, '0')
        val s = seconds.toString().padStart(2, '0')
        return "$m:$s"
    }

    companion object {
        private const val TAG = "TrainingSessionViewModel"
    }
}