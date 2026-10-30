package com.example.streetset.presentation.training.timer

import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.presentation.training.model.WorkoutStep
import kotlinx.coroutines.flow.StateFlow

interface ProtocolTimer {
    val state: StateFlow<ActiveProtocolState>

    fun configure(step: WorkoutStep)

    fun start()

    fun cancel()

    fun reset()
}
