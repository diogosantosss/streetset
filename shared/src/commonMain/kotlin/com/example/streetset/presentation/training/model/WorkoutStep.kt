package com.example.streetset.presentation.training.model

import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.label
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol

data class WorkoutStep(
    val blockWithSets: BlockWithSets,
    val set: PlannedSet,
    val setIndex: Int,
    val totalSetsInBlock: Int,
    val stepIndex: Int = 0,
) {
    val exerciseName: String
        get() = blockWithSets.exerciseName?.takeIf { it.isNotBlank() } ?: "-"

    val protocol: TrainingProtocol
        get() = blockWithSets.block.protocol

    val restSeconds: Int
        get() = blockWithSets.block.restSeconds

    val rpe: Int?
        get() = blockWithSets.block.rpe

    val repsDisplay: String
        get() = set.repsText?.trim().takeUnless { it.isNullOrEmpty() } ?: "-"

    val loadRaw: String?
        get() = set.loadText

    val loadPair: Pair<String, String?>
        get() = splitLoad(loadRaw)

    val loadValue: String
        get() = loadPair.first

    val loadUnit: String?
        get() = loadPair.second

    val protocolDisplay: String
        get() = protocol.label

    val restDisplayShort: String
        get() = if (restSeconds > 0) formatRestSecondsShort(restSeconds) else ""

    val isEmom: Boolean
        get() = protocol == TrainingProtocol.EMOM

    val emomRounds: Int
        get() {
            val blockRounds = blockWithSets.block.rounds
            if (blockRounds != null && blockRounds > 0) return blockRounds
            return if (totalSetsInBlock > 0) totalSetsInBlock else 1
        }

    private fun splitLoad(raw: String?): Pair<String, String?> {
        val load = raw?.trim().orEmpty()
        return when {
            load.isEmpty() -> "-" to null
            isNumericLoad(load) -> load to "kg"
            else -> load to null
        }
    }

    private fun isNumericLoad(load: String): Boolean {
        val normalized = load.replace(",", ".")
        return normalized.toDoubleOrNull() != null
    }
}

fun formatRestSecondsShort(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0 && s > 0) "${m}m ${s}s" else if (m > 0) "${m}m" else "${s}s"
}
