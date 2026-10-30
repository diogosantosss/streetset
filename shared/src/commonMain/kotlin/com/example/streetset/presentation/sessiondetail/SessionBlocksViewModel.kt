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
import com.example.streetset.presentation.sessiondetail.model.buildBlocksWithSets
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
                SessionBlocksState.Success(buildBlocksWithSets(blocks, exercises, plannedSets))
            }
                .catch { throwable ->
                    Napier.e("Failed to load blocks", throwable, TAG)
                    _state.value = SessionBlocksState.Error("Loading block failed")
                }
                .collect { state -> _state.value = state }
        }
    }

    companion object {
        private const val TAG = "SessionBlocksViewModel"
    }
}
