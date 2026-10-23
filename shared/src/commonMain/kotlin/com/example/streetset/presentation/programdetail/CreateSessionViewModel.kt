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

data class SessionInput(
    val weekday: Int = 1,
    val name: String = "",
    val type: String = "",
) {
    val isValid: Boolean get() = weekday in 1..7 && name.isNotBlank() && type.isNotBlank()
}

sealed interface CreateSessionState {
    data object Closed : CreateSessionState

    data class Form(
        val weekId: Int,
        val input: SessionInput = SessionInput(),
        val error: String? = null
    ) : CreateSessionState

    data object Submitting : CreateSessionState
}

class CreateSessionViewModel : ViewModel(), KoinComponent {
    private val sessionDao: SessionDao by inject()

    private val _state = MutableStateFlow<CreateSessionState>(CreateSessionState.Closed)
    val state: StateFlow<CreateSessionState> = _state.asStateFlow()

    fun showForm(weekId: Int) {
        _state.value = CreateSessionState.Form(weekId)
    }

    fun updateInput(input: SessionInput) {
        val current = _state.value as? CreateSessionState.Form ?: return
        _state.value = current.copy(input = input, error = null)
    }

    fun dismiss() {
        if (_state.value !is CreateSessionState.Submitting) _state.value = CreateSessionState.Closed
    }

    fun submit() {
        val current = _state.value as? CreateSessionState.Form ?: return
        if (!current.input.isValid) return
        _state.value = CreateSessionState.Submitting
        viewModelScope.launch {
            delay(2.seconds)
            try {
                sessionDao.insert(
                    Session(
                        weekId = current.weekId,
                        weekday = current.input.weekday,
                        name = current.input.name.trim(),
                        type = current.input.type.trim(),
                    ),
                )
                _state.value = CreateSessionState.Closed
            } catch (exception: Exception) {
                _state.value = current.copy(error = exception.message ?: "Unable to create session")
            }
        }
    }
}
