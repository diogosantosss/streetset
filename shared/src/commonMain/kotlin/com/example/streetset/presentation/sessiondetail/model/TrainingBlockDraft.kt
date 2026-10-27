package com.example.streetset.presentation.sessiondetail.model

data class TrainingBlockDraft(
    val protocol: TrainingProtocol = TrainingProtocol.NORMAL,
    val exerciseName: String = "",
    val sets: List<PlannedSetDraft> = listOf(PlannedSetDraft()),
    val restSeconds: String = "",
    val rpe: String = "",
    val bpm: String = "",
    val holdSeconds: String = "",
    val rounds: String = "",
    val restBetweenRoundsSeconds: String = "",
    val exercises: List<GroupExerciseDraft> = listOf(GroupExerciseDraft()),
)

/** Whether the draft has the minimum needed to be saved. The ViewModel can reuse this. */
val TrainingBlockDraft.isValid: Boolean
    get() = if (protocol.isGroup) {
        exercises.isNotEmpty() && exercises.all { it.name.isNotBlank() }
    } else {
        exerciseName.isNotBlank()
    }
