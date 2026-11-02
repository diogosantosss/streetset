package com.example.streetset.presentation.training.timer

import com.example.streetset.audio.AppAudio
import com.example.streetset.audio.AppSound
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.MetronomeUiState
import com.example.streetset.presentation.training.model.WorkoutStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class MetronomeTimer(
    private val scope: CoroutineScope,
) : ProtocolTimer {

    private val _state = MutableStateFlow<ActiveProtocolState>(ActiveProtocolState.Idle)
    override val state: StateFlow<ActiveProtocolState> = _state.asStateFlow()

    private var beatJob: Job? = null

    override fun configure(step: WorkoutStep) {
        _state.value = ActiveProtocolState.Metronome(
            uiState = MetronomeUiState.IDLE,
            bpm = step.metronomeBpm,
            beatCount = 0,
        )
    }

    override fun start() {
        val current = _state.value as? ActiveProtocolState.Metronome ?: return
        if (current.isRunning || current.bpm <= 0) return

        beatJob?.cancel()
        _state.value = current.copy(uiState = MetronomeUiState.RUNNING, beatCount = 0)

        beatJob = scope.launch {
            // Every beat is scheduled against the same start, so the time spent
            // updating the state and playing the sound doesn't add up as drift
            val interval = (60_000.0 / current.bpm).milliseconds
            val startedAt = TimeSource.Monotonic.markNow()
            var beat = 0

            while (isActive) {
                beat++
                _state.update { state ->
                    (state as? ActiveProtocolState.Metronome)?.copy(beatCount = beat) ?: state
                }
                AppAudio.play(AppSound.BEEP)
                delay(interval * beat - startedAt.elapsedNow())
            }
        }
    }

    /** Stops the beat but keeps the metronome configured, ready to start again. */
    fun stop() {
        beatJob?.cancel()
        beatJob = null
        _state.update { state ->
            (state as? ActiveProtocolState.Metronome)
                ?.copy(uiState = MetronomeUiState.IDLE, beatCount = 0)
                ?: state
        }
    }

    override fun cancel() {
        beatJob?.cancel()
        beatJob = null
    }

    override fun reset() {
        cancel()
        _state.value = ActiveProtocolState.Idle
    }
}