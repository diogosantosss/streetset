package com.example.streetset.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.SessionDao
import com.example.streetset.data.entities.Session
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface CreateSessionState {
    data object Closed : CreateSessionState

    /** [error] is set when the last attempt failed. */
    data class Form(
        val date: LocalDate,
        val name: String = "",
        val error: String? = null,
    ) : CreateSessionState

    data class Creating(val date: LocalDate, val name: String) : CreateSessionState
}

class CreateSessionCalendarViewModel : ViewModel(), KoinComponent {
    private val sessionDao: SessionDao by inject()

    private val _state = MutableStateFlow<CreateSessionState>(CreateSessionState.Closed)
    val state: StateFlow<CreateSessionState> = _state.asStateFlow()

    fun open(date: LocalDate) {
        if (_state.value !is CreateSessionState.Closed) return
        _state.value = CreateSessionState.Form(date)
    }

    fun updateName(name: String) {
        val current = _state.value as? CreateSessionState.Form ?: return
        _state.value = current.copy(name = name, error = null)
    }

    fun dismiss() {
        if (_state.value is CreateSessionState.Creating) return
        _state.value = CreateSessionState.Closed
    }

    fun create() {
        val current = _state.value as? CreateSessionState.Form ?: return
        val date = current.date

        _state.value = CreateSessionState.Creating(date, current.name)
        viewModelScope.launch {
            try {
                val position = sessionDao.nextPosition(date)
                val name = current.name.trim().ifBlank { "Session ${position + 1}" }
                sessionDao.insert(Session(date = date, position = position, name = name))
                _state.value = CreateSessionState.Closed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to create session", e, TAG)
                _state.value = CreateSessionState.Form(
                    date = date,
                    name = current.name,
                    error = "Could not create session. Try again.",
                )
            }
        }
    }

    companion object {
        private const val TAG = "CreateSessionViewModel"
    }
}