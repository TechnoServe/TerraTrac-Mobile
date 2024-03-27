package org.technoserve.farmcollector.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.infoWindowClickEvents
import com.tns.lab.composegooglemaps.MapState
import com.tns.lab.composegooglemaps.clusters.ZoneClusterManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("PotentialBehaviorOverride")
@Composable
fun MapScreen(
    state: MapState,
    setupClusterManager: (Context, GoogleMap) -> ZoneClusterManager,
    calculateZoneViewCenter: () -> LatLngBounds,
) {
    // Set properties using MapProperties which you can use to recompose the map
    val mapProperties = MapProperties(
        // Only enable if user has accepted location permissions.
        isMyLocationEnabled = state.lastKnownLocation != null,
    )
    val cameraPositionState = rememberCameraPositionState()
    val polylineOptions = remember { // MutableState for polyline
        mutableStateOf(PolylineOptions())
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState
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

                        val marker = map.addMarker(markerOptions)
                        // Update polyline with each marker position
                        markerPositions.add(LatLng(latitude, longitude))
                    }
                    polylineOptions.value.addAll(markerPositions)
                    map.addPolyline(polylineOptions.value)
                }else
                {
                    map.clear()
                }
            }
            // Listen on every changes happen on clusterItems such addcoordinates and then run the following code
            MapEffect(state.clusterItems) { map ->
                 if (state.clusterItems.isNotEmpty()) {

                    val clusterManager = setupClusterManager(context, map)
                    map.setOnCameraIdleListener(clusterManager)
                    // map.setOnMarkerClickListener(clusterManager)
                    state.clusterItems.forEach { clusterItem ->
                        map.addPolygon(clusterItem.polygonOptions)
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
    }
//    // Center camera to include all the Zones.
//    LaunchedEffect(state.clusterItems) {
//        if (state.clusterItems.isNotEmpty()) {
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngBounds(
//                    calculateZoneViewCenter(),
//                    0
//                ),
//            )
//        }
//    }
}

/**
 * If you want to center on a specific location.
 */
private suspend fun CameraPositionState.centerOnLocation(
    location: Location
) = animate(
    update = CameraUpdateFactory.newLatLngZoom(
        LatLng(location.latitude, location.longitude),
        15f
    ),
)

// Marker click listener
private fun onMarkerClick(marker: Marker) {
    // Remove the clicked marker
    marker.remove()
}

