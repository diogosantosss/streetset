package com.example.streetset.presentation.programs

import com.example.streetset.data.entities.Program
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn


data class ProgramInput(
    val name: String = "",
    val goal: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && endDate >= startDate

    companion object {
        fun initial(
            today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        ): ProgramInput = ProgramInput(
            startDate = today,
            endDate = today,
        )

        fun fromProgram(program: Program): ProgramInput = ProgramInput(
            name = program.name,
            goal = program.goal ?: "",
            startDate = program.startDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
            endDate = program.endDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }
}