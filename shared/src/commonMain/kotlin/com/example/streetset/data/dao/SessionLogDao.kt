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


    data class ProtocolSets(val protocol: String, val sets: Int)

    // Planned sets per protocol, for the sessions that were completed. The exercises of a circuit
    // or superset count under the protocol of their parent block, once per round.
    @Query(
        """
        SELECT COALESCE(parent.protocol, blocks.protocol) AS protocol,
               SUM(COALESCE(parent.rounds, 1)) AS sets
        FROM planned_sets
        INNER JOIN blocks ON planned_sets.blockId = blocks.id
        LEFT JOIN blocks AS parent ON blocks.parentId = parent.id
        WHERE blocks.sessionId IN (SELECT DISTINCT sessionId FROM session_logs WHERE completedAt >= :since)
        GROUP BY COALESCE(parent.protocol, blocks.protocol)
        """
    )
    fun getProtocolSets(since: LocalDateTime): Flow<List<ProtocolSets>>


    data class ExerciseSets(val name: String, val sets: Int)

    @Query(
        """
        SELECT exercises.name AS name,
               SUM(COALESCE(parent.rounds, 1)) AS sets
        FROM planned_sets
        INNER JOIN blocks ON planned_sets.blockId = blocks.id
        INNER JOIN exercises ON blocks.exerciseId = exercises.id
        LEFT JOIN blocks AS parent ON blocks.parentId = parent.id
        WHERE blocks.sessionId IN (SELECT DISTINCT sessionId FROM session_logs WHERE completedAt >= :since)
        GROUP BY exercises.id
        ORDER BY sets DESC, exercises.name
        LIMIT 5
        """
    )
    fun getTopExercises(since: LocalDateTime): Flow<List<ExerciseSets>>

    @Insert
    suspend fun insert(log: SessionLog): Long

    @Update
    suspend fun update(log: SessionLog)

    @Delete
    suspend fun delete(log: SessionLog)
}
