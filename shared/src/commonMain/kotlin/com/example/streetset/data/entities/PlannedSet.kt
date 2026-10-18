package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "planned_sets",
    foreignKeys = [
        ForeignKey(
            entity = Block::class,
            parentColumns = ["id"],
            childColumns = ["blockId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("blockId")],
)
data class PlannedSet(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val blockId: Int,
    val setNumber: Int,
    val repsText: String? = null,
    val loadText: String? = null,
    val minutes: Int? = null,
)
