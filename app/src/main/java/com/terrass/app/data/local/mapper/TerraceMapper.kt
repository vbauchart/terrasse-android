package com.terrass.app.data.local.mapper

import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.remote.dto.TerraceDto
import com.terrass.app.domain.model.Comfort
import com.terrass.app.domain.model.Environment
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.RoadProximity
import com.terrass.app.domain.model.Service
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.SunExposure
import com.terrass.app.domain.model.SunTime
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.TerraceStatus
import com.terrass.app.domain.model.ViewQuality

fun TerraceEntity.toDomain(): Terrace = Terrace(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address,
    sunExposure = SunExposure(
        sunTimes = sunTimes
            ?.split(",")
            ?.mapNotNull { SunTime.fromValue(it.trim()) }
            ?.toSet()
            ?: emptySet(),
    ),
    comfort = Comfort(
        isCovered = isCovered,
        isHeated = isHeated,
        size = TerraceSize.fromValue(size),
    ),
    environment = Environment(
        roadProximity = RoadProximity.fromValue(roadProximity),
        noiseLevel = NoiseLevel.fromValue(noiseLevel),
        viewQuality = ViewQuality.fromValue(viewQuality),
        hasVegetation = hasVegetation,
    ),
    service = Service(
        quality = ServiceQuality.fromValue(serviceQuality),
        priceRange = PriceRange.fromValue(priceRange),
        cuisineType = cuisineType,
    ),
    thumbsUp = thumbsUp,
    thumbsDown = thumbsDown,
    createdAt = createdAt,
    status = TerraceStatus.fromValue(status),
)

fun Terrace.toEntity(): TerraceEntity = TerraceEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address,
    sunTimes = sunExposure.sunTimes.takeIf { it.isNotEmpty() }
        ?.joinToString(",") { it.value },
    isCovered = comfort.isCovered,
    isHeated = comfort.isHeated,
    size = comfort.size?.value,
    roadProximity = environment.roadProximity?.value,
    noiseLevel = environment.noiseLevel?.value,
    viewQuality = environment.viewQuality?.value,
    hasVegetation = environment.hasVegetation,
    serviceQuality = service.quality?.value,
    priceRange = service.priceRange?.value,
    cuisineType = service.cuisineType,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis(),
    status = status.value,
    thumbsUp = thumbsUp,
    thumbsDown = thumbsDown,
)

fun TerraceDto.toEntity(): TerraceEntity = TerraceEntity(
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address,
    sunTimes = sunTimes,
    isCovered = isCovered,
    isHeated = isHeated,
    size = size,
    roadProximity = roadProximity,
    noiseLevel = noiseLevel,
    viewQuality = viewQuality,
    hasVegetation = hasVegetation,
    serviceQuality = serviceQuality,
    priceRange = priceRange,
    cuisineType = cuisineType,
    status = status,
    remoteId = id,
    synced = true,
    thumbsUp = thumbsUp,
    thumbsDown = thumbsDown,
)
