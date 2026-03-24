package com.terrass.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "votes",
    foreignKeys = [ForeignKey(
        entity = TerraceEntity::class,
        parentColumns = ["id"],
        childColumns = ["terrace_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("terrace_id"), Index(value = ["terrace_id", "device_id"], unique = true)],
)
data class VoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "terrace_id") val terraceId: Long,
    @ColumnInfo(name = "is_positive") val isPositive: Boolean,
    @ColumnInfo(name = "device_id") val deviceId: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    @ColumnInfo(name = "synced") val synced: Boolean = false,
)
