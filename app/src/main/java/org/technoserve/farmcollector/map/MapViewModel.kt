package com.tns.lab.composegooglemaps

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tns.lab.composegooglemaps.clusters.ZoneClusterItem
import com.tns.lab.composegooglemaps.clusters.ZoneClusterManager
import com.tns.lab.composegooglemaps.clusters.calculateCameraViewPoints
import com.tns.lab.composegooglemaps.clusters.getCenterOfPolygon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ktx.model.polygonOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(): ViewModel() {

    val state: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = null,
            markers = mutableListOf(),
            clusterItems = emptyList(),
            clearMap = false
        )
    )

    @SuppressLint("MissingPermission")
    fun getDeviceLocation(
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    state.value = state.value.copy(
                        lastKnownLocation = task.result,
                    )
                }
            }
        } catch (e: SecurityException) {
            // Show error or something
        }
    }

    fun setupClusterManager(
        context: Context,
        map: GoogleMap,
    ): ZoneClusterManager {
        val clusterManager = ZoneClusterManager(context, map)
        clusterManager.addItems(state.value.clusterItems)
        return clusterManager
    }

    fun calculateZoneLatLngBounds(): LatLngBounds {
        // Get all the points from all the polygons and calculate the camera view that will show them all.
        val latLngs = state.value.clusterItems.map { it.polygonOptions }
                .map { it.points.map { LatLng(it.latitude, it.longitude) } }.flatten()
       return latLngs.calculateCameraViewPoints().getCenterOfPolygon()
    }
    @Composable
    fun addMarker(latitude: Double, longitude: Double)
    {
        MarkerInfoWindow(
            state = rememberMarkerState(position = LatLng(latitude, longitude)),
            snippet = "Some stuff",
            onClick = {
                true
            },
            draggable = true
        )
    }

    companion object {
        private val POLYGON_FILL_COLOR = Color.parseColor("#ABF44336")
    }
    fun addCoordinate(latitude: Double, longitude: Double) {
        val currentClusterItems = state.value.clusterItems.toMutableList()
        val lastItemId = currentClusterItems.lastOrNull()?.id?.substringAfter("-")?.toIntOrNull() ?: 0
        val newClusterItem = ZoneClusterItem(
            id = "zone-$lastItemId",
            title = "Zone $lastItemId",
            snippet = "This is Zone $lastItemId.",
            polygonOptions = polygonOptions { add(LatLng(latitude, longitude)) }
        )
        currentClusterItems.add(newClusterItem)
        state.value = state.value.copy(clusterItems = currentClusterItems)
    }

    fun addCoordinates(coordinates: List<Pair<Double, Double>>) {
        // Add coordinates on the map, this list of of LatLong form a polygons
        if (coordinates.isEmpty()) {
            return  // Return early without performing any further actions
        }
        val currentClusterItems = state.value.clusterItems.toMutableList()
        val lastItemId = currentClusterItems.lastOrNull()?.id?.substringAfter("-")?.toIntOrNull() ?: 0

        var polygonOptions = polygonOptions {
            coordinates.forEach { (latitude, longitude) ->
                add(LatLng(latitude, longitude))
            }
            fillColor(POLYGON_FILL_COLOR)
        }
        // This detail displays when user hit on the polygon
        val newClusterItem = ZoneClusterItem(
            id = "zone-${lastItemId + 1}",
            title = "Zone ${lastItemId + 1}",
            snippet = "This is Zone ${lastItemId + 1}.",
            polygonOptions = polygonOptions
        )

        currentClusterItems.add(newClusterItem)
        state.value = state.value.copy(clusterItems = currentClusterItems)
    }
    fun addMarker(coordinate: Pair<Double, Double>)
    {
        // Add marker or point on google map
        val currentMarkers = state.value.markers?.toMutableList() ?: mutableListOf() // If markers list is null, initialize it with a new mutable list
        currentMarkers?.let {
            it.add(coordinate)
            state.value = state.value.copy(markers = it)
        }
    }
    fun clearCoordinates() {
        // Clear everything on google map (polylines, polygons, markers,etc)
        state.value = state.value.copy(clusterItems = emptyList(), markers = emptyList(), clearMap = true)
    }

    fun removeLastCoordinate() {
        // Remove the last added marker on the map
        if (state.value.markers?.isNotEmpty() == true) {
            val currentMarker = state.value.markers?.dropLast(1)
            state.value = state.value.copy( markers = currentMarker)
        }
    }

}