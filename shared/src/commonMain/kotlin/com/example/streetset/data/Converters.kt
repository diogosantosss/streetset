package com.example.streetset.data

import androidx.room3.ColumnTypeConverter
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
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

    @ColumnTypeConverter
    fun fromTrainingProtocol(value: String): TrainingProtocol =
        runCatching { TrainingProtocol.valueOf(value) }.getOrDefault(TrainingProtocol.NORMAL)

    @ColumnTypeConverter
    fun toTrainingProtocol(value: TrainingProtocol): String = value.name
}