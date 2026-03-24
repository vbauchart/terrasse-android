package com.terrass.app.domain.model

enum class Orientation(val value: String, val label: String) {
    NORTH("north", "N"),
    SOUTH("south", "S"),
    EAST("east", "E"),
    WEST("west", "O"),
    NORTHEAST("northeast", "NE"),
    NORTHWEST("northwest", "NO"),
    SOUTHEAST("southeast", "SE"),
    SOUTHWEST("southwest", "SO");

    companion object {
        fun fromValue(value: String?): Orientation? = entries.find { it.value == value }
    }
}

enum class ExposureType(val value: String, val label: String) {
    FULL_SUN("full_sun", "Plein soleil"),
    PARTIAL("partial", "Partiel"),
    SHADE("shade", "Ombre");

    companion object {
        fun fromValue(value: String?): ExposureType? = entries.find { it.value == value }
    }
}

enum class FurnitureType(val value: String, val label: String) {
    CHAIRS("chairs", "Chaises"),
    BENCHES("benches", "Bancs"),
    LOUNGE("lounge", "Lounge"),
    MIXED("mixed", "Mixte");

    companion object {
        fun fromValue(value: String?): FurnitureType? = entries.find { it.value == value }
    }
}

enum class TerraceSize(val value: String, val label: String) {
    SMALL("small", "Petite"),
    MEDIUM("medium", "Moyenne"),
    LARGE("large", "Grande");

    companion object {
        fun fromValue(value: String?): TerraceSize? = entries.find { it.value == value }
    }
}

enum class RoadProximity(val value: String, val label: String) {
    NONE("none", "Aucune"),
    LOW("low", "Faible"),
    MEDIUM("medium", "Moyenne"),
    HIGH("high", "Forte");

    companion object {
        fun fromValue(value: String?): RoadProximity? = entries.find { it.value == value }
    }
}

enum class NoiseLevel(val value: String, val label: String) {
    QUIET("quiet", "Calme"),
    MODERATE("moderate", "Modéré"),
    NOISY("noisy", "Bruyant");

    companion object {
        fun fromValue(value: String?): NoiseLevel? = entries.find { it.value == value }
    }
}

enum class ViewQuality(val value: String, val label: String) {
    NONE("none", "Aucune"),
    PARTIAL("partial", "Partielle"),
    GOOD("good", "Belle"),
    EXCEPTIONAL("exceptional", "Exceptionnelle");

    companion object {
        fun fromValue(value: String?): ViewQuality? = entries.find { it.value == value }
    }
}

enum class ServiceQuality(val value: String, val label: String) {
    POOR("poor", "Médiocre"),
    AVERAGE("average", "Moyen"),
    GOOD("good", "Bon"),
    EXCELLENT("excellent", "Excellent");

    companion object {
        fun fromValue(value: String?): ServiceQuality? = entries.find { it.value == value }
    }
}

enum class PriceRange(val value: String, val label: String) {
    CHEAP("cheap", "Bon marché"),
    MODERATE("moderate", "Moyen"),
    EXPENSIVE("expensive", "Cher");

    companion object {
        fun fromValue(value: String?): PriceRange? = entries.find { it.value == value }
    }
}

enum class TerraceStatus(val value: String) {
    ACTIVE("active"),
    PENDING("pending"),
    HIDDEN("hidden");

    companion object {
        fun fromValue(value: String?): TerraceStatus = entries.find { it.value == value } ?: ACTIVE
    }
}
