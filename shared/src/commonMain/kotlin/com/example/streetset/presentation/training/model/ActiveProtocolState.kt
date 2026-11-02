package com.example.streetset.presentation.training.model

import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.training.components.EmomUiState

private const val DefaultEmomIntervalSeconds = 60
private const val DefaultPrepareSeconds = 5

sealed interface ActiveProtocolState {
    /**
     * Whether the primary action (complete set or round) is available.
     * Timed protocols hold it back until they finish.
     */
    val canComplete: Boolean get() = true

    data object Idle : ActiveProtocolState

    data class Emom(
        val uiState: EmomUiState = EmomUiState.IDLE,
        val currentRound: Int = 0,
        val totalRounds: Int = 0,
        val secondsLeft: Int = DefaultEmomIntervalSeconds,
        val prepareSecondsLeft: Int = 5,
        val intervalSeconds: Int = DefaultEmomIntervalSeconds,
    ) : ActiveProtocolState {
        override val canComplete: Boolean get() = uiState == EmomUiState.COMPLETED

        val isLastRound: Boolean get() = totalRounds in 1..currentRound

        /** How much of the current interval has passed, from 0 to 1. */
        val progress: Float
            get() = if (intervalSeconds > 0) {
                (1f - secondsLeft.toFloat() / intervalSeconds).coerceIn(0f, 1f)
            } else {
                0f
            }
    }

    data class Hold(
        val uiState: HoldUiState = HoldUiState.IDLE,
        val secondsLeft: Int = 0,
        val targetSeconds: Int = 0,
        val prepareSecondsLeft: Int = 5,   // ← novo (mesmo valor do EMOM)
    ) : ActiveProtocolState {
        override val canComplete: Boolean get() = uiState == HoldUiState.COMPLETED

        val progress: Float
            get() = if (targetSeconds > 0) {
                (1f - secondsLeft.toFloat() / targetSeconds).coerceIn(0f, 1f)
            } else {
                0f
            }
    }

    data class Metronome(
        val uiState: MetronomeUiState = MetronomeUiState.IDLE,
        val bpm: Int = 60,
        val beatCount: Int = 0,
    ) : ActiveProtocolState {
        val isRunning: Boolean get() = uiState == MetronomeUiState.RUNNING

        val beatIntervalMillis: Long get() = if (bpm > 0) 60_000L / bpm else 0L
    }

    /** Circuit or superset. One step per round, completed manually. */
    data class Group(
        val protocol: TrainingProtocol,
        val currentRound: Int,
        val totalRounds: Int,
        /** Rest after this round, already resolved (between rounds, or after the last one). */
        val restSeconds: Int = 0,
    ) : ActiveProtocolState {
        val isSuperset: Boolean get() = protocol == TrainingProtocol.SUPERSET
        val isLastRound: Boolean get() = currentRound >= totalRounds

        companion object {
            fun from(step: WorkoutStep) = Group(
                protocol = step.protocol,
                currentRound = step.setIndex,
                totalRounds = step.totalSetsInBlock,
                restSeconds = step.restAfterSeconds(),
            )
        }
    }
}

enum class HoldUiState {
    IDLE,
    RUNNING,
    PREPARE_COUNTDOWN,
    COMPLETED,
}

enum class MetronomeUiState {
    IDLE,
    RUNNING,
}