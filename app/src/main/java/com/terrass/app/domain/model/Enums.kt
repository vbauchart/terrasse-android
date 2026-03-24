package com.terrass.app.domain.model

enum class SunTime(val value: String, val label: String) {
    MORNING("morning", "Matin"),
    NOON("noon", "Midi"),
    EVENING("evening", "Soir");

    companion object {
        fun fromValue(value: String?): SunTime? = entries.find { it.value == value }
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
