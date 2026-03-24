package com.terrass.app.domain.model

data class FilterCriteria(
    val sunTimes: Set<SunTime> = emptySet(),
    val isCovered: Boolean? = null,
    val isHeated: Boolean? = null,
    val sizes: Set<TerraceSize> = emptySet(),
    val noiseLevels: Set<NoiseLevel> = emptySet(),
    val viewQualities: Set<ViewQuality> = emptySet(),
    val hasVegetation: Boolean? = null,
    val serviceQualities: Set<ServiceQuality> = emptySet(),
    val priceRanges: Set<PriceRange> = emptySet(),
    val minPositivePercent: Int? = null,
) {
    val activeCount: Int
        get() {
            var count = 0
            if (sunTimes.isNotEmpty()) count++
            if (isCovered != null) count++
            if (isHeated != null) count++
            if (sizes.isNotEmpty()) count++
            if (noiseLevels.isNotEmpty()) count++
            if (viewQualities.isNotEmpty()) count++
            if (hasVegetation != null) count++
            if (serviceQualities.isNotEmpty()) count++
            if (priceRanges.isNotEmpty()) count++
            if (minPositivePercent != null) count++
            return count
        }

    fun matches(terrace: Terrace): Boolean {
        if (sunTimes.isNotEmpty() && !terrace.sunExposure.sunTimes.any { it in sunTimes }) return false
        if (isCovered != null && terrace.comfort.isCovered != isCovered) return false
        if (isHeated != null && terrace.comfort.isHeated != isHeated) return false
        if (sizes.isNotEmpty() && terrace.comfort.size !in sizes) return false
        if (noiseLevels.isNotEmpty() && terrace.environment.noiseLevel !in noiseLevels) return false
        if (viewQualities.isNotEmpty() && terrace.environment.viewQuality !in viewQualities) return false
        if (hasVegetation != null && terrace.environment.hasVegetation != hasVegetation) return false
        if (serviceQualities.isNotEmpty() && terrace.service.quality !in serviceQualities) return false
        if (priceRanges.isNotEmpty() && terrace.service.priceRange !in priceRanges) return false
        if (minPositivePercent != null) {
            val pct = terrace.votePercentage
            if (pct < 0 || pct < minPositivePercent) return false
        }
        return true
    }
}
