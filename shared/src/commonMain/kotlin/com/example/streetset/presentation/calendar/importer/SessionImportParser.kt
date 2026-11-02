package com.example.streetset.presentation.calendar.importer

import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.sessiondetail.model.parsePositiveInt
import com.example.streetset.presentation.sessiondetail.model.parseSeconds
import kotlinx.datetime.LocalDate

data class ImportedSet(val reps: String?, val load: String?)

data class ImportedExercise(val name: String, val sets: List<ImportedSet>)

data class ImportedBlock(
    val protocol: TrainingProtocol,
    /** Null for circuits and supersets, which carry their exercises in [children]. */
    val exerciseName: String?,
    val sets: List<ImportedSet>,
    val children: List<ImportedExercise>,
    val restSeconds: Int,
    val rpe: Int?,
    val rounds: Int?,
    val restBetweenRoundsSeconds: Int,
    val holdSeconds: Int?,
    val bpm: Int?,
)

data class ImportedSession(val date: LocalDate, val name: String, val blocks: List<ImportedBlock>)

data class ImportRowError(val line: Int, val message: String)

data class ImportParseResult(
    val sessions: List<ImportedSession>,
    val errors: List<ImportRowError>,
) {
    val isValid: Boolean get() = errors.isEmpty() && sessions.isNotEmpty()

    val blockCount: Int get() = sessions.sumOf { it.blocks.size }

    companion object {
        val Empty = ImportParseResult(emptyList(), emptyList())
    }
}

/**
 * Reads rows copied from a spreadsheet:
 * date, session, block, exercise, protocol, reps, load, rest, rpe, rounds, rest_between, hold, bpm
 */
object SessionImportParser {

    private object Col {
        const val DATE = 0
        const val SESSION = 1
        const val BLOCK = 2
        const val EXERCISE = 3
        const val PROTOCOL = 4
        const val REPS = 5
        const val LOAD = 6
        const val REST = 7
        const val RPE = 8
        const val ROUNDS = 9
        const val REST_BETWEEN = 10
        const val HOLD = 11
        const val BPM = 12
        const val COUNT = 13
    }

    private class Row(val line: Int, private val cells: List<String>) {
        operator fun get(column: Int): String = cells[column]
    }

    private class SessionBuilder(val date: LocalDate, val name: String) {
        val blocks = mutableListOf<BlockBuilder>()
    }

    private class BlockBuilder(val line: Int, val protocol: TrainingProtocol) {
        var exerciseName: String? = null
        val sets = mutableListOf<ImportedSet>()
        val children = mutableListOf<ImportedExercise>()
        var restSeconds = 0
        var rpe: Int? = null
        var rounds: Int? = null
        var restBetweenRoundsSeconds = 0
        var holdSeconds: Int? = null
        var bpm: Int? = null
    }

