package com.example.streetset.presentation.programdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.ProgramDao
import com.example.streetset.data.dao.WeekDao
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.Week
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface ProgramDetailState {
    data object Loading : ProgramDetailState
    data class Error(val message: String) : ProgramDetailState
    data class Success(
        val program: Program,
        val weeks: List<Week>,
    ) : ProgramDetailState
}

class ProgramDetailViewModel(
    private val programId: Int,
) : ViewModel(), KoinComponent {

    private val programDao: ProgramDao by inject()
    private val weekDao: WeekDao by inject()

    private val _state = MutableStateFlow<ProgramDetailState>(ProgramDetailState.Loading)
    val state: StateFlow<ProgramDetailState> = _state.asStateFlow()

    private var lastSyncedProgramHash: String? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val program = programDao.getById(programId)
            if (program == null) {
                _state.value = ProgramDetailState.Error("Program not found")
                return@launch
            }

            weekDao.getByProgram(programId).collectLatest { weeks ->
                val hash = "${program.startDate},${program.endDate},${program.weeksCount}"
                if (lastSyncedProgramHash != hash) {
                    lastSyncedProgramHash = hash
                    syncWeeks(program, weeks)
                    return@collectLatest
                }

                val currentProgram = programDao.getById(programId) ?: program
                _state.value = ProgramDetailState.Success(
                    program = currentProgram,
                    weeks = weeks.sortedBy { it.number },
                )
            }
        }
    }

    private suspend fun syncWeeks(program: Program, existingWeeks: List<Week>) {
        val start = program.startDate ?: return
        val end = program.endDate ?: return
        val totalDays = start.daysUntil(end) + 1
        val weeksCount = if (totalDays <= 0) 1 else ((totalDays - 1) / 7 + 1).coerceAtLeast(1)

        for (index in 0 until weeksCount) {
            val weekNumber = index + 1
            val weekStart = start.plus(index * 7, DateTimeUnit.DAY)
            val weekEndCalc = weekStart.plus(6, DateTimeUnit.DAY)
            val weekEnd = if (weekEndCalc > end) end else weekEndCalc
            val existing = existingWeeks.find { it.number == weekNumber }
            if (existing == null) {
                weekDao.insert(
                    Week(
                        programId = programId,
                        number = weekNumber,
                        startDate = weekStart,
                        endDate = weekEnd,
                    ),
                )
            } else {
                weekDao.update(
                    existing.copy(
                        startDate = weekStart,
                        endDate = weekEnd,
                    ),
                )
            }
        }
    }
}
