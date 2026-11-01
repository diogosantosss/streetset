package com.example.streetset.presentation.training.timer

import com.example.streetset.audio.CountdownAudio
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.HoldUiState
import com.example.streetset.presentation.training.model.WorkoutStep
import com.example.streetset.presentation.training.model.holdTargetSeconds
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

class HoldTimer(
    private val scope: CoroutineScope,
) : ProtocolTimer {

    private val _state = MutableStateFlow<ActiveProtocolState>(ActiveProtocolState.Idle)
    override val state: StateFlow<ActiveProtocolState> = _state.asStateFlow()

    private var prepareJob: Job? = null
    private var holdJob: Job? = null

    private val audio = CountdownAudio()

    var onCompleted: (() -> Unit)? = null

    override fun configure(step: WorkoutStep) {
        val target = step.holdTargetSeconds()
        audio.reset()
        _state.value = ActiveProtocolState.Hold(
            uiState = HoldUiState.IDLE,
            secondsLeft = target,
            targetSeconds = target,
            prepareSecondsLeft = 5,
        )
    }

    override fun start() {
        val current = _state.value as? ActiveProtocolState.Hold ?: return
        if (current.uiState != HoldUiState.IDLE) return
        if (current.targetSeconds <= 0) return

        cancel()
        audio.reset()

        _state.value = current.copy(
            uiState = HoldUiState.PREPARE_COUNTDOWN,
            prepareSecondsLeft = 5,
        )

        prepareJob = scope.launch {
            try {
                var count = 5
                while (count >= 1) {
                    _state.update { state ->
                        val hold = state as? ActiveProtocolState.Hold ?: return@update state
                        hold.copy(prepareSecondsLeft = count)
                    }
                    audio.onTick(count.toLong())
                    delay(1000.milliseconds)
                    count--
                }
                _state.update { state ->
                    val hold = state as? ActiveProtocolState.Hold ?: return@update state
                    hold.copy(prepareSecondsLeft = 0)
                }
                audio.onTick(0L)
                startHold()
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun startHold() {
        val current = _state.value as? ActiveProtocolState.Hold ?: return
        cancelHoldOnly()
        audio.reset()

        _state.value = current.copy(
            uiState = HoldUiState.RUNNING,
            secondsLeft = current.targetSeconds,
        )

        holdJob = scope.launch {
            try {
                var seconds = current.targetSeconds
                while (seconds >= 0) {
                    _state.update { state ->
                        val hold = state as? ActiveProtocolState.Hold ?: return@update state
                        hold.copy(secondsLeft = seconds)
                    }
                    audio.onTick(seconds.toLong())
                    if (seconds == 0) break
                    delay(1000.milliseconds)
                    seconds--
                }
                delay(200.milliseconds)
                complete()
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun complete() {
        cancelHoldOnly()
        _state.update { state ->
            val hold = state as? ActiveProtocolState.Hold ?: return@update state
            hold.copy(uiState = HoldUiState.COMPLETED, secondsLeft = 0)
        }
        onCompleted?.invoke()
    }

    override fun cancel() {
        prepareJob?.cancel()
        holdJob?.cancel()
        prepareJob = null
        holdJob = null
    }

    private fun cancelHoldOnly() {
        holdJob?.cancel()
        holdJob = null
    }

    override fun reset() {
        cancel()
        audio.reset()
        _state.value = ActiveProtocolState.Idle
    }
}
