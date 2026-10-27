package com.example.streetset.presentation.sessiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.entities.Block
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface BlockOptionsState {
    data object Closed : BlockOptionsState

    data class Options(val block: Block) : BlockOptionsState

    data class ConfirmDelete(
        val block: Block,
        val error: String? = null,
    ) : BlockOptionsState

    data class Deleting(val block: Block) : BlockOptionsState
}

class BlockOptionsViewModel : ViewModel(), KoinComponent {
    private val blockDao: BlockDao by inject()

    private val _state = MutableStateFlow<BlockOptionsState>(BlockOptionsState.Closed)
    val state: StateFlow<BlockOptionsState> = _state.asStateFlow()

    fun showOptions(block: Block) {
        if (_state.value is BlockOptionsState.Deleting) return
        _state.value = BlockOptionsState.Options(block)
    }

    fun requestEdit() {
        val current = _state.value as? BlockOptionsState.Options ?: return
        _state.value = BlockOptionsState.Closed
        // handled by caller
    }

    fun requestDelete() {
        val current = _state.value as? BlockOptionsState.Options ?: return
        _state.value = BlockOptionsState.ConfirmDelete(current.block)
    }

    fun dismiss() {
        if (_state.value is BlockOptionsState.Deleting) return
        _state.value = BlockOptionsState.Closed
    }

    fun confirmDelete() {
        val current = _state.value as? BlockOptionsState.ConfirmDelete ?: return
        val block = current.block

        _state.value = BlockOptionsState.Deleting(block)
        viewModelScope.launch {
            try {
                blockDao.delete(block)
                _state.value = BlockOptionsState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to delete block", e, TAG)
                _state.value = BlockOptionsState.ConfirmDelete(block, error = "Could not delete block")
            }
        }
    }

    companion object {
        private const val TAG = "BlockOptionsViewModel"
    }
}
