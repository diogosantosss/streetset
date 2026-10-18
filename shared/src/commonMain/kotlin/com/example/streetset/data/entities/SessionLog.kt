package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "session_logs",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val sessionId: Int,
    val completedAt: LocalDateTime,
    val durationSeconds: Int,
    val notes: String? = null,
)
