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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TerraceMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = TerraceEntity(
            id = 1, name = "Café Soleil", latitude = 43.6, longitude = 1.4,
            address = "1 rue Test",
            orientation = "south", sunExposure = "full_sun",
            isCovered = true, isHeated = false, furnitureType = "chairs", size = "medium",
            roadProximity = "low", noiseLevel = "quiet", viewQuality = "good", hasVegetation = true,
            serviceQuality = "excellent", priceRange = "moderate", cuisineType = "français",
            createdAt = 1000L, status = "active",
        )
        val withVotes = TerraceWithVotes(entity, thumbsUp = 8, thumbsDown = 2)

        val terrace = withVotes.toDomain()

        assertEquals(1, terrace.id)
        assertEquals("Café Soleil", terrace.name)
        assertEquals(43.6, terrace.latitude)
        assertEquals(1.4, terrace.longitude)
        assertEquals("1 rue Test", terrace.address)
        assertEquals(Orientation.SOUTH, terrace.sunExposure.orientation)
        assertEquals(ExposureType.FULL_SUN, terrace.sunExposure.exposure)
        assertEquals(true, terrace.comfort.isCovered)
        assertEquals(FurnitureType.CHAIRS, terrace.comfort.furnitureType)
        assertEquals(TerraceSize.MEDIUM, terrace.comfort.size)
        assertEquals(RoadProximity.LOW, terrace.environment.roadProximity)
        assertEquals(NoiseLevel.QUIET, terrace.environment.noiseLevel)
        assertEquals(ViewQuality.GOOD, terrace.environment.viewQuality)
        assertEquals(true, terrace.environment.hasVegetation)
        assertEquals(ServiceQuality.EXCELLENT, terrace.service.quality)
        assertEquals(PriceRange.MODERATE, terrace.service.priceRange)
        assertEquals("français", terrace.service.cuisineType)
        assertEquals(8, terrace.thumbsUp)
        assertEquals(2, terrace.thumbsDown)
        assertEquals(TerraceStatus.ACTIVE, terrace.status)
    }

    @Test
    fun `toDomain handles null optional fields`() {
        val entity = TerraceEntity(id = 2, name = "Bar", latitude = 48.0, longitude = 2.0)
        val withVotes = TerraceWithVotes(entity, thumbsUp = 0, thumbsDown = 0)

        val terrace = withVotes.toDomain()

        assertNull(terrace.sunExposure.orientation)
        assertNull(terrace.sunExposure.exposure)
        assertNull(terrace.comfort.furnitureType)
        assertNull(terrace.environment.roadProximity)
        assertNull(terrace.service.quality)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val terrace = Terrace(
            id = 5, name = "Test", latitude = 43.0, longitude = 1.0,
            sunExposure = SunExposure(Orientation.EAST, ExposureType.SHADE),
            comfort = Comfort(isCovered = true, isHeated = true, furnitureType = FurnitureType.LOUNGE, size = TerraceSize.LARGE),
            environment = Environment(RoadProximity.NONE, NoiseLevel.QUIET, ViewQuality.EXCEPTIONAL, true),
            service = Service(ServiceQuality.GOOD, PriceRange.CHEAP, "italien"),
        )

        val entity = terrace.toEntity()

        assertEquals(5, entity.id)
        assertEquals("Test", entity.name)
        assertEquals("east", entity.orientation)
        assertEquals("shade", entity.sunExposure)
        assertEquals(true, entity.isCovered)
        assertEquals(true, entity.isHeated)
        assertEquals("lounge", entity.furnitureType)
        assertEquals("large", entity.size)
        assertEquals("none", entity.roadProximity)
        assertEquals("quiet", entity.noiseLevel)
        assertEquals("exceptional", entity.viewQuality)
        assertEquals(true, entity.hasVegetation)
        assertEquals("good", entity.serviceQuality)
        assertEquals("cheap", entity.priceRange)
        assertEquals("italien", entity.cuisineType)
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = TerraceEntity(
            id = 3, name = "Roundtrip", latitude = 44.0, longitude = 3.0,
            orientation = "northwest", sunExposure = "partial",
            isCovered = false, isHeated = true, furnitureType = "mixed", size = "small",
            roadProximity = "high", noiseLevel = "noisy", viewQuality = "none", hasVegetation = false,
            serviceQuality = "poor", priceRange = "expensive", cuisineType = "bar",
            createdAt = 5000L, status = "active",
        )
        val withVotes = TerraceWithVotes(original, 0, 0)
        val domain = withVotes.toDomain()
        val backToEntity = domain.toEntity()

        assertEquals(original.id, backToEntity.id)
        assertEquals(original.name, backToEntity.name)
        assertEquals(original.orientation, backToEntity.orientation)
        assertEquals(original.sunExposure, backToEntity.sunExposure)
        assertEquals(original.isCovered, backToEntity.isCovered)
        assertEquals(original.furnitureType, backToEntity.furnitureType)
        assertEquals(original.roadProximity, backToEntity.roadProximity)
        assertEquals(original.noiseLevel, backToEntity.noiseLevel)
        assertEquals(original.serviceQuality, backToEntity.serviceQuality)
    }
}
