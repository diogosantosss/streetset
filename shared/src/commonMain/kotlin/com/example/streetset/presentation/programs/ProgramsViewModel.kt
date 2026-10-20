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
        val sheet: Sheet = Sheet.None,
    ) : ProgramsState

    sealed interface Sheet {
        data object None : Sheet

        data class Options(val program: Program) : Sheet

        data class ConfirmDelete(val program: Program) : Sheet

        data class Form(
            val program: Program?,
            val error: String? = null
        ) : Sheet
    }
}

class ProgramsViewModel : ViewModel(), KoinComponent {

    private val programDao: ProgramDao by inject()

    private val _state = MutableStateFlow<ProgramsState>(ProgramsState.Loading)
    val state: StateFlow<ProgramsState> = _state.asStateFlow()

    private val sheet: ProgramsState.Sheet
        get() = (_state.value as? ProgramsState.Success)?.sheet ?: ProgramsState.Sheet.None

    init {
        loadPrograms()
    }

    private fun loadPrograms() {
        viewModelScope.launch {
            delay(4.seconds)
            programDao.getAll()
                .catch { throwable ->
                    Napier.e("Failed to load programs", throwable)
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

    private fun updateSheet(transform: (ProgramsState.Sheet) -> ProgramsState.Sheet) {
        _state.update { curr ->
            if (curr is ProgramsState.Success) curr.copy(sheet = transform(curr.sheet)) else curr
        }
    }

    fun selectProgram(program: Program) =
        updateSheet { ProgramsState.Sheet.Options(program) }

    fun openCreateForm() =
        updateSheet { ProgramsState.Sheet.Form(program = null) }

    fun openEditForm(program: Program) =
        updateSheet { ProgramsState.Sheet.Form(program = program) }

    fun requestDelete(program: Program) =
        updateSheet { ProgramsState.Sheet.ConfirmDelete(program) }

    fun dismissSheet() =
        updateSheet { ProgramsState.Sheet.None }

    fun confirmDelete() {
        val program = (sheet as? ProgramsState.Sheet.ConfirmDelete)?.program ?: return
        viewModelScope.launch {
            delay(2.seconds)
            programDao.delete(program)
            dismissSheet()
        }
    }

    fun submitForm(input: ProgramInput) {
        val form = sheet as? ProgramsState.Sheet.Form ?: return
        if (!input.isValid) return

        viewModelScope.launch {
            val program = Program(
                id = form.program?.id,
                name = input.name.trim(),
                goal = input.goal.trim().ifBlank { null },
                startDate = input.startDate,
                endDate = input.endDate,
                weeksCount = (input.startDate.daysUntil(input.endDate) / 7).coerceAtLeast(1),
            )

            val existing = programDao.getByName(program.name)
            if (existing != null && existing.id != program.id) {
                setFormError("A program with this name already exists")
                return@launch
            }

            if (form.program == null) programDao.insert(program)
            else programDao.update(program)

            dismissSheet()
        }
    }

    fun clearFormError() = setFormError(null)

    private fun setFormError(error: String?) {
        updateSheet { if (it is ProgramsState.Sheet.Form) it.copy(error = error) else it }
    }

    companion object {
        private const val TAG = "ProgramsViewModel"
    }
}