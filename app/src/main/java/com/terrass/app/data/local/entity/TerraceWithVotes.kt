package com.terrass.app.data.local.entity

import androidx.room.Embedded

data class TerraceWithVotes(
    @Embedded val terrace: TerraceEntity,
    val thumbsUp: Int,
    val thumbsDown: Int,
)
