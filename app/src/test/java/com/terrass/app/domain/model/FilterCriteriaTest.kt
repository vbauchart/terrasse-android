package com.terrass.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FilterCriteriaTest {

    private val baseTerrace = Terrace(
        id = 1, name = "Test", latitude = 43.6, longitude = 1.4,
        sunExposure = SunExposure(Orientation.SOUTH, ExposureType.FULL_SUN),
        comfort = Comfort(isCovered = true, isHeated = false, furnitureType = FurnitureType.CHAIRS, size = TerraceSize.MEDIUM),
        environment = Environment(noiseLevel = NoiseLevel.QUIET, viewQuality = ViewQuality.GOOD, hasVegetation = true),
        service = Service(quality = ServiceQuality.GOOD, priceRange = PriceRange.MODERATE),
        thumbsUp = 8, thumbsDown = 2,
    )

    @Test
    fun `empty filter matches everything`() {
        assertTrue(FilterCriteria().matches(baseTerrace))
    }

    @Test
    fun `filter by exposure type matches`() {
        val filter = FilterCriteria(exposureTypes = setOf(ExposureType.FULL_SUN))
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by exposure type rejects`() {
        val filter = FilterCriteria(exposureTypes = setOf(ExposureType.SHADE))
        assertFalse(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by orientation matches`() {
        val filter = FilterCriteria(orientations = setOf(Orientation.SOUTH, Orientation.EAST))
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by orientation rejects`() {
        val filter = FilterCriteria(orientations = setOf(Orientation.NORTH))
        assertFalse(filter.matches(baseTerrace))
    }

    @Test
    fun `filter isCovered true matches`() {
        assertTrue(FilterCriteria(isCovered = true).matches(baseTerrace))
    }

    @Test
    fun `filter isCovered false rejects`() {
        assertFalse(FilterCriteria(isCovered = false).matches(baseTerrace))
    }

    @Test
    fun `filter isHeated true rejects`() {
        assertFalse(FilterCriteria(isHeated = true).matches(baseTerrace))
    }

    @Test
    fun `filter by furniture type matches`() {
        assertTrue(FilterCriteria(furnitureTypes = setOf(FurnitureType.CHAIRS)).matches(baseTerrace))
    }

    @Test
    fun `filter by furniture type rejects`() {
        assertFalse(FilterCriteria(furnitureTypes = setOf(FurnitureType.LOUNGE)).matches(baseTerrace))
    }

    @Test
    fun `filter by size matches`() {
        assertTrue(FilterCriteria(sizes = setOf(TerraceSize.MEDIUM)).matches(baseTerrace))
    }

    @Test
    fun `filter by noise level matches`() {
        assertTrue(FilterCriteria(noiseLevels = setOf(NoiseLevel.QUIET)).matches(baseTerrace))
    }

    @Test
    fun `filter by noise level rejects`() {
        assertFalse(FilterCriteria(noiseLevels = setOf(NoiseLevel.NOISY)).matches(baseTerrace))
    }

    @Test
    fun `filter by view quality matches`() {
        assertTrue(FilterCriteria(viewQualities = setOf(ViewQuality.GOOD, ViewQuality.EXCEPTIONAL)).matches(baseTerrace))
    }

    @Test
    fun `filter hasVegetation true matches`() {
        assertTrue(FilterCriteria(hasVegetation = true).matches(baseTerrace))
    }

    @Test
    fun `filter by service quality matches`() {
        assertTrue(FilterCriteria(serviceQualities = setOf(ServiceQuality.GOOD)).matches(baseTerrace))
    }

    @Test
    fun `filter by price range matches`() {
        assertTrue(FilterCriteria(priceRanges = setOf(PriceRange.MODERATE)).matches(baseTerrace))
    }

    @Test
    fun `filter by price range rejects`() {
        assertFalse(FilterCriteria(priceRanges = setOf(PriceRange.CHEAP)).matches(baseTerrace))
    }

    @Test
    fun `filter by minPositivePercent matches`() {
        assertTrue(FilterCriteria(minPositivePercent = 70).matches(baseTerrace)) // 80%
    }

    @Test
    fun `filter by minPositivePercent rejects`() {
        assertFalse(FilterCriteria(minPositivePercent = 90).matches(baseTerrace)) // 80%
    }

    @Test
    fun `filter by minPositivePercent rejects terrace with no votes`() {
        val noVotes = baseTerrace.copy(thumbsUp = 0, thumbsDown = 0)
        assertFalse(FilterCriteria(minPositivePercent = 50).matches(noVotes))
    }

    @Test
    fun `combined filters all must match`() {
        val filter = FilterCriteria(
            exposureTypes = setOf(ExposureType.FULL_SUN),
            noiseLevels = setOf(NoiseLevel.QUIET),
            isCovered = true,
        )
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `combined filters one fails rejects`() {
        val filter = FilterCriteria(
            exposureTypes = setOf(ExposureType.FULL_SUN),
            noiseLevels = setOf(NoiseLevel.NOISY), // mismatch
        )
        assertFalse(filter.matches(baseTerrace))
    }

    @Test
    fun `activeCount counts active filters`() {
        val filter = FilterCriteria(
            exposureTypes = setOf(ExposureType.FULL_SUN),
            isCovered = true,
            minPositivePercent = 50,
        )
        assertEquals(3, filter.activeCount)
    }

    @Test
    fun `activeCount is zero for empty filter`() {
        assertEquals(0, FilterCriteria().activeCount)
    }

    @Test
    fun `filter with null attribute in terrace rejects`() {
        val noExposure = baseTerrace.copy(sunExposure = SunExposure())
        assertFalse(FilterCriteria(exposureTypes = setOf(ExposureType.FULL_SUN)).matches(noExposure))
    }
}
