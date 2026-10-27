package com.example.streetset.presentation.sessiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.data.entities.toDraft
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.GroupExerciseDraft
import com.example.streetset.presentation.sessiondetail.model.PlannedSetDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingBlockDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import com.example.streetset.presentation.sessiondetail.model.isValid
import com.example.streetset.presentation.sessiondetail.model.toBlock
import com.example.streetset.presentation.sessiondetail.model.toChildBlock
import com.example.streetset.presentation.sessiondetail.model.toPlannedSet
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

sealed interface AddBlockState {
    /** Editor collapsed, showing only the "Add block" button. */
    data object Closed : AddBlockState

    /** Editor open. [error] is set when the last save failed. */
    data class Editing(
        val draft: TrainingBlockDraft = TrainingBlockDraft(),
        val error: String? = null,
        val editingBlockId: Int? = null,
    ) : AddBlockState

    data class Submitting(
        val draft: TrainingBlockDraft,
        val editingBlockId: Int? = null,
    ) : AddBlockState
}

val AddBlockState.editingBlockId: Int?
    get() = when (this) {
        AddBlockState.Closed -> null
        is AddBlockState.Editing -> editingBlockId
        is AddBlockState.Submitting -> editingBlockId
    }

class AddBlockViewModel(
    private val sessionId: Int,
) : ViewModel(), KoinComponent {

    private val blockDao: BlockDao by inject()
    private val plannedSetDao: PlannedSetDao by inject()
    private val exerciseDao: ExerciseDao by inject()

    private val _state = MutableStateFlow<AddBlockState>(AddBlockState.Closed)
    val state: StateFlow<AddBlockState> = _state.asStateFlow()

    fun open() {
        if (_state.value !is AddBlockState.Closed) return
        _state.value = AddBlockState.Editing()
    }

    /** Cancel. Discards the draft. */
    fun close() {
        if (_state.value is AddBlockState.Submitting) return
        _state.value = AddBlockState.Closed
    }

    fun updateDraft(draft: TrainingBlockDraft) {
        if (_state.value !is AddBlockState.Editing) return
        val editingId = _state.value.editingBlockId
        // Editing after a failure clears the error
        _state.value = AddBlockState.Editing(draft, editingBlockId = editingId)
    }

    fun save() {
        val current = _state.value as? AddBlockState.Editing ?: return
        val draft = current.draft
        if (!draft.isValid) return

        _state.value = AddBlockState.Submitting(draft, editingBlockId = current.editingBlockId)
        viewModelScope.launch {
            try {
                if (current.editingBlockId != null) {
                    update(draft, current.editingBlockId)
                } else {
                    persist(draft)
                }
                _state.value = AddBlockState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to save block", e, TAG)
                _state.value = AddBlockState.Editing(draft, error = "Could not save block. Try again.", editingBlockId = current.editingBlockId)
            }
        }
    }

    fun startEdit(selected: Block, allBlocks: List<BlockWithSets>) {
        val item = allBlocks.find { it.block.id == selected.id }
            ?: allBlocks.flatMap { listOf(it) + it.children }.find { it.block.id == selected.id }
            ?: return

        val childrenBlocks = item.children.map { it.block }
        val allSets = (listOf(item) + item.children).flatMap { it.sets }
        val exerciseNames = (listOf(item) + item.children).mapNotNull { bwi ->
            bwi.exerciseName?.let { name -> bwi.block.exerciseId?.let { id -> id to Exercise(id = id, name = name) } }
        }.toMap().values.toList()

        editBlock(item.block, childrenBlocks, exerciseNames, allSets)
    }

    fun editBlock(block: Block, children: List<Block>, exercises: List<Exercise>, sets: List<PlannedSet>) {
        if (_state.value is AddBlockState.Submitting) return
        _state.value = AddBlockState.Editing(block.toDraft(children, exercises, sets), editingBlockId = block.id)
    }

    private suspend fun persist(draft: TrainingBlockDraft) {
        val position = blockDao.nextPosition(sessionId)
        val exerciseId = if (draft.protocol.isGroup) null else resolveExerciseId(draft.exerciseName)
        val block = draft.toBlock(sessionId, position, exerciseId)
        val blockId = blockDao.insert(block).toInt()

        try {
            saveContent(blockId, draft)
        } catch (e: Exception) {
            withContext(NonCancellable) { blockDao.delete(block.copy(id = blockId)) }
            throw e
        }
    }

    private suspend fun update(draft: TrainingBlockDraft, blockId: Int) {
        val existing = blockDao.getById(blockId) ?: throw IllegalStateException("Block not found")
        val exerciseId = if (draft.protocol.isGroup) null else resolveExerciseId(draft.exerciseName)
        val updated = draft.toBlock(sessionId, existing.position, exerciseId).copy(id = blockId, parentId = existing.parentId)
        blockDao.update(updated)

        if (existing.parentId != null) {
            plannedSetDao.deleteByBlock(blockId)
            plannedSetDao.insert(
                PlannedSet(blockId = blockId, setNumber = 1, repsText = null, loadText = null)
            )
            return
        }

        plannedSetDao.deleteByBlock(blockId)
        blockDao.deleteChildren(blockId)

        try {
            saveContent(blockId, draft)
        } catch (e: Exception) {
            withContext(NonCancellable) { blockDao.delete(existing.copy(id = blockId)) }
            throw e
        }
    }

    private suspend fun saveContent(blockId: Int, draft: TrainingBlockDraft) {
        if (draft.protocol.isGroup) {
            draft.exercises.forEachIndexed { index, exercise ->
                val childId = blockDao.insert(
                    exercise.toChildBlock(
                        sessionId = sessionId,
                        parentId = blockId,
                        position = index,
                        exerciseId = resolveExerciseId(exercise.name),
                    ),
                ).toInt()
                plannedSetDao.insert(exercise.toPlannedSet(childId))
            }
        } else {
            draft.sets.forEachIndexed { index, set ->
                plannedSetDao.insert(set.toPlannedSet(blockId, setNumber = index + 1))
            }
        }
    }

    /** Reuses an exercise with the same name (ignoring case) or creates it. */
    private suspend fun resolveExerciseId(name: String): Int {
        val clean = name.trim()
        exerciseDao.getByName(clean)?.id?.let { return it }
        return exerciseDao.insert(Exercise(name = clean)).toInt()
    }

    companion object {
        private const val TAG = "AddBlockViewModel"
    }
}