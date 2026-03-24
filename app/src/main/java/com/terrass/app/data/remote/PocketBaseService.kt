package com.terrass.app.data.remote

import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.preferences.DeviceIdProvider
import com.terrass.app.data.remote.dto.TerraceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class PocketBaseEvent(
    val action: String,
    val record: TerraceDto?,
    val recordId: String?,
)

@Singleton
class PocketBaseService @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider,
) {
    private val baseUrl = PocketBaseConfig.BASE_URL

    suspend fun fetchAllTerraces(): List<TerraceDto> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TerraceDto>()
        var page = 1
        val perPage = 200
        while (true) {
            val url = URL("$baseUrl/api/collections/terraces/records?filter=(status='active')&page=$page&perPage=$perPage")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            try {
                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val items = json.getJSONArray("items")
                for (i in 0 until items.length()) {
                    results.add(TerraceDto.fromJson(items.getJSONObject(i)))
                }
                val totalPages = json.optInt("totalPages", 1)
                if (page >= totalPages) break
                page++
            } finally {
                conn.disconnect()
            }
        }
        results
    }

    suspend fun createTerrace(entity: TerraceEntity): String = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/api/collections/terraces/records")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        val body = entity.toJsonBody(deviceIdProvider.getDeviceId()).toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try {
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response).getString("id")
        } finally {
            conn.disconnect()
        }
    }

    suspend fun updateTerrace(remoteId: String, entity: TerraceEntity) = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/api/collections/terraces/records/$remoteId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        val body = entity.toJsonBody(deviceIdProvider.getDeviceId()).toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    suspend fun deleteTerrace(remoteId: String) = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/api/collections/terraces/records/$remoteId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            conn.responseCode
        } finally {
            conn.disconnect()
        }
    }

    suspend fun addVote(terraceRemoteId: String, isPositive: Boolean) = withContext(Dispatchers.IO) {
        val deviceId = deviceIdProvider.getDeviceId()
        // Create vote record
        val voteUrl = URL("$baseUrl/api/collections/votes/records")
        val voteConn = voteUrl.openConnection() as HttpURLConnection
        voteConn.requestMethod = "POST"
        voteConn.setRequestProperty("Content-Type", "application/json")
        voteConn.doOutput = true
        voteConn.connectTimeout = 10_000
        voteConn.readTimeout = 10_000
        val voteBody = JSONObject().apply {
            put("terrace_id", terraceRemoteId)
            put("is_positive", isPositive)
            put("device_id", deviceId)
        }.toString()
        voteConn.outputStream.use { it.write(voteBody.toByteArray()) }
        try {
            voteConn.inputStream.bufferedReader().readText()
        } finally {
            voteConn.disconnect()
        }

        // PATCH increment counter on terrace
        val field = if (isPositive) "thumbs_up" else "thumbs_down"
        val patchUrl = URL("$baseUrl/api/collections/terraces/records/$terraceRemoteId")
        val patchConn = patchUrl.openConnection() as HttpURLConnection
        patchConn.requestMethod = "PATCH"
        patchConn.setRequestProperty("Content-Type", "application/json")
        patchConn.doOutput = true
        patchConn.connectTimeout = 10_000
        patchConn.readTimeout = 10_000
        // PocketBase supports increment via "+N" syntax
        val patchBody = JSONObject().apply { put(field, "+1") }.toString()
        patchConn.outputStream.use { it.write(patchBody.toByteArray()) }
        try {
            patchConn.inputStream.bufferedReader().readText()
        } finally {
            patchConn.disconnect()
        }
    }

    fun subscribeToEvents(): Flow<PocketBaseEvent> = flow {
        // Step 1: open SSE stream
        val sseUrl = URL("$baseUrl/api/realtime")
        val sseConn = sseUrl.openConnection() as HttpURLConnection
        sseConn.setRequestProperty("Accept", "text/event-stream")
        sseConn.connectTimeout = 10_000
        sseConn.readTimeout = 0 // no timeout for SSE
        sseConn.connect()

        val reader = BufferedReader(InputStreamReader(sseConn.inputStream))
        try {
            var clientId: String? = null
            var subscribed = false
            var dataLine: String? = null

            while (true) {
                val line = reader.readLine() ?: break
                when {
                    line.startsWith("data:") -> {
                        dataLine = line.removePrefix("data:").trim()
                    }
                    line.isEmpty() && dataLine != null -> {
                        val json = JSONObject(dataLine)
                        dataLine = null
                        if (!subscribed) {
                            // First event is PB_CONNECT
                            clientId = json.optString("clientId").ifBlank { null }
                            if (clientId != null) {
                                subscribeClient(clientId)
                                subscribed = true
                            }
                        } else {
                            val action = json.optString("action")
                            val record = json.optJSONObject("record")
                            if (action.isNotBlank()) {
                                val recordId = record?.optString("id")
                                val dto = record?.let { TerraceDto.fromJson(it) }
                                emit(PocketBaseEvent(action = action, record = dto, recordId = recordId))
                            }
                        }
                    }
                }
            }
        } finally {
            reader.close()
            sseConn.disconnect()
        }
    }.flowOn(Dispatchers.IO).retryWhen { _, attempt ->
        delay(minOf(2000L * (attempt + 1), 30_000L))
        true
    }

    private fun subscribeClient(clientId: String) {
        val url = URL("$baseUrl/api/realtime")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        val body = JSONObject().apply {
            put("clientId", clientId)
            put("subscriptions", JSONArray().apply {
                put("terraces")
                put("votes")
            })
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    private fun TerraceEntity.toJsonBody(deviceId: String) = JSONObject().apply {
        put("name", name)
        put("latitude", latitude)
        put("longitude", longitude)
        put("address", address ?: "")
        put("sun_times", sunTimes ?: "")
        put("is_covered", isCovered)
        put("is_heated", isHeated)
        put("size", size ?: "")
        put("road_proximity", roadProximity ?: "")
        put("noise_level", noiseLevel ?: "")
        put("view_quality", viewQuality ?: "")
        put("has_vegetation", hasVegetation)
        put("service_quality", serviceQuality ?: "")
        put("price_range", priceRange ?: "")
        put("cuisine_type", cuisineType ?: "")
        put("status", status)
        put("device_id", deviceId)
        put("thumbs_up", thumbsUp)
        put("thumbs_down", thumbsDown)
    }
}
