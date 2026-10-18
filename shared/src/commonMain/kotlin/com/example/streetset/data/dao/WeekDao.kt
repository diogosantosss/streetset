package com.example.streetset.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.example.streetset.data.entities.Week
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekDao {
    @Query("SELECT * FROM weeks WHERE programId = :programId ORDER BY number")
    fun getByProgram(programId: Int): Flow<List<Week>>

    @Query("SELECT * FROM weeks WHERE id = :id")
    suspend fun getById(id: Int): Week?

    @Insert
    suspend fun insert(week: Week): Long

    @Update
    suspend fun update(week: Week)

    @Delete
    suspend fun delete(week: Week)
}
