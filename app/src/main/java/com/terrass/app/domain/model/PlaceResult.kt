package com.terrass.app.domain.model

data class PlaceResult(
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
)
