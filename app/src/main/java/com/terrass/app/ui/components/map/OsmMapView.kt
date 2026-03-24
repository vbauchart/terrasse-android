package com.terrass.app.ui.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.terrass.app.R
import com.terrass.app.domain.model.Terrace
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private const val PIN_WIDTH_DP = 40
private const val PIN_HEIGHT_DP = 56
private const val MARKER_POSITIVE_COLOR = 0xFF388E3C.toInt()
private const val MARKER_NEGATIVE_COLOR = 0xFFD32F2F.toInt()
private const val MARKER_NEUTRAL_COLOR = 0xFF9E9E9E.toInt()

private fun createPinIcon(context: Context, pinColor: Int): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val w = (PIN_WIDTH_DP * density).toInt()
    val h = (PIN_HEIGHT_DP * density).toInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = w / 2f
    val radius = w / 2f - 2 * density  // circle radius with small margin for stroke
    val circleY = radius + 2 * density  // center Y of circle

    // Draw pin shape: circle + triangle pointing down
    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pinColor
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2 * density
    }

    // Triangle (pointer)
    val pointerPath = Path().apply {
        moveTo(cx - radius * 0.45f, circleY + radius * 0.7f)
        lineTo(cx, h.toFloat() - density)
        lineTo(cx + radius * 0.45f, circleY + radius * 0.7f)
        close()
    }
    canvas.drawPath(pointerPath, pinPaint)

    // Circle
    canvas.drawCircle(cx, circleY, radius, pinPaint)
    canvas.drawCircle(cx, circleY, radius, strokePaint)

    // Draw logo inside the circle
    val logoDrawable = ContextCompat.getDrawable(context, R.drawable.ic_logo_foreground)!!.mutate()
    logoDrawable.setTint(Color.WHITE)
    val iconInset = (radius * 0.55f).toInt()  // logo slightly smaller than circle
    logoDrawable.setBounds(
        (cx - iconInset).toInt(),
        (circleY - iconInset).toInt(),
        (cx + iconInset).toInt(),
        (circleY + iconInset).toInt(),
    )
    logoDrawable.draw(canvas)

    return BitmapDrawable(context.resources, bitmap)
}

private fun markerColorForVotes(votePercentage: Int): Int = when {
    votePercentage < 0 -> MARKER_NEUTRAL_COLOR
    votePercentage >= 50 -> MARKER_POSITIVE_COLOR
    else -> MARKER_NEGATIVE_COLOR
}

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

    val pinIcons = remember {
        mapOf(
            MARKER_POSITIVE_COLOR to createPinIcon(context, MARKER_POSITIVE_COLOR),
            MARKER_NEGATIVE_COLOR to createPinIcon(context, MARKER_NEGATIVE_COLOR),
            MARKER_NEUTRAL_COLOR to createPinIcon(context, MARKER_NEUTRAL_COLOR),
        )
    }

    val lastCenter = remember { mutableStateOf(center) }
    val lastZoom = remember { mutableStateOf(zoom) }

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
            // Recentrer/zoomer uniquement si center ou zoom ont changé
            if (center != lastCenter.value || zoom != lastZoom.value) {
                view.controller.animateTo(center, zoom, 300L)
                lastCenter.value = center
                lastZoom.value = zoom
            }

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

            // Add terrace markers with colored pin + logo
            terraces.forEach { terrace ->
                val color = markerColorForVotes(terrace.votePercentage)
                val marker = Marker(view).apply {
                    position = GeoPoint(terrace.latitude, terrace.longitude)
                    title = terrace.name
                    icon = pinIcons[color]
                    setAnchor(Marker.ANCHOR_CENTER, 1.0f)  // pointe du pin
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
        color = 0xFF1976D2.toInt()  // Blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE
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
