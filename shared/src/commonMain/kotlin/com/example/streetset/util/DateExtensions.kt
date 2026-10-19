package com.example.streetset.util

import kotlinx.datetime.LocalDate

fun LocalDate.shortMonthName(): String =
    month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

fun LocalDate.shortDisplay(): String = "$day ${shortMonthName()}"

fun LocalDate.fullDisplay(): String = "$day ${shortMonthName()} $year"
