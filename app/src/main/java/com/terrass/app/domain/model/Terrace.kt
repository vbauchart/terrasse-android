package com.terrass.app.domain.model

data class Terrace(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val sunExposure: SunExposure = SunExposure(),
    val comfort: Comfort = Comfort(),
    val environment: Environment = Environment(),
    val service: Service = Service(),
    val thumbsUp: Int = 0,
    val thumbsDown: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val status: TerraceStatus = TerraceStatus.ACTIVE,
) {
    val votePercentage: Int
        get() {
            val total = thumbsUp + thumbsDown
            return if (total == 0) -1 else (thumbsUp * 100) / total
        }

    val totalVotes: Int get() = thumbsUp + thumbsDown
}

data class SunExposure(
    val orientation: Orientation? = null,
    val exposure: ExposureType? = null,
)

data class Comfort(
    val isCovered: Boolean = false,
    val isHeated: Boolean = false,
    val furnitureType: FurnitureType? = null,
    val size: TerraceSize? = null,
)

data class Environment(
    val roadProximity: RoadProximity? = null,
    val noiseLevel: NoiseLevel? = null,
    val viewQuality: ViewQuality? = null,
    val hasVegetation: Boolean = false,
)

data class Service(
    val quality: ServiceQuality? = null,
    val priceRange: PriceRange? = null,
    val cuisineType: String? = null,
)
