package com.example.streetset.presentation.training.model

import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets

data class WorkoutStep(
    val blockWithSets: BlockWithSets,
    val set: PlannedSet,
    val setIndex: Int,
    val totalSetsInBlock: Int,
    val stepIndex: Int = 0,
) {
    val exerciseName: String
        get() = blockWithSets.exerciseName?.takeIf { it.isNotBlank() } ?: "-"

    val protocol: String
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
        get() = protocolLabel(protocol)

    val restDisplayShort: String
        get() = if (restSeconds > 0) formatRestSecondsShort(restSeconds) else ""

    private fun protocolLabel(protocol: String): String =
        when (protocol.uppercase().replace("-", "_").replace(" ", "_")) {
            "NORMAL" -> "Normal"
            "WARM_UP", "WARMUP" -> "Warm-up"
            "TOP_SET", "TOPSET" -> "Top set"
            "BACK_OFF", "BACKOFF" -> "Back-off"
            else -> protocol.replace("_", " ")
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
