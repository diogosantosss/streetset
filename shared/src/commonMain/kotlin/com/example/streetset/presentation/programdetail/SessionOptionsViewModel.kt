package com.example.streetset.presentation.programdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.SessionDao
import com.example.streetset.data.entities.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

sealed interface SessionOptionsState {
    data object Closed : SessionOptionsState

    data class Options(val session: Session) : SessionOptionsState

    data class ConfirmDelete(val session: Session) : SessionOptionsState

    data object Deleting : SessionOptionsState
}

class SessionOptionsViewModel : ViewModel(), KoinComponent {
    private val sessionDao: SessionDao by inject()

    private val _state = MutableStateFlow<SessionOptionsState>(SessionOptionsState.Closed)
    val state: StateFlow<SessionOptionsState> = _state.asStateFlow()

    fun showOptions(session: Session) {
        if (_state.value !is SessionOptionsState.Deleting) {
            _state.value = SessionOptionsState.Options(session)
        }
    }

    fun requestDelete() {
        val current = _state.value as? SessionOptionsState.Options ?: return
        _state.value = SessionOptionsState.ConfirmDelete(current.session)
    }

    fun dismiss() {
        if (_state.value !is SessionOptionsState.Deleting) _state.value = SessionOptionsState.Closed
    }

    fun confirmDelete() {
        val current = _state.value as? SessionOptionsState.ConfirmDelete ?: return
        _state.value = SessionOptionsState.Deleting
        viewModelScope.launch {
            delay(2.seconds)
            sessionDao.delete(current.session)
            _state.value = SessionOptionsState.Closed
        }
    }
}
