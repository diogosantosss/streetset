package com.example.streetset.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.PlannedSetDao
import com.example.streetset.data.dao.SessionDao
import com.example.streetset.data.dao.SessionLogDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.data.entities.Session
import com.example.streetset.data.entities.SessionLog

@Database(
    entities = [
        Exercise::class,
        Session::class,
        Block::class,
        PlannedSet::class,
        SessionLog::class,
    ],
    version = 6,
    exportSchema = false,
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(StreetSetDatabaseConstructor::class)
abstract class StreetSetDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun blockDao(): BlockDao
    abstract fun plannedSetDao(): PlannedSetDao
    abstract fun sessionLogDao(): SessionLogDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object StreetSetDatabaseConstructor : RoomDatabaseConstructor<StreetSetDatabase> {
    override fun initialize(): StreetSetDatabase
}
