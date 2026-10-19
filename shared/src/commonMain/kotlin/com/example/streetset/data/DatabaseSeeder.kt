package com.example.streetset.data

import com.example.streetset.data.dao.BlockDao
import com.example.streetset.data.dao.ExerciseDao
import com.example.streetset.data.dao.ProgramDao
import com.example.streetset.data.dao.SessionDao
import com.example.streetset.data.dao.WeekDao
import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.Program
import com.example.streetset.data.entities.Session
import com.example.streetset.data.entities.Week
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class DatabaseSeeder(
    private val programDao: ProgramDao,
    private val weekDao: WeekDao,
    private val sessionDao: SessionDao,
    private val blockDao: BlockDao,
    private val exerciseDao: ExerciseDao,
) {
    companion object {}
}
