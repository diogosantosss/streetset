package com.example.streetset.presentation.sessiondetail.model

import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet

/**
 * Draft -> entity mapping. The draft holds raw text from the form, so everything is parsed here.
 * Only the fields that make sense for the chosen protocol are kept.
 */

/** The block itself. For circuits and supersets this is the parent block (no exercise). */
fun TrainingBlockDraft.toBlock(
    sessionId: Int,
    position: Int,
    exerciseId: Int?,
): Block {
    val usesRest = !protocol.isGroup && protocol != TrainingProtocol.HOLD

    return Block(
        sessionId = sessionId,
        position = position,
        protocol = protocol,
        exerciseId = exerciseId,
        restSeconds = if (usesRest) parseSeconds(restSeconds) ?: 0 else 0,
        rounds = if (protocol.needsRounds) parsePositiveInt(rounds) else null,
        restBetweenRoundsSeconds = if (protocol.needsRounds) parseSeconds(restBetweenRoundsSeconds) ?: 0 else 0,
        holdSeconds = if (protocol == TrainingProtocol.HOLD) parseSeconds(holdSeconds) else null,
        rpe = parsePositiveInt(rpe)?.coerceIn(1, 10),
        bpm = if (protocol == TrainingProtocol.METRONOME) parsePositiveInt(bpm) else null,
    )
}

fun PlannedSetDraft.toPlannedSet(blockId: Int, setNumber: Int): PlannedSet = PlannedSet(
    blockId = blockId,
    setNumber = setNumber,
    repsText = reps.cleaned(),
    loadText = load.cleaned(),
)

/** A circuit/superset exercise is a child block with a single planned set. */
fun GroupExerciseDraft.toChildBlock(
    sessionId: Int,
    parentId: Int,
    position: Int,
    exerciseId: Int,
): Block = Block(
    sessionId = sessionId,
    parentId = parentId,
    position = position,
    protocol = TrainingProtocol.NORMAL,
    exerciseId = exerciseId,
)

fun GroupExerciseDraft.toPlannedSet(blockId: Int): PlannedSet = PlannedSet(
    blockId = blockId,
    setNumber = 1,
    repsText = reps.cleaned(),
    loadText = load.cleaned(),
)

// Parsing helpers
private val clockFormat = Regex("""(\d+):(\d{1,2})""")
private val minutesFormat = Regex("""(\d+)\s*(?:min|m)\s*(?:(\d+)\s*s?)?""")
private val secondsFormat = Regex("""(\d+)\s*s?""")

/** Accepts "90", "90s", "1:30", "2min", "1m30s". Returns null when empty or invalid. */
internal fun parseSeconds(text: String): Int? {
    val value = text.trim().lowercase()
    if (value.isEmpty()) return null

    clockFormat.matchEntire(value)?.let {
        val (minutes, seconds) = it.destructured
        return minutes.toInt() * 60 + seconds.toInt()
    }
    minutesFormat.matchEntire(value)?.let {
        val minutes = it.groupValues[1].toInt()
        val seconds = it.groupValues[2].toIntOrNull() ?: 0
        return minutes * 60 + seconds
    }
    return secondsFormat.matchEntire(value)?.groupValues?.get(1)?.toIntOrNull()
}

internal fun parsePositiveInt(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it > 0 }

private fun String.cleaned(): String? = trim().ifBlank { null }