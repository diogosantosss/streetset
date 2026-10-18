package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Week::class,
            parentColumns = ["id"],
            childColumns = ["weekId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("weekId")],
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val weekId: Int,
    val weekday: Int? = null,
    val name: String,
    val type: String? = null,
)
