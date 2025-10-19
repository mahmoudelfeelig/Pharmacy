package com.example.pharmacy.feature.map
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable fun MapScreen(onBack:()->Unit) {
    AndroidView(
        factory = { ctx ->
            setupOsmdroid(ctx)
            MapView(ctx).apply {
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(30.0444, 31.2357))
                listOf(
                    GeoPoint(30.0470,31.2330), GeoPoint(30.0425,31.2401), GeoPoint(30.0459,31.2380)
                ).forEach {
                    overlays.add(Marker(this).apply { position = it; title = "Drug Store" })
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
private fun setupOsmdroid(ctx: Context) {
    Configuration.getInstance().userAgentValue = "com.example.pharmacy"
}
