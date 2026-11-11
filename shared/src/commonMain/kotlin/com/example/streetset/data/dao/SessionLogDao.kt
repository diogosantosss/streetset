package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.SessionLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs WHERE sessionId = :sessionId ORDER BY completedAt DESC")
    fun getBySession(sessionId: Int): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs ORDER BY completedAt DESC")
    fun getAll(): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs WHERE completedAt >= :since ORDER BY completedAt")
    fun getSince(since: LocalDateTime): Flow<List<SessionLog>>

    /** A completed workout, joined with the name of the session it belongs to. */
    data class HistoryEntry(
        val logId: Int,
        val sessionName: String,
        val completedAt: LocalDateTime,
        val durationSeconds: Int,
        val notes: String?,
    )

    @Query(
        """
        SELECT session_logs.id AS logId,
               sessions.name AS sessionName,
               session_logs.completedAt AS completedAt,
               session_logs.durationSeconds AS durationSeconds,
               session_logs.notes AS notes
        FROM session_logs
        INNER JOIN sessions ON session_logs.sessionId = sessions.id
        ORDER BY session_logs.completedAt DESC
        """
    )
    fun getHistory(): Flow<List<HistoryEntry>>

    @Insert
    suspend fun insert(log: SessionLog): Long

    @Update
    suspend fun update(log: SessionLog)

    @Delete
    suspend fun delete(log: SessionLog)

    @Query("UPDATE session_logs SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Int, notes: String?)

    @Query("DELETE FROM session_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}
