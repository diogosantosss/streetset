package com.example.streetset.presentation.sessiondetail.model

import com.example.streetset.data.entities.Block
import com.example.streetset.data.entities.PlannedSet

data class BlockWithSets(
    val block: Block,
    val exerciseName: String? = null,
    val sets: List<PlannedSet> = emptyList(),
    val children: List<BlockWithSets> = emptyList(),
)