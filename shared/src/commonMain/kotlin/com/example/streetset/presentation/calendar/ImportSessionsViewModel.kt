package com.example.streetset.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.dao.SessionDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.data.entities.Session
import com.example.streetset.presentation.calendar.importer.ImportParseResult
import com.example.streetset.presentation.calendar.importer.ImportedBlock
import com.example.streetset.presentation.calendar.importer.ImportedSession
import com.example.streetset.presentation.calendar.importer.ImportedSet
import com.example.streetset.presentation.calendar.importer.SessionImportParser
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import io.github.aakira.napier.Napier
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface ImportSessionsState {
    data object Closed : ImportSessionsState

    /** [error] is set when the last import failed. */
    data class Editing(
        val text: String = "",
        val parsed: ImportParseResult = ImportParseResult.Empty,
        val error: String? = null,
    ) : ImportSessionsState

    data class Importing(val text: String, val parsed: ImportParseResult) : ImportSessionsState

    data class Done(val added: Int, val replaced: Int) : ImportSessionsState
}

class ImportSessionsViewModel : ViewModel(), KoinComponent {

    private val sessionDao: SessionDao by inject()
    private val blockDao: BlockDao by inject()
    private val plannedSetDao: PlannedSetDao by inject()
    private val exerciseDao: ExerciseDao by inject()

    private val _state = MutableStateFlow<ImportSessionsState>(ImportSessionsState.Closed)
    val state: StateFlow<ImportSessionsState> = _state.asStateFlow()

    fun open() {
        if (_state.value !is ImportSessionsState.Closed) return
        _state.value = ImportSessionsState.Editing()
    }

    fun close() {
        if (_state.value is ImportSessionsState.Importing) return
        _state.value = ImportSessionsState.Closed
    }

    fun updateText(text: String) {
        if (_state.value !is ImportSessionsState.Editing) return
        // Editing after a failure clears the error
        _state.value = ImportSessionsState.Editing(text, SessionImportParser.parse(text))
    }

    fun import() {
        val current = _state.value as? ImportSessionsState.Editing ?: return
        val parsed = current.parsed
        if (!parsed.isValid) return

        _state.value = ImportSessionsState.Importing(current.text, parsed)
        viewModelScope.launch {
            var added = 0
            var replaced = 0
            var failedAt: ImportedSession? = null

            try {
                val exerciseIds = mutableMapOf<String, Int>()
                for (session in parsed.sessions) {
                    failedAt = session
                    if (importSession(session, exerciseIds)) replaced++ else added++
                }
                _state.value = ImportSessionsState.Done(added, replaced)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to import sessions", e, TAG)
                val where = failedAt?.let { "\"${it.name.ifBlank { "Session" }}\"" } ?: "the sessions"
                val before = (added + replaced).takeIf { it > 0 }
                    ?.let { " $it sessions before it were imported, and importing again replaces them." }
                    .orEmpty()
                _state.value = ImportSessionsState.Editing(
                    text = current.text,
                    parsed = parsed,
                    error = "Import stopped at $where.$before",
                )
            }
        }
    }

    /** Returns true when it replaced an existing session, false when it created a new one. */
    private suspend fun importSession(session: ImportedSession, exerciseIds: MutableMap<String, Int>): Boolean {
        val position = sessionDao.nextPosition(session.date)
        val name = session.name.ifBlank { "Session ${position + 1}" }
        val existingId = sessionDao.findIdByDateAndName(session.date, name)

        if (existingId != null) {
            replaceBlocks(existingId, session, exerciseIds)
            return true
        }

        val sessionId = sessionDao.insert(Session(date = session.date, position = position, name = name)).toInt()
        try {
            session.blocks.forEachIndexed { index, block ->
                insertBlock(sessionId, index, block, exerciseIds, created = mutableListOf())
            }
        } catch (e: Exception) {
            // Undo the partial session. Blocks and sets go with it (ON DELETE CASCADE).
            withContext(NonCancellable) { sessionDao.deleteById(sessionId) }
            throw e
        }
        return false
    }

    /** The session row stays (id, position, notes and workout history). Only its plan is replaced. */
    private suspend fun replaceBlocks(
        sessionId: Int,
        session: ImportedSession,
        exerciseIds: MutableMap<String, Int>,
    ) {
        val oldIds = blockDao.getTopLevelIds(sessionId)
        val created = mutableListOf<Int>()

        try {
            session.blocks.forEachIndexed { index, block ->
                insertBlock(sessionId, index, block, exerciseIds, created)
            }
        } catch (e: Exception) {
            // Keep the old plan: remove what was inserted so far
            withContext(NonCancellable) { blockDao.deleteByIds(created) }
            throw e
        }
        blockDao.deleteByIds(oldIds)
    }

    private suspend fun insertBlock(
        sessionId: Int,
        position: Int,
        block: ImportedBlock,
        exerciseIds: MutableMap<String, Int>,
        created: MutableList<Int>,
    ) {
        val blockId = blockDao.insert(
            Block(
                sessionId = sessionId,
                position = position,
                protocol = block.protocol,
                exerciseId = block.exerciseName?.let { exerciseId(it, exerciseIds) },
                restSeconds = block.restSeconds,
                rounds = block.rounds,
                restBetweenRoundsSeconds = block.restBetweenRoundsSeconds,
                holdSeconds = block.holdSeconds,
                rpe = block.rpe,
                bpm = block.bpm,
            ),
        ).toInt()
        // Registered right away, so a failure while inserting its children still removes it
        created += blockId

        if (block.protocol.isGroup) {
            block.children.forEachIndexed { index, child ->
                val childId = blockDao.insert(
                    Block(
                        sessionId = sessionId,
                        parentId = blockId,
                        position = index,
                        protocol = TrainingProtocol.NORMAL,
                        exerciseId = exerciseId(child.name, exerciseIds),
                    ),
                ).toInt()
                insertSets(childId, child.sets)
            }
        } else {
            insertSets(blockId, block.sets)
        }
    }

    private suspend fun insertSets(blockId: Int, sets: List<ImportedSet>) {
        sets.forEachIndexed { index, set ->
            plannedSetDao.insert(
                PlannedSet(blockId = blockId, setNumber = index + 1, repsText = set.reps, loadText = set.load),
            )
        }
    }

    /** Reuses an exercise with the same name (ignoring case) or creates it. */
    private suspend fun exerciseId(name: String, cache: MutableMap<String, Int>): Int {
        val clean = name.trim()
        val key = clean.lowercase()
        cache[key]?.let { return it }

        val id = exerciseDao.getByName(clean)?.id ?: exerciseDao.insert(Exercise(name = clean)).toInt()
        cache[key] = id
        return id
    }

    companion object {
        private const val TAG = "ImportSessionsViewModel"
    }
}
