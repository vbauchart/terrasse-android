package com.terrass.app.data.remote

import com.terrass.app.domain.model.PlaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NominatimService @Inject constructor() {

    suspend fun search(query: String): List<PlaceResult> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = URL(
            "https://nominatim.openstreetmap.org/search" +
                "?q=$encodedQuery" +
                "&format=json" +
                "&addressdetails=1" +
                "&limit=10" +
                "&accept-language=fr"
        )

        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Terrass Android App")
        connection.setRequestProperty("Accept-Language", "fr")
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val response = connection.inputStream.bufferedReader().readText()
            parseResults(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResults(json: String): List<PlaceResult> {
        val array = JSONArray(json)
        val results = mutableListOf<PlaceResult>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val addressObj = obj.optJSONObject("address")
            val name = obj.optString("name").ifBlank {
                obj.optString("display_name").substringBefore(",").trim()
            }
            val road = addressObj?.optString("road").orEmpty()
            val city = addressObj?.optString("city")
                ?: addressObj?.optString("town")
                ?: addressObj?.optString("village")
                ?: ""
            val address = listOf(road, city).filter { it.isNotBlank() }.joinToString(", ")

            results.add(
                PlaceResult(
                    name = name,
                    displayName = obj.optString("display_name"),
                    latitude = obj.getString("lat").toDouble(),
                    longitude = obj.getString("lon").toDouble(),
                    address = address.ifBlank { null },
                )
            )
        }
        return results
    }
}
