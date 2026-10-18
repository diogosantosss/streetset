package com.example.streetset.data

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class Converters {
    @ColumnTypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @ColumnTypeConverter
    fun toLocalDate(value: LocalDate?): String? = value?.toString()

    @ColumnTypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @ColumnTypeConverter
    fun toLocalDateTime(value: LocalDateTime?): String? = value?.toString()
}