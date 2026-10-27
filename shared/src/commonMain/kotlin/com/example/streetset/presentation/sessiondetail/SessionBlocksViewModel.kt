package com.example.streetset.presentation.sessiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface SessionBlocksState {
    data object Loading : SessionBlocksState

    data class Error(val error: String) : SessionBlocksState

    data class Success(val blocks: List<BlockWithSets>) : SessionBlocksState
}

class SessionBlocksViewModel(
    private val sessionId: Int,
) : ViewModel(), KoinComponent {

    private val blockDao by inject<BlockDao>()
    private val plannedSetDao: PlannedSetDao by inject()
    private val exerciseDao: ExerciseDao by inject()

    private val _state = MutableStateFlow<SessionBlocksState>(SessionBlocksState.Loading)
    val state: StateFlow<SessionBlocksState> = _state.asStateFlow()

    private var loadingJob: Job? = null

    init { loadBlocks() }

    fun retry() {
        _state.value = SessionBlocksState.Loading
        loadBlocks()
    }

    private fun loadBlocks() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            combine(
                blockDao.getBySession(sessionId),
                plannedSetDao.getBySession(sessionId),
                exerciseDao.getAll(),
            ) { blocks, exercises, plannedSets ->
                SessionBlocksState.Success(buildBlocks(blocks, exercises, plannedSets))
            }
                .catch { throwable ->
                    Napier.e("Failed to load blocks", throwable, TAG)
                    _state.value = SessionBlocksState.Error("Loading block failed")
                }
                .collect { state -> _state.value = state }
        }
    }

    private fun buildBlocks(
        blocks: List<Block>,
        sets: List<PlannedSet>,
        exercises: List<Exercise>,
    ): List<BlockWithSets> {
        val exerciseNames = exercises
            .mapNotNull { exercise -> exercise.id?.let { it to exercise.name } }
            .toMap()
        val setsByBlock = sets.groupBy { it.blockId }
        val childrenByParent = blocks.filter { it.parentId != null }.groupBy { it.parentId }

        fun Block.toItem(children: List<BlockWithSets> = emptyList()) = BlockWithSets(
            block = this,
            exerciseName = exerciseId?.let { exerciseNames[it] },
            sets = id?.let { setsByBlock[it] }.orEmpty().sortedBy { it.setNumber },
            children = children,
        )

        return blocks
            .filter { it.parentId == null }
            .sortedBy { it.position }
            .map { parent ->
                val children = parent.id
                    ?.let { childrenByParent[it] }
                    .orEmpty()
                    .sortedBy { it.position }
                    .map(Block::toItem)
                parent.toItem(children)
            }
    }

    companion object {
        private const val TAG = "SessionBlocksViewModel"
    }
}
