package com.terrass.app.data.local.mapper

import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.local.entity.TerraceWithVotes
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TerraceMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = TerraceEntity(
            id = 1, name = "Café Soleil", latitude = 43.6, longitude = 1.4,
            address = "1 rue Test",
            sunTimes = "morning,noon",
            isCovered = true, isHeated = false, size = "medium",
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
        assertEquals(setOf(SunTime.MORNING, SunTime.NOON), terrace.sunExposure.sunTimes)
        assertEquals(true, terrace.comfort.isCovered)
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

        assertTrue(terrace.sunExposure.sunTimes.isEmpty())
        assertNull(terrace.comfort.size)
        assertNull(terrace.environment.roadProximity)
        assertNull(terrace.service.quality)
    }

    @Test
    fun `toDomain parses all three sun times`() {
        val entity = TerraceEntity(
            id = 3, name = "Soleil", latitude = 43.0, longitude = 1.0,
            sunTimes = "morning,noon,evening",
        )
        val terrace = TerraceWithVotes(entity, 0, 0).toDomain()

        assertEquals(setOf(SunTime.MORNING, SunTime.NOON, SunTime.EVENING), terrace.sunExposure.sunTimes)
    }

    @Test
    fun `toDomain ignores unknown sun time values`() {
        val entity = TerraceEntity(
            id = 4, name = "Test", latitude = 43.0, longitude = 1.0,
            sunTimes = "morning,unknown_value",
        )
        val terrace = TerraceWithVotes(entity, 0, 0).toDomain()

        assertEquals(setOf(SunTime.MORNING), terrace.sunExposure.sunTimes)
    }

    @Test
    fun `toEntity maps all fields correctly`() {
        val terrace = Terrace(
            id = 5, name = "Test", latitude = 43.0, longitude = 1.0,
            sunExposure = SunExposure(setOf(SunTime.MORNING, SunTime.EVENING)),
            comfort = Comfort(isCovered = true, isHeated = true, size = TerraceSize.LARGE),
            environment = Environment(RoadProximity.NONE, NoiseLevel.QUIET, ViewQuality.EXCEPTIONAL, true),
            service = Service(ServiceQuality.GOOD, PriceRange.CHEAP, "italien"),
        )

        val entity = terrace.toEntity()

        assertEquals(5, entity.id)
        assertEquals("Test", entity.name)
        assertEquals(true, entity.isCovered)
        assertEquals(true, entity.isHeated)
        assertEquals("large", entity.size)
        assertEquals("none", entity.roadProximity)
        assertEquals("quiet", entity.noiseLevel)
        assertEquals("exceptional", entity.viewQuality)
        assertEquals(true, entity.hasVegetation)
        assertEquals("good", entity.serviceQuality)
        assertEquals("cheap", entity.priceRange)
        assertEquals("italien", entity.cuisineType)
        // sun_times order may vary, check both values are present
        val times = entity.sunTimes?.split(",")?.toSet()
        assertEquals(setOf("morning", "evening"), times)
    }

    @Test
    fun `toEntity stores null for empty sun times`() {
        val terrace = Terrace(
            id = 6, name = "Ombre", latitude = 43.0, longitude = 1.0,
            sunExposure = SunExposure(emptySet()),
            comfort = Comfort(),
            environment = Environment(),
            service = Service(),
        )

        assertNull(terrace.toEntity().sunTimes)
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = TerraceEntity(
            id = 3, name = "Roundtrip", latitude = 44.0, longitude = 3.0,
            sunTimes = "morning,noon",
            isCovered = false, isHeated = true, size = "small",
            roadProximity = "high", noiseLevel = "noisy", viewQuality = "none", hasVegetation = false,
            serviceQuality = "poor", priceRange = "expensive", cuisineType = "bar",
            createdAt = 5000L, status = "active",
        )
        val withVotes = TerraceWithVotes(original, 0, 0)
        val domain = withVotes.toDomain()
        val backToEntity = domain.toEntity()

        assertEquals(original.id, backToEntity.id)
        assertEquals(original.name, backToEntity.name)
        assertEquals(original.isCovered, backToEntity.isCovered)
        assertEquals(original.roadProximity, backToEntity.roadProximity)
        assertEquals(original.noiseLevel, backToEntity.noiseLevel)
        assertEquals(original.serviceQuality, backToEntity.serviceQuality)
        // sun times roundtrip (order may vary)
        assertEquals(
            original.sunTimes?.split(",")?.toSet(),
            backToEntity.sunTimes?.split(",")?.toSet(),
        )
    }
}
