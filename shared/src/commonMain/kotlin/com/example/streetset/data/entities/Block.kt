package com.example.streetset.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.streetset.presentation.sessiondetail.model.BlockWithSets
import com.example.streetset.presentation.sessiondetail.model.GroupExerciseDraft
import com.example.streetset.presentation.sessiondetail.model.PlannedSetDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingBlockDraft
import com.example.streetset.presentation.sessiondetail.model.TrainingProtocol
import kotlin.collections.orEmpty

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Block::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sessionId"), Index("parentId"), Index("exerciseId")],
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val sessionId: Int,
    val parentId: Int? = null,
    val position: Int = 0,
    val protocol: String,
    val exerciseId: Int? = null,
    val restSeconds: Int = 0,
    val rounds: Int? = null,
    val restBetweenRoundsSeconds: Int = 0,
    val holdSeconds: Int? = null,
    val rpe: Int? = null,
    val bpm: Int? = null,
)

fun Block.toItem(
    children: List<BlockWithSets> = emptyList(),
    exerciseNames: Map<Int, String>,
    setsByBlock: Map<Int, List<PlannedSet>>
) = BlockWithSets(
    block = this,
    exerciseName = exerciseId?.let { exerciseNames[it] },
    sets = id?.let { setsByBlock[it] }.orEmpty().sortedBy { it.setNumber },
    children = children,
)

fun Block.toDraft(
    children: List<Block>,
    exercises: List<Exercise>,
    sets: List<PlannedSet>,
): TrainingBlockDraft {
    val protocol = runCatching { TrainingProtocol.valueOf(protocol) }.getOrDefault(TrainingProtocol.NORMAL)
    if (parentId != null) {
        return TrainingBlockDraft(protocol = TrainingProtocol.NORMAL)
    }

    val exerciseMap = exercises.associateBy { it.id }

    if (!protocol.isGroup) {
        val exerciseName = exerciseId?.let { exerciseMap[it]?.name }.orEmpty()
        val blockSets = sets
            .filter { it.blockId == id }
            .sortedBy { it.setNumber }
            .map { PlannedSetDraft(it.repsText.orEmpty(), it.loadText.orEmpty()) }

        return TrainingBlockDraft(
            protocol = protocol,
            exerciseName = exerciseName,
            sets = blockSets.ifEmpty { listOf(PlannedSetDraft()) },
            restSeconds = if (restSeconds > 0) formatSecondsForDraft(restSeconds) else "",
            rpe = rpe?.toString().orEmpty(),
            bpm = bpm?.toString().orEmpty(),
            holdSeconds = holdSeconds?.takeIf { it > 0 }?.let { formatSecondsForDraft(it) }.orEmpty(),
            rounds = rounds?.toString().orEmpty(),
            restBetweenRoundsSeconds = if (restBetweenRoundsSeconds > 0) formatSecondsForDraft(restBetweenRoundsSeconds) else "",
        )
    }

    val groupExercises = children
        .sortedBy { it.position }
        .map { child ->
            val set = sets.firstOrNull { it.blockId == child.id }
            val name = child.exerciseId?.let { exerciseMap[it]?.name }
                ?: exerciseId?.let { exerciseMap[it]?.name }
                    .orEmpty()

            GroupExerciseDraft(
                name = name,
                reps = set?.repsText.orEmpty(),
                load = set?.loadText.orEmpty(),
            )
        }

    return TrainingBlockDraft(
        protocol = protocol,
        rounds = rounds?.toString().orEmpty(),
        restBetweenRoundsSeconds = if (restBetweenRoundsSeconds > 0) formatSecondsForDraft(restBetweenRoundsSeconds) else "",
        rpe = rpe?.toString().orEmpty(),
        exercises = groupExercises.ifEmpty { listOf(GroupExerciseDraft()) },
    )
}

private fun formatSecondsForDraft(total: Int): String {
    if (total % 60 == 0 && total >= 60) {
        val min = total / 60
        return "${min}min"
    }

    return "${total}s"
}