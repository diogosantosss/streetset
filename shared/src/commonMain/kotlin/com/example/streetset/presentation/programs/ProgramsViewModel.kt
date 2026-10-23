package com.example.streetset.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.ProgramDao
import com.example.streetset.data.entities.Program
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.daysUntil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

sealed interface ProgramsState {
    data object Loading : ProgramsState

    data class Error(val error: String) : ProgramsState

    data class Success(
        val programs: List<Program>,
    ) : ProgramsState
}

class ProgramsViewModel : ViewModel(), KoinComponent {

    private val programDao: ProgramDao by inject()

    private val _state = MutableStateFlow<ProgramsState>(ProgramsState.Loading)
    val state: StateFlow<ProgramsState> = _state.asStateFlow()

    init {
        loadPrograms()
    }

    private fun loadPrograms() {
        viewModelScope.launch {
            // simulate network response delay
            delay(2.seconds)

            programDao.getAll()
                .catch { throwable ->
                    Napier.e("Failed to load programs", throwable, TAG)
                    _state.value = ProgramsState.Error("Loading programs failed")
                }
                .collect { programs ->
                    _state.update { curr ->
                        if (curr is ProgramsState.Success) {
                            curr.copy(programs = programs)
                        } else {
                            ProgramsState.Success(programs = programs)
                        }
                    }
                }
        }
    }

    companion object {
        private const val TAG = "ProgramsViewModel"
    }
}