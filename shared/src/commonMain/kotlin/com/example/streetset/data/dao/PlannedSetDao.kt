package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.PlannedSet
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedSetDao {
    @Query("SELECT * FROM planned_sets WHERE blockId = :blockId ORDER BY setNumber")
    fun getByBlock(blockId: Int): Flow<List<PlannedSet>>

    @Query("SELECT * FROM planned_sets WHERE id = :id")
    suspend fun getById(id: Int): PlannedSet?

    @Insert
    suspend fun insert(plannedSet: PlannedSet): Long

    @Update
    suspend fun update(plannedSet: PlannedSet)

    @Delete
    suspend fun delete(plannedSet: PlannedSet)
}
