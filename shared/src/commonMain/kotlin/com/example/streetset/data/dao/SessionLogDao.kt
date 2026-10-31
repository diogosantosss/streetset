package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.SessionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs WHERE sessionId = :sessionId ORDER BY completedAt DESC")
    fun getBySession(sessionId: Int): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs ORDER BY completedAt DESC")
    fun getAll(): Flow<List<SessionLog>>

    @Insert
    suspend fun insert(log: SessionLog): Long

    @Update
    suspend fun update(log: SessionLog)

    @Delete
    suspend fun delete(log: SessionLog)
}
