package com.example.streetset.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.SessionLogDao
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface HistoryOptionsState {
    data object Closed : HistoryOptionsState

    data class Options(val entry: SessionLogDao.HistoryEntry) : HistoryOptionsState

    data class Editing(
        val entry: SessionLogDao.HistoryEntry,
        val notes: String,
        val error: String? = null,
    ) : HistoryOptionsState

    data class ConfirmDelete(
        val entry: SessionLogDao.HistoryEntry,
        val error: String? = null,
    ) : HistoryOptionsState

    data class Deleting(val entry: SessionLogDao.HistoryEntry) : HistoryOptionsState
}

class HistoryOptionsViewModel : ViewModel(), KoinComponent {
    private val sessionLogDao: SessionLogDao by inject()

    private val _state = MutableStateFlow<HistoryOptionsState>(HistoryOptionsState.Closed)
    val state: StateFlow<HistoryOptionsState> = _state.asStateFlow()

    fun showOptions(entry: SessionLogDao.HistoryEntry) {
        if (_state.value is HistoryOptionsState.Deleting) return
        _state.value = HistoryOptionsState.Options(entry)
    }

    fun requestEdit() {
        val current = _state.value as? HistoryOptionsState.Options ?: return
        _state.value = HistoryOptionsState.Editing(current.entry, current.entry.notes.orEmpty())
    }

    fun onNotesChange(notes: String) {
        val current = _state.value as? HistoryOptionsState.Editing ?: return
        _state.value = current.copy(notes = notes, error = null)
    }

    fun saveNotes() {
        val current = _state.value as? HistoryOptionsState.Editing ?: return
        viewModelScope.launch {
            try {
                sessionLogDao.updateNotes(current.entry.logId, current.notes.ifBlank { null })
                _state.value = HistoryOptionsState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to update session log notes", e, TAG)
                _state.value = current.copy(error = "Could not save notes")
            }
        }
    }

    fun requestDelete() {
        val current = _state.value as? HistoryOptionsState.Options ?: return
        _state.value = HistoryOptionsState.ConfirmDelete(current.entry)
    }

    fun confirmDelete() {
        val current = _state.value as? HistoryOptionsState.ConfirmDelete ?: return
        val entry = current.entry

        _state.value = HistoryOptionsState.Deleting(entry)
        viewModelScope.launch {
            try {
                sessionLogDao.deleteById(entry.logId)
                _state.value = HistoryOptionsState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to delete session log", e, TAG)
                _state.value = HistoryOptionsState.ConfirmDelete(entry, error = "Could not delete workout")
            }
        }
    }

    fun dismiss() {
        if (_state.value is HistoryOptionsState.Deleting) return
        _state.value = HistoryOptionsState.Closed
    }

    companion object {
        private const val TAG = "HistoryOptionsViewModel"
    }
}
