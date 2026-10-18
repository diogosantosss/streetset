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
    companion object {
        private val programs: List<Program> =
            listOf(
                Program(null, "Program 1", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 4),
                Program(null, "Program 2", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 2),
                Program(null, "Program 3", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 1),
                Program(null, "Program 4", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 3),
                Program(null, "Program 5", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 1),
                Program(null, "Program 6", "Build strength and endurance", LocalDate(2021, 10, 20), LocalDate(2021, 10, 22), 2),
            )
    }

    suspend fun seedIfEmpty() {
        if (programDao.getAll().first().isNotEmpty()) return

        programs.forEach { program ->
            programDao.insert(program)
        }

        val benchId = exerciseDao.insert(
            Exercise(name = "Bench Press", muscleGroup = "Chest", equipment = "Barbell")
        ).toInt()
        val squatId = exerciseDao.insert(
            Exercise(name = "Squat", muscleGroup = "Legs", equipment = "Barbell")
        ).toInt()
        val deadliftId = exerciseDao.insert(
            Exercise(name = "Deadlift", muscleGroup = "Back", equipment = "Barbell")
        ).toInt()

        val programId = programDao.insert(
            Program(
                name = "Strength Base",
                goal = "Build foundational strength",
                startDate = LocalDate(2026, 10, 20),
                weeksCount = 4,
            )
        ).toInt()

        val week1Id = weekDao.insert(Week(programId = programId, number = 1)).toInt()
        val week2Id = weekDao.insert(Week(programId = programId, number = 2)).toInt()

        val pushId = sessionDao.insert(
            Session(weekId = week1Id, weekday = 1, name = "Push Day", type = "Strength")
        ).toInt()
        val pullId = sessionDao.insert(
            Session(weekId = week1Id, weekday = 3, name = "Pull Day", type = "Strength")
        ).toInt()

        blockDao.insert(
            Block(sessionId = pushId, position = 0, protocol = "Straight", exerciseId = benchId, restSeconds = 90)
        )
        blockDao.insert(
            Block(sessionId = pullId, position = 0, protocol = "Straight", exerciseId = deadliftId, restSeconds = 120)
        )
        blockDao.insert(
            Block(sessionId = pullId, position = 1, protocol = "Straight", exerciseId = squatId, restSeconds = 120)
        )
    }
}
