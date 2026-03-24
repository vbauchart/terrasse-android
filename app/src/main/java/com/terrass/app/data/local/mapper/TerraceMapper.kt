package com.terrass.app.data.local.mapper

import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.local.entity.TerraceWithVotes
import com.terrass.app.domain.model.Comfort
import com.terrass.app.domain.model.Environment
import com.terrass.app.domain.model.ExposureType
import com.terrass.app.domain.model.FurnitureType
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.Orientation
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.RoadProximity
import com.terrass.app.domain.model.Service
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.SunExposure
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.TerraceStatus
import com.terrass.app.domain.model.ViewQuality

fun TerraceWithVotes.toDomain(): Terrace = Terrace(
    id = terrace.id,
    name = terrace.name,
    latitude = terrace.latitude,
    longitude = terrace.longitude,
    address = terrace.address,
    sunExposure = SunExposure(
        orientation = Orientation.fromValue(terrace.orientation),
        exposure = ExposureType.fromValue(terrace.sunExposure),
    ),
    comfort = Comfort(
        isCovered = terrace.isCovered,
        isHeated = terrace.isHeated,
        furnitureType = FurnitureType.fromValue(terrace.furnitureType),
        size = TerraceSize.fromValue(terrace.size),
    ),
    environment = Environment(
        roadProximity = RoadProximity.fromValue(terrace.roadProximity),
        noiseLevel = NoiseLevel.fromValue(terrace.noiseLevel),
        viewQuality = ViewQuality.fromValue(terrace.viewQuality),
        hasVegetation = terrace.hasVegetation,
    ),
    service = Service(
        quality = ServiceQuality.fromValue(terrace.serviceQuality),
        priceRange = PriceRange.fromValue(terrace.priceRange),
        cuisineType = terrace.cuisineType,
    ),
    thumbsUp = thumbsUp,
    thumbsDown = thumbsDown,
    createdAt = terrace.createdAt,
    status = TerraceStatus.fromValue(terrace.status),
)

fun Terrace.toEntity(): TerraceEntity = TerraceEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address,
    orientation = sunExposure.orientation?.value,
    sunExposure = sunExposure.exposure?.value,
    isCovered = comfort.isCovered,
    isHeated = comfort.isHeated,
    furnitureType = comfort.furnitureType?.value,
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
)
