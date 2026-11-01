package com.example.streetset.presentation.training

import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep

/**
 * Current phase of a training session.
 *
 * Phases are intentionally simple: every transition is driven by
 * [TrainingSessionViewModel], which coordinates the timers (rest, EMOM, etc.).
 */
sealed interface TrainingPhase {

    /** Initial queue load. No step is ready to be performed yet. */
    data object Loading : TrainingPhase

    /** The user is currently performing [step]. */
    data class Working(val step: WorkoutStep) : TrainingPhase

    /**
     * Rest between steps.
     *
     * @param step the step that was just completed (source of the rest).
     * @param restEndsAtMillis absolute instant (epoch millis) when the rest ends.
     *   Useful for progress and to survive recompositions.
     */
    data class Rest(
        val step: WorkoutStep,
        val restEndsAtMillis: Long,
    ) : TrainingPhase

    /** Every step has been completed. */
    data object Finished : TrainingPhase
}

data class TrainingSessionUiState(
    val sessionTimer: String = "00:00",
    val startedAtMillis: Long = 0L,
    val finishedAtMillis: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val notes: String = "",
    val phase: TrainingPhase = TrainingPhase.Loading,
    val queue: List<WorkoutStep> = emptyList(),
    val currentIndex: Int = 0,
    val sessionPaused: Boolean = false,
    val awaitingFinish: Boolean = false,
    val restSecondsLeft: Long = 0L,
    val restEndsAtMillis: Long = 0L,
    val protocol: ActiveProtocolState = ActiveProtocolState.Idle,
) {
    val currentStep: WorkoutStep?
        get() = queue.getOrNull(currentIndex)

    val upcomingSteps: List<WorkoutStep>
        get() = queue.drop(currentIndex + 1)

    /** Number of steps performed. One step per set, or per round for groups. */
    val setsCompleted: Int
        get() = queue.size

    /** Total reps across all performed steps. Non-numeric reps count as the first number found. */
    val totalReps: Int
        get() = queue.sumOf { step ->
            if (step.protocol.isGroup) {
                step.blockWithSets.children.sumOf { child ->
                    child.sets.sumOf { it.repsText.firstIntOrNull() }
                }
            } else {
                step.set.repsText.firstIntOrNull()
            }
        }

    /**
     * Total tonnage lifted, in kilograms.
     *
     * Reps that are not a plain number contribute the first integer they contain
     * (e.g. "10-12" → 10) so that ranges still count towards the tonnage.
     */
    val volumeKg: Double
        get() = queue.sumOf { step ->
            if (step.protocol.isGroup) {
                step.blockWithSets.children.sumOf { child ->
                    child.sets.sumOf { setVolume(it.repsText, it.loadText) }
                }
            } else {
                setVolume(step.set.repsText, step.set.loadText)
            }
        }
}

private val numberRegex = Regex("""\d+""")

private fun String?.firstIntOrNull(): Int =
    this?.let { numberRegex.find(it)?.value?.toIntOrNull() } ?: 0

private fun setVolume(reps: String?, load: String?): Double {
    val repsN = reps.firstIntOrNull()
    if (repsN == 0) return 0.0

    val loadN = load
        ?.trim()
        ?.removeSuffix("kg")
        ?.replace(",", ".")
        ?.trim()
        ?.toDoubleOrNull()
        ?: return 0.0

    return repsN * loadN
}