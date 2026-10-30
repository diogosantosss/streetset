package com.example.streetset.presentation.training.model

import com.example.streetset.presentation.training.components.EmomUiState

sealed interface ActiveProtocolState {
    data object Idle : ActiveProtocolState

    data class Emom(
        val uiState: EmomUiState = EmomUiState.IDLE,
        val currentRound: Int = 0,
        val totalRounds: Int = 0,
        val secondsLeft: Int = 60,
        val prepareSecondsLeft: Int = 5,
        val intervalSeconds: Int = 60,
    ) : ActiveProtocolState

    data class Hold(
        val uiState: HoldUiState = HoldUiState.IDLE,
        val secondsLeft: Int = 0,
        val targetSeconds: Int = 0,
    ) : ActiveProtocolState

    data class Metronome(
        val bpm: Int = 60,
        val isRunning: Boolean = false,
    ) : ActiveProtocolState
}

enum class HoldUiState {
    IDLE,
    RUNNING,
    COMPLETED,
}