    fun parse(text: String): ImportParseResult {
        if (text.isBlank()) return ImportParseResult.Empty

        // Tabs come from a spreadsheet copy. Semicolons cover a CSV saved in a European locale.
        val delimiter = if ('\t' in text) '\t' else ';'
        val errors = mutableListOf<ImportRowError>()
        val sessions = mutableListOf<SessionBuilder>()

        var session: SessionBuilder? = null
        var block: BlockBuilder? = null
        var lastBlockNumber = ""
        var ignoring = false
        var headerChecked = false

        for ((index, raw) in text.lines().withIndex()) {
            val line = index + 1
            val cells = raw.split(delimiter).map { it.cleanCell() }
            if (cells.all { it.isEmpty() }) continue

            if (!headerChecked) {
                headerChecked = true
                if (cells[0].equals("date", ignoreCase = true)) continue
            }

            val row = Row(line, cells + List((Col.COUNT - cells.size).coerceAtLeast(0)) { "" })

            // Session
            val dateCell = row[Col.DATE]
            val nameCell = row[Col.SESSION]
            if (dateCell.isNotEmpty()) {
                val date = parseDate(dateCell)
                if (date == null) {
                    errors += ImportRowError(line, "Invalid date \"$dateCell\", use dd/mm/yyyy")
                    session = null
                    block = null
                    ignoring = true
                    continue
                }
                // Same date and name as the session being read: the cells were filled down
                val sameAsCurrent = session != null &&
                        session.date == date &&
                        (nameCell.isEmpty() || nameCell.equals(session.name, ignoreCase = true))
                if (!sameAsCurrent) {
                    session = SessionBuilder(date, nameCell).also { sessions += it }
                    block = null
                    lastBlockNumber = ""
                }
                ignoring = false
            } else if (nameCell.isNotEmpty() && session != null && !nameCell.equals(session.name, ignoreCase = true)) {
                session = SessionBuilder(session.date, nameCell).also { sessions += it }
                block = null
                lastBlockNumber = ""
            }

            val currentSession = session
            if (currentSession == null) {
                if (!ignoring) {
                    errors += ImportRowError(line, "The first row of a session needs a date")
                    ignoring = true
                }
                continue
            }

            // Block
            val blockNumber = row[Col.BLOCK]
            val protocolCell = row[Col.PROTOCOL]
            val startsBlock = block == null ||
                    protocolCell.isNotEmpty() ||
                    (blockNumber.isNotEmpty() && blockNumber != lastBlockNumber)
            if (blockNumber.isNotEmpty()) lastBlockNumber = blockNumber

            if (startsBlock) {
                val protocol = if (protocolCell.isEmpty()) TrainingProtocol.NORMAL else parseProtocol(protocolCell)
                if (protocol == null) {
                    errors += ImportRowError(line, "Unknown protocol \"$protocolCell\"")
                    // Not added to the session, so the rows that continue it are skipped
                    block = BlockBuilder(line, TrainingProtocol.NORMAL)
                    continue
                }
                val builder = BlockBuilder(line, protocol)
                readValues(row, builder, errors)
                readFirstRow(row, builder, errors)
                currentSession.blocks += builder
                block = builder
                continue
            }

            val existing = block ?: continue
            if (existing !in currentSession.blocks) continue

            if (existing.protocol.isGroup) readChild(row, existing, errors) else readExtraSets(row, existing, errors)
        }

        val result = sessions.map { builder ->
            ImportedSession(
                date = builder.date,
                name = builder.name,
                blocks = builder.blocks.mapNotNull { it.build(errors) },
            )
        }
        return ImportParseResult(result, errors.sortedBy { it.line })
    }

    // Rows
    private fun readValues(row: Row, block: BlockBuilder, errors: MutableList<ImportRowError>) {
        fun seconds(column: Int, label: String): Int? {
            val text = row[column]
            if (text.isEmpty()) return null
            return parseSeconds(text) ?: run {
                errors += ImportRowError(row.line, "Invalid $label \"$text\"")
                null
            }
        }

        fun count(column: Int, label: String): Int? {
            val text = row[column]
            if (text.isEmpty()) return null
            return parsePositiveInt(text) ?: run {
                errors += ImportRowError(row.line, "Invalid $label \"$text\"")
                null
            }
        }

        block.restSeconds = seconds(Col.REST, "rest") ?: 0
        block.rpe = count(Col.RPE, "RPE")?.coerceIn(1, 10)
        block.rounds = count(Col.ROUNDS, "rounds")
        block.restBetweenRoundsSeconds = seconds(Col.REST_BETWEEN, "rest between rounds") ?: 0
        block.holdSeconds = seconds(Col.HOLD, "hold")
        block.bpm = count(Col.BPM, "BPM")
    }

    private fun readFirstRow(row: Row, block: BlockBuilder, errors: MutableList<ImportRowError>) {
        if (block.protocol.isGroup) {
            if (row[Col.EXERCISE].isNotEmpty() || row[Col.REPS].isNotEmpty() || row[Col.LOAD].isNotEmpty()) {
                errors += ImportRowError(
                    row.line,
                    "The exercises of a ${block.protocol.name.lowercase()} go on the rows below it",
                )
            }
            return
        }

        val name = row[Col.EXERCISE]
        if (name.isEmpty()) {
            errors += ImportRowError(row.line, "Missing exercise")
            return
        }
        block.exerciseName = name
        addSets(row, block.sets, errors)
    }

