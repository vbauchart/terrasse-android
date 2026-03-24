package com.terrass.app.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FilterCriteriaTest {

    private val baseTerrace = Terrace(
        id = 1, name = "Test", latitude = 43.6, longitude = 1.4,
        sunExposure = SunExposure(setOf(SunTime.MORNING, SunTime.NOON)),
        comfort = Comfort(isCovered = true, isHeated = false, size = TerraceSize.MEDIUM),
        environment = Environment(noiseLevel = NoiseLevel.QUIET, viewQuality = ViewQuality.GOOD, hasVegetation = true),
        service = Service(quality = ServiceQuality.GOOD, priceRange = PriceRange.MODERATE),
        thumbsUp = 8, thumbsDown = 2,
    )

    @Test
    fun `empty filter matches everything`() {
        assertTrue(FilterCriteria().matches(baseTerrace))
    }

    @Test
    fun `filter by sun time matches when terrace has that time`() {
        val filter = FilterCriteria(sunTimes = setOf(SunTime.MORNING))
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by sun time rejects when terrace lacks that time`() {
        val filter = FilterCriteria(sunTimes = setOf(SunTime.EVENING))
        assertFalse(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by sun time matches when any time matches`() {
        val filter = FilterCriteria(sunTimes = setOf(SunTime.NOON, SunTime.EVENING))
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `filter by sun time rejects terrace with no sun times`() {
        val noSun = baseTerrace.copy(sunExposure = SunExposure())
        assertFalse(FilterCriteria(sunTimes = setOf(SunTime.MORNING)).matches(noSun))
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
    fun `filter by size matches`() {
        assertTrue(FilterCriteria(sizes = setOf(TerraceSize.MEDIUM)).matches(baseTerrace))
    }

    @Test
    fun `filter by size rejects`() {
        assertFalse(FilterCriteria(sizes = setOf(TerraceSize.LARGE)).matches(baseTerrace))
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
            sunTimes = setOf(SunTime.MORNING),
            noiseLevels = setOf(NoiseLevel.QUIET),
            isCovered = true,
        )
        assertTrue(filter.matches(baseTerrace))
    }

    @Test
    fun `combined filters one fails rejects`() {
        val filter = FilterCriteria(
            sunTimes = setOf(SunTime.MORNING),
            noiseLevels = setOf(NoiseLevel.NOISY), // mismatch
        )
        assertFalse(filter.matches(baseTerrace))
    }

    @Test
    fun `activeCount counts active filters`() {
        val filter = FilterCriteria(
            sunTimes = setOf(SunTime.NOON),
            isCovered = true,
            minPositivePercent = 50,
        )
        assertEquals(3, filter.activeCount)
    }

    @Test
    fun `activeCount is zero for empty filter`() {
        assertEquals(0, FilterCriteria().activeCount)
    }
}
