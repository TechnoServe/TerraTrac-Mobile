package org.technoserve.farmcollector.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.technoserve.farmcollector.R
@OptIn(MapsComposeExperimentalApi::class)
@SuppressLint("PotentialBehaviorOverride")
@Composable
fun MapScreen(
    state: MapState,
    setupClusterManager: (Context, GoogleMap) -> ZoneClusterManager,
    calculateZoneViewCenter: () -> LatLngBounds,
    onMapTypeChange: (MapType) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()
    val polylineOptions = remember { // MutableState for polyline
        mutableStateOf(PolylineOptions())
    }
    // Set properties using MapProperties which you can use to recompose the map
    val mapProperties = MapProperties(
        // Only enable if user has accepted location permissions.
        isMyLocationEnabled = state.lastKnownLocation != null,
        mapType = state.mapType,
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        var isLayerCardVisible by remember { mutableStateOf(false) }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState,
        ) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Listen every changes made on state.markers and then run beneath code
            MapEffect(state.markers) { map ->
                if (state.clearMap) {
                    map.clear()
                    state.clearMap = false
                }
                map.clear()
                val clusterManager = setupClusterManager(context, map)
                map.setOnCameraIdleListener(clusterManager)
                polylineOptions.value = PolylineOptions().color(android.graphics.Color.RED)
                val markerPositions = mutableListOf<LatLng>()
                if (state.markers?.isNotEmpty() == true) {
                    state.markers?.forEach { (latitude, longitude) ->
                        //  polylineOptions.value = PolylineOptions()
                        val markerOptions = MarkerOptions()
                        markerOptions.position(LatLng(latitude, longitude))
                        markerOptions.snippet("(${latitude}, ${longitude})")
                            .title("Point")
                            .draggable(true)

                        // Update polyline with each marker position
                        markerPositions.add(LatLng(latitude, longitude))
                    }
                    polylineOptions.value.addAll(markerPositions)
                    map.addPolyline(polylineOptions.value)
                } else {
                    map.clear()
                }
            }
            // Listen on every changes happen on clusterItems such addCoordinates and then run the following code
            MapEffect(state.clusterItems) { map ->
                if (state.clusterItems.isNotEmpty()) {
                    val clusterManager = setupClusterManager(context, map)
                    map.setOnCameraIdleListener(clusterManager)
                    map.setMinZoomPreference(4f)
                    map.uiSettings.isMapToolbarEnabled = true
                    map.uiSettings.isCompassEnabled = true
                    // map.setOnMarkerClickListener(clusterManager)
                    state.clusterItems.forEach { clusterItem ->
                        map.addPolygon(clusterItem.polygonOptions)
//                            .apply {
//                            strokeColor(android.graphics.Color.BLUE) // Highlight the stroke
//                            strokeWidth(5f)
//                            fillColor(android.graphics.Color.argb(128, 255, 255, 0)) // Semi-transparent fill
//                        })
                    }
                    map.setOnMapLoadedCallback {
                        if (state.clusterItems.isNotEmpty()) {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(
                                        calculateZoneViewCenter(),
                                        0
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLayerCardVisible) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 20.dp)
                    .clickable { isLayerCardVisible = !isLayerCardVisible }
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                state.mapType = MapType.NORMAL
                                onMapTypeChange(MapType.NORMAL)
                                isLayerCardVisible = !isLayerCardVisible
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gm_default_type),
                            contentDescription = "Default Type"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Default",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                state.mapType = MapType.SATELLITE
                                onMapTypeChange(MapType.SATELLITE)
                                isLayerCardVisible = !isLayerCardVisible
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gm_satellite_type),
                            contentDescription = "Satellite Type"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Satellite",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                state.mapType = MapType.TERRAIN
                                onMapTypeChange(MapType.TERRAIN)
                                isLayerCardVisible = !isLayerCardVisible
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gm_terrain_type),
                            contentDescription = "Terrain Type"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Terrain",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Toggle button icon
        IconButton(
            onClick = { isLayerCardVisible = !isLayerCardVisible },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .shadow(4.dp, clip = true)
                .background(Color.White)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_layers_24),
                contentDescription = "Layer Selector"
            )
        }

//    // Center camera to include all the Zones.
        LaunchedEffect(state.clusterItems) {
            if (state.clusterItems.isNotEmpty()) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(
                        calculateZoneViewCenter(),
                        0
                    ),
                )
            }
        }
    }

    /**
     * If you want to center on a specific location.
     */
    suspend fun CameraPositionState.centerOnLocation(
        location: Location
    ) = animate(
        update = CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            15f
        ),
    )

    // Marker click listener
    fun onMarkerClick(marker: Marker) {
        // Remove the clicked marker
        marker.remove()
    }
}