    private fun readExtraSets(row: Row, block: BlockBuilder, errors: MutableList<ImportRowError>) {
        val name = row[Col.EXERCISE]
        if (name.isNotEmpty() && !name.equals(block.exerciseName, ignoreCase = true)) {
            errors += ImportRowError(row.line, "Different exercise in the same block. Add a protocol to start a new block")
            return
        }
        addSets(row, block.sets, errors)
    }

    private fun readChild(row: Row, block: BlockBuilder, errors: MutableList<ImportRowError>) {
        val name = row[Col.EXERCISE]
        if (name.isEmpty()) {
            errors += ImportRowError(row.line, "Missing exercise")
            return
        }
        val sets = mutableListOf<ImportedSet>()
        addSets(row, sets, errors)
        block.children += ImportedExercise(name, sets)
    }

    private fun addSets(row: Row, into: MutableList<ImportedSet>, errors: MutableList<ImportRowError>) {
        val sets = parseSets(row[Col.REPS], row[Col.LOAD])
        if (sets == null) {
            errors += ImportRowError(row.line, "Reps and load have a different number of sets")
        } else {
            into += sets
        }
    }

    private fun BlockBuilder.build(errors: MutableList<ImportRowError>): ImportedBlock? {
        if (protocol.isGroup) {
            if (children.isEmpty()) {
                errors += ImportRowError(line, "A ${protocol.name.lowercase()} needs at least one exercise in the rows below it")
                return null
            }
        } else if (exerciseName == null) {
            return null // already reported
        }

        return ImportedBlock(
            protocol = protocol,
            exerciseName = exerciseName,
            sets = sets.toList(),
            children = children.toList(),
            restSeconds = restSeconds,
            rpe = rpe,
            rounds = rounds,
            restBetweenRoundsSeconds = restBetweenRoundsSeconds,
            holdSeconds = holdSeconds,
            bpm = bpm,
        )
    }

    // Cells
    private fun String.cleanCell(): String {
        val trimmed = trim()
        if (trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"')) {
            return trimmed.substring(1, trimmed.length - 1).replace("\"\"", "\"").trim()
        }
        return trimmed
    }

    private fun parseProtocol(text: String): TrainingProtocol? {
        val key = text.uppercase().filter { it.isLetter() }
        return TrainingProtocol.entries.firstOrNull { protocol -> protocol.name.filter { it.isLetter() } == key }
    }

    /** "5-3-1" and "BW-10-15" become three sets. A single value applies to every set. */
    internal fun parseSets(reps: String, load: String): List<ImportedSet>? {
        val repsParts = splitParts(reps)
        val loadParts = splitParts(load)
        if (repsParts.size > 1 && loadParts.size > 1 && repsParts.size != loadParts.size) return null

        fun pick(parts: List<String>, index: Int): String? = when {
            parts.isEmpty() -> null
            parts.size == 1 -> parts[0].ifEmpty { null }
            else -> parts[index].ifEmpty { null }
        }

        val count = maxOf(repsParts.size, loadParts.size, 1)
        return List(count) { ImportedSet(pick(repsParts, it), pick(loadParts, it)) }
    }

    private fun splitParts(text: String): List<String> =
        if (text.isBlank()) emptyList() else text.split('-').map { it.trim() }

    /** dd/mm/yyyy (or dd.mm.yyyy, dd-mm-yyyy) and yyyy-mm-dd. */
    internal fun parseDate(text: String): LocalDate? {
        val parts = text.split('/', '.', '-').map { it.trim() }
        if (parts.size != 3) return null
        val numbers = parts.map { it.toIntOrNull() ?: return null }

        return runCatching {
            if (parts[0].length == 4) {
                LocalDate(numbers[0], numbers[1], numbers[2])
            } else {
                val year = if (numbers[2] < 100) 2000 + numbers[2] else numbers[2]
                LocalDate(year, numbers[1], numbers[0])
            }
        }.getOrNull()
    }
}
