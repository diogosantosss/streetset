package com.example.streetset.presentation.training.timer

import com.example.streetset.audio.CountdownAudio
import com.example.streetset.presentation.training.components.EmomUiState
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class EMOMTimer(
    private val scope: CoroutineScope,
) : ProtocolTimer {

    private val _state = MutableStateFlow<ActiveProtocolState>(ActiveProtocolState.Idle)
    override val state: StateFlow<ActiveProtocolState> = _state.asStateFlow()

    private var prepareJob: Job? = null
    private var roundJob: Job? = null

    private val audio = CountdownAudio()

    var onAllRoundsCompleted: (() -> Unit)? = null

    override fun configure(step: WorkoutStep) {
        val block = step.blockWithSets.block
        val interval = block.restBetweenRoundsSeconds
            .takeIf { it > 0 }
            ?: block.restSeconds.takeIf { it > 0 }
            ?: 60

        audio.reset()

        _state.value = ActiveProtocolState.Emom(
            uiState = EmomUiState.IDLE,
            currentRound = 0,
            totalRounds = step.emomRounds,
            secondsLeft = interval,
            prepareSecondsLeft = 5,
            intervalSeconds = interval,
        )
    }

    override fun start() {
        val current = _state.value as? ActiveProtocolState.Emom ?: return
        if (current.uiState != EmomUiState.IDLE) return
        if (current.totalRounds <= 0) return

        cancel()

        audio.reset()

        _state.value = current.copy(
            uiState = EmomUiState.PREPARE_COUNTDOWN,
            prepareSecondsLeft = 5,
        )

        prepareJob = scope.launch {
            try {
                var count = 5
                while (count >= 1) {
                    _state.update { state ->
                        val emom = state as? ActiveProtocolState.Emom ?: return@update state
                        emom.copy(prepareSecondsLeft = count)
                    }
                    audio.onTick(count.toLong())
                    delay(1000.milliseconds)
                    count--
                }
                _state.update { state ->
                    val emom = state as? ActiveProtocolState.Emom ?: return@update state
                    emom.copy(prepareSecondsLeft = 0)
                }
                audio.onTick(0L)
                startRound(0)
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun startRound(roundIndex: Int) {
        val current = _state.value as? ActiveProtocolState.Emom ?: return
        if (roundIndex >= current.totalRounds) {
            complete()
            return
        }

        cancelRoundOnly()
        audio.reset()
        _state.value = current.copy(
            uiState = EmomUiState.RUNNING,
            currentRound = roundIndex,
            secondsLeft = current.intervalSeconds,
        )

        roundJob = scope.launch {
            try {
                var seconds = current.intervalSeconds
                while (seconds >= 0) {
                    _state.update { state ->
                        val emom = state as? ActiveProtocolState.Emom ?: return@update state
                        emom.copy(secondsLeft = seconds)
                    }
                    audio.onTick(seconds.toLong())
                    if (seconds == 0) break
                    delay(1000.milliseconds)
                    seconds--
                }
                delay(200.milliseconds)
                val next = roundIndex + 1
                val latest = _state.value as? ActiveProtocolState.Emom ?: return@launch
                if (next < latest.totalRounds) {
                    startRound(next)
                } else {
                    complete()
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun complete() {
        cancelRoundOnly()
        _state.update { state ->
            val emom = state as? ActiveProtocolState.Emom ?: return@update state
            emom.copy(uiState = EmomUiState.COMPLETED)
        }
        onAllRoundsCompleted?.invoke()
    }

    override fun cancel() {
        prepareJob?.cancel()
        roundJob?.cancel()
        prepareJob = null
        roundJob = null
    }

    private fun cancelRoundOnly() {
        roundJob?.cancel()
        roundJob = null
    }

    override fun reset() {
        cancel()
        audio.reset()
        _state.value = ActiveProtocolState.Idle
    }
}