package com.example.streetset.presentation.calendar

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year, month, 1)

fun Month.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

fun DayOfWeek.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

fun LocalDate.monthTitle(): String = "${month.displayName()} $year"

fun LocalDate.fullTitle(): String = "${dayOfWeek.displayName()}, $day ${month.displayName()}"