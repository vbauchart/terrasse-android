package com.terrass.app.data.remote

import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.preferences.AuthTokenProvider
import com.terrass.app.data.preferences.DeviceIdProvider
import com.terrass.app.data.remote.dto.TerraceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
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
    private val authTokenProvider: AuthTokenProvider,
) {
    private val baseUrl = PocketBaseConfig.BASE_URL

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun ensureAuth() = withContext(Dispatchers.IO) {
        if (authTokenProvider.getToken() != null) return@withContext

        val deviceId = deviceIdProvider.getDeviceId()
        val email = "$deviceId@terrasse.app"
        val password = authTokenProvider.getPassword() ?: UUID.randomUUID().toString().also {
            authTokenProvider.savePassword(it)
        }

        // Essaie d'enregistrer, fall back sur login si déjà existant
        val token = runCatching { registerDevice(email, password) }
            .recoverCatching { loginDevice(email, password) }
            .recoverCatching {
                // Cas extrême : serveur réinitialisé, on recrée les credentials
                val newPassword = UUID.randomUUID().toString()
                authTokenProvider.savePassword(newPassword)
                registerDevice(email, newPassword)
            }
            .getOrThrow()

        authTokenProvider.saveToken(token)
    }

    private fun registerDevice(email: String, password: String): String {
        val conn = openConn("$baseUrl/api/collections/devices/records")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("passwordConfirm", password)
            put("emailVisibility", false)
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
                throw java.io.IOException("Register failed ($code): $err")
            }
            conn.inputStream.bufferedReader().readText()
            return loginDevice(email, password)
        } finally {
            conn.disconnect()
        }
    }

    private fun loginDevice(email: String, password: String): String {
        val conn = openConn("$baseUrl/api/collections/devices/auth-with-password")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().apply {
            put("identity", email)
            put("password", password)
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
                throw java.io.IOException("Login failed ($code): $err")
            }
            return JSONObject(conn.inputStream.bufferedReader().readText()).getString("token")
        } finally {
            conn.disconnect()
        }
    }

    // ── Terrasses ─────────────────────────────────────────────────────────────

    suspend fun fetchAllTerraces(): List<TerraceDto> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TerraceDto>()
        var page = 1
        val perPage = 200
        while (true) {
            val conn = openAuthConn("$baseUrl/api/collections/terraces/records?filter=(status='active')&page=$page&perPage=$perPage")
            try {
                val json = JSONObject(conn.checkResponse().inputStream.bufferedReader().readText())
                val items = json.getJSONArray("items")
                for (i in 0 until items.length()) results.add(TerraceDto.fromJson(items.getJSONObject(i)))
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
        val conn = openAuthConn("$baseUrl/api/collections/terraces/records")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.use { it.write(entity.toJsonBody(deviceIdProvider.getDeviceId()).toString().toByteArray()) }
        try {
            JSONObject(conn.inputStream.bufferedReader().readText()).getString("id")
        } finally {
            conn.disconnect()
        }
    }

    suspend fun updateTerrace(remoteId: String, entity: TerraceEntity) = withContext(Dispatchers.IO) {
        val conn = openAuthConn("$baseUrl/api/collections/terraces/records/$remoteId")
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.use { it.write(entity.toJsonBody(deviceIdProvider.getDeviceId()).toString().toByteArray()) }
        try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    suspend fun deleteTerrace(remoteId: String) = withContext(Dispatchers.IO) {
        val conn = openAuthConn("$baseUrl/api/collections/terraces/records/$remoteId")
        conn.requestMethod = "DELETE"
        try {
            conn.responseCode
        } finally {
            conn.disconnect()
        }
    }

    // ── Votes ─────────────────────────────────────────────────────────────────

    suspend fun addVote(terraceRemoteId: String, isPositive: Boolean): String = withContext(Dispatchers.IO) {
        val conn = openAuthConn("$baseUrl/api/collections/votes/records")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().apply {
            put("terrace_id", terraceRemoteId)
            put("is_positive", isPositive)
            put("device_id", deviceIdProvider.getDeviceId())
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        val voteRemoteId = try {
            JSONObject(conn.inputStream.bufferedReader().readText()).getString("id")
        } finally {
            conn.disconnect()
        }
        patchTerraceCounter(terraceRemoteId, up = if (isPositive) 1 else 0, down = if (isPositive) 0 else 1)
        voteRemoteId
    }

    suspend fun updateVote(terraceRemoteId: String, voteRemoteId: String?, isPositive: Boolean) = withContext(Dispatchers.IO) {
        voteRemoteId?.let { id ->
            val conn = openAuthConn("$baseUrl/api/collections/votes/records/$id")
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write(JSONObject().apply { put("is_positive", isPositive) }.toString().toByteArray()) }
            try { conn.inputStream.bufferedReader().readText() } finally { conn.disconnect() }
        }
        val upDelta = if (isPositive) 1 else -1
        patchTerraceCounter(terraceRemoteId, up = upDelta, down = -upDelta)
    }

    private fun patchTerraceCounter(terraceRemoteId: String, up: Int, down: Int) {
        val conn = openAuthConn("$baseUrl/api/collections/terraces/records/$terraceRemoteId")
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().apply {
            if (up != 0) put("thumbs_up", "${if (up > 0) "+" else ""}$up")
            if (down != 0) put("thumbs_down", "${if (down > 0) "+" else ""}$down")
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try { conn.inputStream.bufferedReader().readText() } finally { conn.disconnect() }
    }

    // ── SSE temps réel ────────────────────────────────────────────────────────

    fun subscribeToEvents(): Flow<PocketBaseEvent> = flow {
        val sseConn = openAuthConn("$baseUrl/api/realtime", readTimeout = 0)
        sseConn.setRequestProperty("Accept", "text/event-stream")
        sseConn.connect()

        val reader = BufferedReader(InputStreamReader(sseConn.inputStream))
        try {
            var clientId: String? = null
            var subscribed = false
            var dataLine: String? = null
            while (true) {
                val line = reader.readLine() ?: break
                when {
                    line.startsWith("data:") -> dataLine = line.removePrefix("data:").trim()
                    line.isEmpty() && dataLine != null -> {
                        val json = JSONObject(dataLine)
                        dataLine = null
                        if (!subscribed) {
                            clientId = json.optString("clientId").ifBlank { null }
                            if (clientId != null) { subscribeClient(clientId); subscribed = true }
                        } else {
                            val action = json.optString("action")
                            val record = json.optJSONObject("record")
                            if (action.isNotBlank()) {
                                emit(PocketBaseEvent(
                                    action = action,
                                    record = record?.let { TerraceDto.fromJson(it) },
                                    recordId = record?.optString("id"),
                                ))
                            }
                        }
                    }
                }
            }
        } finally {
            reader.close()
            sseConn.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    private fun subscribeClient(clientId: String) {
        val conn = openAuthConn("$baseUrl/api/realtime")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        val body = JSONObject().apply {
            put("clientId", clientId)
            put("subscriptions", JSONArray().apply { put("terraces"); put("votes") })
        }.toString()
        conn.outputStream.use { it.write(body.toByteArray()) }
        try { conn.inputStream.bufferedReader().readText() } finally { conn.disconnect() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Connexion avec token d'auth (toutes les requêtes API). */
    private fun openAuthConn(url: String, readTimeout: Int = 10_000): HttpURLConnection =
        openConn(url, readTimeout).also { conn ->
            authTokenProvider.getToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        }

    /** Vérifie le code HTTP ; efface le token si 401 pour forcer un re-login au prochain appel. */
    private fun HttpURLConnection.checkResponse(): HttpURLConnection {
        val code = responseCode
        if (code == 401) authTokenProvider.clearToken()
        if (code !in 200..299) {
            val err = errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            throw java.io.IOException(err)
        }
        return this
    }

    /** Connexion sans auth (enregistrement / login). */
    private fun openConn(url: String, readTimeout: Int = 10_000): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            this.readTimeout = readTimeout
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
