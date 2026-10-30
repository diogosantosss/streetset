package com.example.streetset.presentation.sessiondetail.model

import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.Exercise
import com.example.streetset.data.entities.PlannedSet
import com.example.streetset.data.entities.toItem

data class BlockWithSets(
    val block: Block,
    val exerciseName: String? = null,
    val sets: List<PlannedSet> = emptyList(),
    val children: List<BlockWithSets> = emptyList(),
)

fun buildBlocksWithSets(
    blocks: List<Block>,
    sets: List<PlannedSet>,
    exercises: List<Exercise>,
): List<BlockWithSets> {
    val exerciseNames = exercises.associate { it.id!! to it.name }
    val setsByBlock = sets.groupBy { it.blockId }
    val childrenByParent = blocks.groupBy { it.parentId }

    fun toBlockWithSets(block: Block): BlockWithSets {
        val blockId = block.id
        val children = childrenByParent[blockId]
            .orEmpty()
            .sortedBy { it.position }
            .map { toBlockWithSets(it) }
        return block.toItem(children, exerciseNames, setsByBlock)
    }

    return blocks
        .filter { it.parentId == null }
        .sortedBy { it.position }
        .map { toBlockWithSets(it) }
}