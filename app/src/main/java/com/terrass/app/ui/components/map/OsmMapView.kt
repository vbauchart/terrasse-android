package com.terrass.app.ui.components.map

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.terrass.app.domain.model.Terrace
import com.terrass.app.ui.theme.Green40
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

data class MapMarkerData(
    val id: Long,
    val position: GeoPoint,
    val title: String,
    val votePercentage: Int,
)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(48.8566, 2.3522),
    zoom: Double = 15.0,
    terraces: List<Terrace> = emptyList(),
    userLocation: GeoPoint? = null,
    onMarkerClick: ((Long) -> Unit)? = null,
    onMapLongClick: ((GeoPoint) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(center)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // Recentrer et zoomer la carte
            view.controller.animateTo(center, zoom, 300L)

            // Long-press handler
            view.overlays.removeAll { it is MapEventsOverlay }
            if (onMapLongClick != null) {
                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        p?.let { onMapLongClick(it) }
                        return true
                    }
                })
                view.overlays.add(0, eventsOverlay)
            }

            // Remove old markers (keep overlays that are not Marker or UserLocationOverlay)
            view.overlays.removeAll { it is Marker || it is UserLocationOverlay }

            // Add terrace markers
            terraces.forEach { terrace ->
                val marker = Marker(view).apply {
                    position = GeoPoint(terrace.latitude, terrace.longitude)
                    title = terrace.name
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(terrace.id)
                        true
                    }
                }
                view.overlays.add(marker)
            }

            // User location dot
            if (userLocation != null) {
                view.overlays.add(UserLocationOverlay(userLocation))
            }

            view.invalidate()
        },
    )
}

private class UserLocationOverlay(private val location: GeoPoint) : Overlay() {
    private val paint = Paint().apply {
        color = Green40.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val point = Point()
        mapView.projection.toPixels(location, point)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 12f, paint)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 12f, borderPaint)
    }
}
