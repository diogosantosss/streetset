package com.example.streetset.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.audio.AppAudio
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.buildBlocksWithSets
import com.example.streetset.presentation.training.components.EmomUiState
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.timer.EMOMTimer
import com.example.streetset.util.Time
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface TrainingPhase {
    data object Loading : TrainingPhase

    data class Working(val step: WorkoutStep) : TrainingPhase

    data class Rest(
        val step: WorkoutStep,
        val restEndsAtMillis: Long,
    ) : TrainingPhase

    data object Finished : TrainingPhase
}

data class TrainingSessionUiState(
    val sessionTimer: String = "00:00",
    val phase: TrainingPhase = TrainingPhase.Loading,
    val queue: List<WorkoutStep> = emptyList(),
    val currentIndex: Int = 0,
    val sessionPaused: Boolean = false,
    val restSecondsLeft: Long = 0L,
    val restEndsAtMillis: Long = 0L,
    val protocol: ActiveProtocolState = ActiveProtocolState.Idle,
) {
    val currentStep: WorkoutStep?
        get() = queue.getOrNull(currentIndex)

    val upcomingSteps: List<WorkoutStep>
        get() = queue.drop(currentIndex + 1)
}

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
    val uiState: StateFlow<TrainingSessionUiState> =
        combine(_uiState, emomTimer.state) { base, protocolState ->
            base.copy(protocol = protocolState)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TrainingSessionUiState(),
        )

    init {
        AppAudio.init()
        startSessionTimer()
        loadQueue()
        emomTimer.onAllRoundsCompleted = ::onEmomCompleted
    }

    private fun onEmomCompleted() {
        val current = _uiState.value
        val step = (current.phase as? TrainingPhase.Working)?.step ?: return
        val nextIndex = current.currentIndex + 1

        if (nextIndex >= current.queue.size) {
            _uiState.value = current.copy(phase = TrainingPhase.Finished, currentIndex = nextIndex)
            return
        }

        val rest = step.restSeconds
        if (rest > 0) startRest(step, nextIndex, rest) else goToWorking(nextIndex)
    }

    private fun startSessionTimer() {
        if (timerJob?.isActive == true) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            try {
                stopwatch.start().collect { (minutes, seconds) ->
                    _uiState.value = _uiState.value.copy(sessionTimer = formatTime(minutes, seconds))
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun loadQueue() {
        viewModelScope.launch {
            combine(
                blockDao.getBySession(sessionId),
                plannedSetDao.getBySession(sessionId),
                exerciseDao.getAll(),
            ) { blocks, sets, exercises ->
                buildQueue(buildBlocksWithSets(blocks, sets, exercises))
            }
                .catch { throwable ->
                    Napier.e("Failed to load training queue", throwable, TAG)
                    _uiState.value = _uiState.value.copy(phase = TrainingPhase.Finished)
                }
                .collect { queue ->
                    _uiState.value = _uiState.value.copy(queue = queue)
                    if (queue.isNotEmpty()) {
                        goToWorking(0)
                    } else {
                        _uiState.value = _uiState.value.copy(phase = TrainingPhase.Finished)
                    }
                }
        }
    }

    private fun buildQueue(built: List<BlockWithSets>): List<WorkoutStep> {
        val result = mutableListOf<WorkoutStep>()
        built.forEach { blockWithSets ->
            val blockSets = blockWithSets.sets.sortedBy { it.setNumber }
            blockSets.forEachIndexed { index, plannedSet ->
                result.add(
                    WorkoutStep(
                        blockWithSets = blockWithSets,
                        set = plannedSet,
                        setIndex = index + 1,
                        totalSetsInBlock = blockSets.size,
                    ),
                )
            }
        }
        return result
    }

    private fun goToWorking(index: Int) {
        restJob?.cancel()
        restJob = null
        emomTimer.reset()

        val step = _uiState.value.queue.getOrNull(index) ?: run {
            _uiState.value = _uiState.value.copy(
                phase = TrainingPhase.Finished,
                currentIndex = index,
                restSecondsLeft = 0L,
                restEndsAtMillis = 0L,
            )
            return
        }

        if (step.isEmom) emomTimer.configure(step)

        _uiState.value = _uiState.value.copy(
            phase = TrainingPhase.Working(step),
            currentIndex = index,
            restSecondsLeft = 0L,
            restEndsAtMillis = 0L,
        )
    }

    fun onPrimaryAction() {
        val state = _uiState.value
        when (val phase = state.phase) {
            is TrainingPhase.Rest -> skipRest()
            is TrainingPhase.Working -> {
                val step = phase.step
                if (step.isEmom && (emomTimer.state.value as? ActiveProtocolState.Emom)?.uiState != EmomUiState.COMPLETED) return

                val nextIndex = state.currentIndex + 1
                val rest = step.restSeconds

                when {
                    nextIndex >= state.queue.size -> _uiState.value = state.copy(phase = TrainingPhase.Finished, currentIndex = nextIndex)
                    rest > 0 -> startRest(step, nextIndex, rest)
                    else -> goToWorking(nextIndex)
                }
            }
            else -> Unit
        }
    }

    private fun startRest(step: WorkoutStep, nextIndex: Int, restSeconds: Int) {
        val endsAt = Time.nowMillis() + restSeconds * 1000L
        _uiState.value = _uiState.value.copy(
            phase = TrainingPhase.Rest(step, endsAt),
            restEndsAtMillis = endsAt,
        )
        restJob?.cancel()
        restJob = viewModelScope.launch {
            try {
                restTimer.countdown(endsAt).collect { left ->
                    _uiState.value = _uiState.value.copy(restSecondsLeft = left)
                    if (left <= 0) goToWorking(nextIndex)
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun skipRest() {
        val state = _uiState.value
        if (state.phase !is TrainingPhase.Rest) return
        restJob?.cancel()
        restJob = null
        goToWorking(state.currentIndex + 1)
    }

    fun togglePause() {
        val paused = !_uiState.value.sessionPaused
        _uiState.value = _uiState.value.copy(sessionPaused = paused)
        if (paused) {
            stopwatch.stop()
            timerJob?.cancel()
            timerJob = null
        } else {
            startSessionTimer()
        }
    }

    fun startActiveProtocol() {
        val protocol = emomTimer.state.value
        if (protocol is ActiveProtocolState.Emom) {
            emomTimer.start()
        }
    }

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