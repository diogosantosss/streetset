package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Block::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sessionId"), Index("parentId"), Index("exerciseId")],
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val sessionId: Int,
    val parentId: Int? = null,
    val position: Int = 0,
    val protocol: String,
    val exerciseId: Int? = null,
    val restSeconds: Int = 0,
    val rounds: Int? = null,
    val restBetweenRoundsSeconds: Int = 0,
    val holdSeconds: Int? = null,
    val rpe: Int? = null,
    val bpm: Int? = null,
)
