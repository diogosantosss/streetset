package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY startDate DESC")
    fun getAll(): Flow<List<Program>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: Int): Program?

    @Insert
    suspend fun insert(program: Program): Long

    @Update
    suspend fun update(program: Program)

    @Delete
    suspend fun delete(program: Program)
}
