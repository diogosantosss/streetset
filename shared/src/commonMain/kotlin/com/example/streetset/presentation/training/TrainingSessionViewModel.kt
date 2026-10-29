package com.example.streetset.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.util.Time
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface TrainingPhase {
    data object Loading : TrainingPhase

    data class Working(val step: WorkoutStep) : TrainingPhase

    data class Rest(
        val step: WorkoutStep,
        val restEndsAtMillis: Long
    ) : TrainingPhase

    data object Finished : TrainingPhase
}

data class TrainingSessionUiState(
    val sessionTimer: String = "00:00",
    val phase: TrainingPhase = TrainingPhase.Loading,
    val queue: List<WorkoutStep> = emptyList(),
    val currentIndex: Int = 0,
    val sessionPaused: Boolean = false,
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
    private var timerJob: Job? = null
    private var restJob: Job? = null

    private val _uiState = MutableStateFlow(TrainingSessionUiState())
    val uiState: StateFlow<TrainingSessionUiState> = _uiState.asStateFlow()

    private val _restSecondsLeft = MutableStateFlow(0L)
    val restSecondsLeft: StateFlow<Long> = _restSecondsLeft.asStateFlow()

    init {
        startSessionTimer()
        loadQueue()
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
                buildQueue(blocks, sets, exercises)
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

    private fun buildQueue(
        blocks: List<Block>,
        sets: List<PlannedSet>,
        exercises: List<Exercise>,
    ): List<WorkoutStep> {
        val exerciseNames = exercises
            .mapNotNull { exercise -> exercise.id?.let { it to exercise.name } }
            .toMap()
        val setsByBlock = sets.groupBy { it.blockId }
        val childrenByParent = blocks.filter { it.parentId != null }.groupBy { it.parentId }

        fun Block.toItem(children: List<BlockWithSets> = emptyList()) = BlockWithSets(
            block = this,
            exerciseName = exerciseId?.let { exerciseNames[it] },
            sets = id?.let { setsByBlock[it] }.orEmpty().sortedBy { it.setNumber },
            children = children,
        )

        val built = blocks
            .filter { it.parentId == null }
            .sortedBy { it.position }
            .map { parent ->
                val children = parent.id
                    ?.let { childrenByParent[it] }
                    .orEmpty()
                    .sortedBy { it.position }
                    .map(Block::toItem)
                parent.toItem(children)
            }

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
        _restSecondsLeft.value = 0L
        val step = _uiState.value.queue.getOrNull(index) ?: run {
            _uiState.value = _uiState.value.copy(phase = TrainingPhase.Finished, currentIndex = index)
            return
        }
        _uiState.value = _uiState.value.copy(
            phase = TrainingPhase.Working(step),
            currentIndex = index,
        )
    }

    fun onPrimaryAction() {
        val state = _uiState.value
        when (val phase = state.phase) {
            is TrainingPhase.Working -> {
                val rest = phase.step.blockWithSets.block.restSeconds
                val nextIndex = state.currentIndex + 1
                if (rest > 0 && nextIndex < state.queue.size) {
                    startRest(phase.step, nextIndex, rest)
                } else if (nextIndex >= state.queue.size) {
                    _uiState.value = state.copy(phase = TrainingPhase.Finished, currentIndex = nextIndex)
                } else {
                    goToWorking(nextIndex)
                }
            }
            is TrainingPhase.Rest -> {
                skipRest()
            }
            else -> Unit
        }
    }

    private fun startRest(step: WorkoutStep, nextIndex: Int, restSeconds: Int) {
        val endsAt = Time.nowMillis() + restSeconds * 1000L
        _uiState.value = _uiState.value.copy(phase = TrainingPhase.Rest(step, endsAt))
        restJob?.cancel()
        restJob = viewModelScope.launch {
            try {
                restTimer.countdown(endsAt).collect { left ->
                    _restSecondsLeft.value = left
                    if (left <= 0) {
                        goToWorking(nextIndex)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun skipRest() {
        val state = _uiState.value
        val phase = state.phase as? TrainingPhase.Rest ?: return
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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restJob?.cancel()
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
