package com.example.streetset.presentation.sessiondetail.model

enum class TrainingProtocol {
    NORMAL, WARM_UP, TOP_SET, BACK_OFF, EMOM, HOLD, CIRCUIT, SUPERSET, METRONOME;

    val isGroup get() = this == CIRCUIT || this == SUPERSET
    val needsRounds get() = this == EMOM || isGroup
}
