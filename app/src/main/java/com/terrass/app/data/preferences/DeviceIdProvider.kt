package com.terrass.app.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)

    fun getDeviceId(): String {
        return prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also { newId ->
            prefs.edit().putString("device_id", newId).apply()
        }
    }
}
