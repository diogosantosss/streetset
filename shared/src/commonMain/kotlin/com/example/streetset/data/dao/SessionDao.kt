package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

data class DaySummary(val date: LocalDate, val total: Int, val completed: Int)

data class DaySession(
    val id: Int,
    val name: String,
    val blockCount: Int,
    val completed: Boolean,
    val durationSeconds: Int?,
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Int): Session?

    @Query(
        """
        SELECT sessions.id AS id,
               sessions.name AS name,
               COUNT(DISTINCT blocks.id) AS blockCount,
               CASE WHEN COUNT(session_logs.id) > 0 THEN 1 ELSE 0 END AS completed,
               MAX(session_logs.durationSeconds) AS durationSeconds
        FROM sessions
        LEFT JOIN blocks ON blocks.sessionId = sessions.id AND blocks.parentId IS NULL
        LEFT JOIN session_logs ON session_logs.sessionId = sessions.id
        WHERE sessions.date = :date
        GROUP BY sessions.id
        ORDER BY sessions.position
        """
    )
    fun getDaySessions(date: LocalDate): Flow<List<DaySession>>

    @Query(
        """
        SELECT sessions.date AS date,
               COUNT(DISTINCT sessions.id) AS total,
               COUNT(DISTINCT session_logs.sessionId) AS completed
        FROM sessions
        LEFT JOIN session_logs ON session_logs.sessionId = sessions.id
        WHERE sessions.date BETWEEN :start AND :end
        GROUP BY sessions.date
        """
    )
    fun getDaySummaries(start: LocalDate, end: LocalDate): Flow<List<DaySummary>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM sessions WHERE date = :date")
    suspend fun nextPosition(date: LocalDate): Int

    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)
}
