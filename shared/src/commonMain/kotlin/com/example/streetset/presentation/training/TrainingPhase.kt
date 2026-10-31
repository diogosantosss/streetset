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

/**
 * Full state exposed to the UI.
 *
 * Some fields (`restSecondsLeft`, `restEndsAtMillis`) only make sense during
 * [TrainingPhase.Rest], but they live at the state root so the UI doesn't have
 * to cast on every read.
 */
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
    /** Step the user should be performing right now. */
    val currentStep: WorkoutStep?
        get() = queue.getOrNull(currentIndex)

    /** Remaining steps to be performed. */
    val upcomingSteps: List<WorkoutStep>
        get() = queue.drop(currentIndex + 1)
}