package com.terrass.app.data.location

import android.content.Context
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReverseGeocodingService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun getAddress(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            try {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null
                val road = address.thoroughfare
                val number = address.subThoroughfare
                val city = address.locality ?: address.subAdminArea
                listOfNotNull(
                    if (road != null && number != null) "$number $road" else road,
                    city,
                ).joinToString(", ").ifBlank { null }
            } catch (_: Exception) {
                null
            }
        }
}
