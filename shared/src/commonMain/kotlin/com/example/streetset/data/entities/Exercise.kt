package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val muscleGroup: String? = null,
    val equipment: String? = null,
    val notes: String? = null,
)
