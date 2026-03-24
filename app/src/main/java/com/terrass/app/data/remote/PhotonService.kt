package com.terrass.app.data.remote

import com.terrass.app.domain.model.PlaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

// Types OSM food & drink — https://wiki.openstreetmap.org/wiki/Key:amenity#Food_and_drink
private val OSM_TAG_PARAMS = listOf(
    "restaurant", "cafe", "bar", "pub",
    "fast_food", "food_court", "biergarten", "ice_cream",
).joinToString("") { "&osm_tag=amenity:$it" }

@Singleton
class PhotonService @Inject constructor() {

    suspend fun search(query: String, bbox: String? = null): List<PlaceResult> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val url = URL(
            "https://photon.komoot.io/api/" +
                "?q=$encodedQuery" +
                "&limit=10" +
                "&lang=fr" +
                OSM_TAG_PARAMS +
                (bbox?.let { "&bbox=$it" } ?: "")
        )

        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Terrass Android App")
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
        val features = JSONObject(json).getJSONArray("features")
        val results = mutableListOf<PlaceResult>()
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val props = feature.getJSONObject("properties")
            val coords = feature.getJSONObject("geometry").getJSONArray("coordinates")

            val name = props.optString("name")
            if (name.isBlank()) continue

            val housenumber = props.optString("housenumber").ifBlank { null }
            val street = props.optString("street").ifBlank { null }
            val postcode = props.optString("postcode").ifBlank { null }
            val city = props.optString("city").ifBlank { null }
            val state = props.optString("state").ifBlank { null }
            val country = props.optString("country").ifBlank { null }

            val streetFull = listOfNotNull(housenumber, street).joinToString(" ").ifBlank { null }

            results.add(
                PlaceResult(
                    name = name,
                    displayName = listOfNotNull(name, streetFull, postcode, city, state, country).joinToString(", "),
                    latitude = coords.getDouble(1),  // GeoJSON : [lon, lat]
                    longitude = coords.getDouble(0),
                    address = listOfNotNull(streetFull, city).joinToString(", ").ifBlank { null },
                )
            )
        }
        return results
    }
}
