package com.terrass.app.data.remote.dto

import org.json.JSONObject

data class TerraceDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val sunTimes: String?,
    val isCovered: Boolean,
    val isHeated: Boolean,
    val size: String?,
    val roadProximity: String?,
    val noiseLevel: String?,
    val viewQuality: String?,
    val hasVegetation: Boolean,
    val serviceQuality: String?,
    val priceRange: String?,
    val cuisineType: String?,
    val status: String,
    val deviceId: String,
    val thumbsUp: Int,
    val thumbsDown: Int,
) {
    companion object {
        fun fromJson(obj: JSONObject) = TerraceDto(
            id = obj.getString("id"),
            name = obj.getString("name"),
            latitude = obj.getDouble("latitude"),
            longitude = obj.getDouble("longitude"),
            address = obj.optString("address").ifBlank { null },
            sunTimes = obj.optString("sun_times").ifBlank { null },
            isCovered = obj.optBoolean("is_covered"),
            isHeated = obj.optBoolean("is_heated"),
            size = obj.optString("size").ifBlank { null },
            roadProximity = obj.optString("road_proximity").ifBlank { null },
            noiseLevel = obj.optString("noise_level").ifBlank { null },
            viewQuality = obj.optString("view_quality").ifBlank { null },
            hasVegetation = obj.optBoolean("has_vegetation"),
            serviceQuality = obj.optString("service_quality").ifBlank { null },
            priceRange = obj.optString("price_range").ifBlank { null },
            cuisineType = obj.optString("cuisine_type").ifBlank { null },
            status = obj.optString("status").ifBlank { "active" },
            deviceId = obj.optString("device_id"),
            thumbsUp = obj.optInt("thumbs_up"),
            thumbsDown = obj.optInt("thumbs_down"),
        )
    }
}
