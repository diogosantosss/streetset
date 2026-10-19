package com.example.streetset.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.ProgramDao
import com.example.streetset.data.entities.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.daysUntil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface ProgramsState {
    data object Idle : ProgramsState

    data object Loading : ProgramsState

    data class Success(
        val programs: List<Program> = emptyList(),
    ) : ProgramsState

    data class Error(
        val error: String,
    ) : ProgramsState
}

class ProgramsViewModel : ViewModel(), KoinComponent {

    private val programDao: ProgramDao by inject()

    private val _state = MutableStateFlow<ProgramsState>(ProgramsState.Idle)
    val state: StateFlow<ProgramsState> = _state.asStateFlow()

    init {
        loadPrograms()
    }

    private fun loadPrograms() {
        viewModelScope.launch {
            _state.value = ProgramsState.Loading
            programDao.getAll()
                .catch { throwable ->
                    _state.value = ProgramsState.Error(
                        throwable.message ?: "Something went wrong",
                    )
                }
                .collect { programs ->
                    _state.value = ProgramsState.Success(programs)
                }
        }
    }

    fun createProgram(input: ProgramInput) {
        if (!input.isValid) return

        viewModelScope.launch {
            val weeksCount = (input.startDate.daysUntil(input.endDate) / 7).coerceAtLeast(1)
            programDao.insert(
                Program(
                    name = input.name.trim(),
                    goal = input.goal.trim().ifBlank { null },
                    startDate = input.startDate,
                    endDate = input.endDate,
                    weeksCount = weeksCount,
                ),
            )
        }
    }
}