package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "weeks",
    foreignKeys = [
        ForeignKey(
            entity = Program::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("programId")],
)
data class Week(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val programId: Int,
    val number: Int,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
