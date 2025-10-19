package com.example.pharmacy.feature.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core_ui.design.Spacing
import com.example.pharmacy.core.data.FirestorePharmacyRepository
import com.example.pharmacy.core.domain.pharmacy.Pharmacy
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val repo = remember { FirestorePharmacyRepository() }
    var items by remember { mutableStateOf<List<Pharmacy>>(emptyList()) }
    var selected by remember { mutableStateOf<Pharmacy?>(null) }
    var mapRef by remember { mutableStateOf<MapView?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { repo.streamAll().collect { items = it } }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nearby Pharmacies") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    setupOsmdroid(ctx)
                    MapView(ctx).also { map ->
                        mapRef = map
                        map.setMultiTouchControls(true)
                        map.zoomController.setVisibility(
                            CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                        )
                        map.controller.setZoom(13.0)
                        map.controller.setCenter(GeoPoint(30.0444, 31.2357))
                        map.overlays.add(ScaleBarOverlay(map))
                    }
                },
                update = { map ->
                    // rebuild markers
                    map.overlays.removeAll { it is Marker }
                    val source = if (items.isEmpty()) {
                        listOf(
                            Pharmacy("1","Drug Store",30.0470,31.2330),
                            Pharmacy("2","Pharma Plus",30.0425,31.2401),
                            Pharmacy("3","HealthCare",30.0459,31.2380)
                        )
                    } else items
                    source.forEach { p ->
                        map.overlays.add(Marker(map).apply {
                            position = GeoPoint(p.lat, p.lon)
                            title = p.name
                            setOnMarkerClickListener { _, _ ->
                                selected = p
                                map.controller.animateTo(position, 16.0, 500L)
                                true
                            }
                        })
                    }
                    map.invalidate()
                }
            )

            // Zoom + clear
            Row(
                Modifier.align(Alignment.BottomEnd).padding(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                FilledTonalButton(onClick = { mapRef?.controller?.zoomOut() }) { Text("-") }
                FilledTonalButton(onClick = { mapRef?.controller?.zoomIn() })  { Text("+") }
                FilledTonalButton(onClick = { selected = null }) { Text("Clear") }
            }

            // Bottom info card
            selected?.let { p ->
                ElevatedCard(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(Spacing.lg)
                ) {
                    Column(Modifier.padding(Spacing.lg)) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        p.address?.let { Text(it) }
                        p.rating?.let { Text("Rating: $it") }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { selected = null }) { Text("Close") }
                        }
                    }
                }
            }
        }
    }
}

private fun setupOsmdroid(ctx: Context) {
    Configuration.getInstance().userAgentValue = "com.example.pharmacy"
}
