package com.example.streetset.data

import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.SessionDao

class DatabaseSeeder(
    private val sessionDao: SessionDao,
    private val blockDao: BlockDao,
    private val exerciseDao: ExerciseDao,
) {
    companion object {}
}
