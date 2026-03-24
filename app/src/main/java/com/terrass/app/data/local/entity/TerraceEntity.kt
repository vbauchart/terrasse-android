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

    // Sun exposure
    val orientation: String? = null,
    @ColumnInfo(name = "sun_exposure") val sunExposure: String? = null,

    // Comfort
    @ColumnInfo(name = "is_covered") val isCovered: Boolean = false,
    @ColumnInfo(name = "is_heated") val isHeated: Boolean = false,
    @ColumnInfo(name = "furniture_type") val furnitureType: String? = null,
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
)
