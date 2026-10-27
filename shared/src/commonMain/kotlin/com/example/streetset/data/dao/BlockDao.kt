package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.Block
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId ORDER BY position")
    fun getBySession(sessionId: Int): Flow<List<Block>>

    @Query("SELECT * FROM blocks WHERE id = :id")
    suspend fun getById(id: Int): Block?

    @Query("SELECT * FROM blocks WHERE parentId = :parentId ORDER BY position")
    fun getByParent(parentId: Int): Flow<List<Block>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM blocks WHERE sessionId = :sessionId AND parentId IS NULL")
    suspend fun nextPosition(sessionId: Int): Int

    @Insert
    suspend fun insert(block: Block): Long

    @Update
    suspend fun update(block: Block)

    @Delete
    suspend fun delete(block: Block)

    @Query("DELETE FROM blocks WHERE parentId = :parentId")
    suspend fun deleteChildren(parentId: Int)
}
