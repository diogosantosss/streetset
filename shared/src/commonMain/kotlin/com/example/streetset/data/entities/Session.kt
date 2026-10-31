package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "sessions", indices = [Index("date")])
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val date: LocalDate,
    val position: Int = 0,
    val name: String,
    val notes: String? = null,
)