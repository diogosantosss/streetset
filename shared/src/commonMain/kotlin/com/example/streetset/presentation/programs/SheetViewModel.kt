package com.example.streetset.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.ProgramDao
import com.example.streetset.data.entities.Program
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.daysUntil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

sealed interface SheetState {
    data object None : SheetState

    data class Options(val program: Program) : SheetState

    data class ConfirmDelete(val program: Program) : SheetState

    data class Form(
        val program: Program?,
        val error: String? = null,
    ) : SheetState

    data class Deleting(val program: Program) : SheetState

    data class Submitting(val program: Program?) : SheetState
}

class SheetViewModel : ViewModel(), KoinComponent {

    private val programDao: ProgramDao by inject()

    private val _sheetState = MutableStateFlow<SheetState>(SheetState.None)
    val sheetState: StateFlow<SheetState> = _sheetState.asStateFlow()

    fun showOptions(program: Program) {
        val current = _sheetState.value
        if (current is SheetState.Deleting || current is SheetState.Submitting) return
        _sheetState.value = SheetState.Options(program)
    }

    fun showCreateForm() {
        val current = _sheetState.value
        if (current is SheetState.Deleting || current is SheetState.Submitting) return
        _sheetState.value = SheetState.Form(program = null)
    }

    fun showEditForm(program: Program) {
        val current = _sheetState.value
        if (current is SheetState.Deleting || current is SheetState.Submitting) return
        _sheetState.value = SheetState.Form(program = program)
    }

    fun requestDelete(program: Program) {
        val current = _sheetState.value
        if (current is SheetState.Deleting || current is SheetState.Submitting) return
        _sheetState.value = SheetState.ConfirmDelete(program)
    }

    fun dismiss() {
        val current = _sheetState.value
        if (current is SheetState.Deleting || current is SheetState.Submitting) return
        _sheetState.value = SheetState.None
    }

    fun confirmDelete() {
        val current = _sheetState.value as? SheetState.ConfirmDelete ?: return
        val program = current.program
        _sheetState.value = SheetState.Deleting(program)
        viewModelScope.launch {
            delay(4.seconds)
            programDao.delete(program)
            _sheetState.value = SheetState.None
        }
    }

    fun submitForm(input: ProgramInput) {
        val current = _sheetState.value as? SheetState.Form ?: return
        if (!input.isValid) return
        val currentProgram = current.program
        _sheetState.value = SheetState.Submitting(currentProgram)
        viewModelScope.launch {
            val program = Program(
                id = currentProgram?.id,
                name = input.name.trim(),
                goal = input.goal.trim().ifBlank { null },
                startDate = input.startDate,
                endDate = input.endDate,
                weeksCount = (input.startDate.daysUntil(input.endDate) / 7).coerceAtLeast(1),
            )

            val existing = programDao.getByName(program.name)
            if (existing != null && existing.id != program.id) {
                _sheetState.value = SheetState.Form(
                    program = currentProgram,
                    error = "A program with this name already exists",
                )
                return@launch
            }

            delay(2.seconds)
            if (currentProgram == null) {
                programDao.insert(program)
            } else {
                programDao.update(program)
            }
            _sheetState.value = SheetState.None
        }
    }

    fun clearFormError() {
        val current = _sheetState.value
        if (current is SheetState.Form) {
            _sheetState.value = current.copy(error = null)
        }
    }
}
