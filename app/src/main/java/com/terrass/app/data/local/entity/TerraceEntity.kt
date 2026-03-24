package com.terrass.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terraces")
data class TerraceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,

    // Sun exposure (comma-separated: "morning,noon,evening")
    @ColumnInfo(name = "sun_times") val sunTimes: String? = null,

    // Comfort
    @ColumnInfo(name = "is_covered") val isCovered: Boolean = false,
    @ColumnInfo(name = "is_heated") val isHeated: Boolean = false,
    val size: String? = null,

    // Environment
    @ColumnInfo(name = "road_proximity") val roadProximity: String? = null,
    @ColumnInfo(name = "noise_level") val noiseLevel: String? = null,
    @ColumnInfo(name = "view_quality") val viewQuality: String? = null,
    @ColumnInfo(name = "has_vegetation") val hasVegetation: Boolean = false,

    // Service
    @ColumnInfo(name = "service_quality") val serviceQuality: String? = null,
    @ColumnInfo(name = "price_range") val priceRange: String? = null,
    @ColumnInfo(name = "cuisine_type") val cuisineType: String? = null,

    // Meta
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "active",

    // Sync
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    @ColumnInfo(name = "synced") val synced: Boolean = false,
    @ColumnInfo(name = "thumbs_up") val thumbsUp: Int = 0,
    @ColumnInfo(name = "thumbs_down") val thumbsDown: Int = 0,
)
