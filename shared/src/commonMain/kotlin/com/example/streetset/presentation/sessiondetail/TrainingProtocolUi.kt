package com.example.streetset.presentation.sessiondetail

import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol

val TrainingProtocol.label: String
    get() = when (this) {
        TrainingProtocol.NORMAL -> "Normal"
        TrainingProtocol.WARM_UP -> "Warm-up"
        TrainingProtocol.TOP_SET -> "Top set"
        TrainingProtocol.BACK_OFF -> "Back-off"
        TrainingProtocol.EMOM -> "EMOM"
        TrainingProtocol.HOLD -> "Hold"
        TrainingProtocol.CIRCUIT -> "Circuit"
        TrainingProtocol.SUPERSET -> "Superset"
        TrainingProtocol.METRONOME -> "Metronome"
    }

val TrainingProtocol.description: String
    get() = when (this) {
        TrainingProtocol.NORMAL -> "Sets with reps, load, rest and RPE"
        TrainingProtocol.WARM_UP -> "Preparation sets before your working sets"
        TrainingProtocol.TOP_SET -> "Your heaviest working set"
        TrainingProtocol.BACK_OFF -> "Follow-up sets at a lighter load"
        TrainingProtocol.EMOM -> "Repeat the work each minute"
        TrainingProtocol.HOLD -> "Hold each repetition for a set duration"
        TrainingProtocol.CIRCUIT -> "Complete each exercise, then repeat for rounds"
        TrainingProtocol.SUPERSET -> "Perform exercises back to back for rounds"
        TrainingProtocol.METRONOME -> "Perform reps in time with a set BPM"
    }