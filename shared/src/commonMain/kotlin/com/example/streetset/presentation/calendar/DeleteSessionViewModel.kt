package com.example.streetset.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.DaySession
import com.example.streetset.data.dao.SessionDao
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface DeleteSessionState {
    data object Closed : DeleteSessionState

    /** [error] is set when the last delete attempt failed. */
    data class ConfirmDelete(
        val session: DaySession,
        val error: String? = null,
    ) : DeleteSessionState

    data class Deleting(val session: DaySession) : DeleteSessionState
}

class DeleteSessionViewModel : ViewModel(), KoinComponent {
    private val sessionDao: SessionDao by inject()

    private val _state = MutableStateFlow<DeleteSessionState>(DeleteSessionState.Closed)
    val state: StateFlow<DeleteSessionState> = _state.asStateFlow()

    fun requestDelete(session: DaySession) {
        if (_state.value !is DeleteSessionState.Closed) return
        _state.value = DeleteSessionState.ConfirmDelete(session)
    }

    fun dismiss() {
        if (_state.value is DeleteSessionState.Deleting) return
        _state.value = DeleteSessionState.Closed
    }

    fun confirmDelete() {
        val current = _state.value as? DeleteSessionState.ConfirmDelete ?: return
        val session = current.session

        _state.value = DeleteSessionState.Deleting(session)
        viewModelScope.launch {
            try {
                sessionDao.deleteById(session.id)
                _state.value = DeleteSessionState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to delete session", e, TAG)
                _state.value = DeleteSessionState.ConfirmDelete(session, error = "Could not delete session")
            }
        }
    }

    companion object {
        private const val TAG = "DeleteSessionViewModel"
    }
}