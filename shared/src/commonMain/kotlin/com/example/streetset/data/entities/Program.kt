package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Entity(tableName = "programs")
data class Program(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val goal: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val weeksCount: Int? = null,
) {
    fun formatDateRange(): String = "${startDate.short()} - ${endDate.short()}"

    private fun LocalDate?.short(): String =
        this?.let { "${it.day} ${it.month.short()}" } ?: "—"

    private fun Month.short(): String =
        name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}

fun Program.currentWeek(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): Int {
    val start = startDate ?: return 0
    val total = weeksCount ?: return 0
    val elapsed = start.daysUntil(today)
    if (elapsed < 0) return 0
    return (elapsed / 7 + 1).coerceAtMost(total)
}
