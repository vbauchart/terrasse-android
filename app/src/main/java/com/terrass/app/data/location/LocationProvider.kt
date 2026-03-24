package com.terrass.app.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    @SuppressLint("MissingPermission")
    fun locationUpdates(intervalMs: Long = 10_000L): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, null)
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    suspend fun lastLocation(): Location? {
        return try {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location -> cont.resume(location) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } catch (_: Exception) {
            null
        }
    }
}
