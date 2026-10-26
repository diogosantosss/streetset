package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.Session
import kotlinx.coroutines.flow.Flow

data class ProgramSessionProgress(
    val programId: Int,
    val totalSessions: Int,
    val completedSessions: Int,
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE weekId = :weekId ORDER BY weekday")
    fun getByWeek(weekId: Int): Flow<List<Session>>

    @Query("SELECT sessions.* FROM sessions INNER JOIN weeks ON sessions.weekId = weeks.id WHERE weeks.programId = :programId ORDER BY weeks.number, sessions.weekday")
    fun getByProgram(programId: Int): Flow<List<Session>>

    @Query("""
        SELECT programs.id AS programId,
               COUNT(DISTINCT sessions.id) AS totalSessions,
               COUNT(DISTINCT CASE WHEN session_logs.id IS NOT NULL THEN sessions.id END) AS completedSessions
        FROM programs
        LEFT JOIN weeks ON weeks.programId = programs.id
        LEFT JOIN sessions ON sessions.weekId = weeks.id
        LEFT JOIN session_logs ON session_logs.sessionId = sessions.id
        GROUP BY programs.id
    """)
    fun getProgramProgress(): Flow<List<ProgramSessionProgress>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Int): Session?

    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)
}
