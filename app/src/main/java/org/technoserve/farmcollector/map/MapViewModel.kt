package com.codingwithmitch.composegooglemaps

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.codingwithmitch.composegooglemaps.clusters.ZoneClusterItem
import com.codingwithmitch.composegooglemaps.clusters.ZoneClusterManager
import com.codingwithmitch.composegooglemaps.clusters.calculateCameraViewPoints
import com.codingwithmitch.composegooglemaps.clusters.getCenterOfPolygon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
            clusterItems = listOf(
                ZoneClusterItem(
                    id = "zone-1",
                    title = "Zone 1",
                    snippet = "This is Zone 1.",
                    polygonOptions = polygonOptions {
                        add(LatLng(7.58556, -4.71747))
                        add(LatLng(7.585483, -4.717494))
                        add(LatLng(7.585494, -4.717537))
                        add(LatLng(7.585521, -4.717598))
                        add(LatLng(7.58557, -4.717618))
                        add(LatLng(7.585590967078138, -4.71761899459054))
                        add(LatLng(7.585605, -4.717637))
                        add(LatLng(7.585612, -4.717692))
                        add(LatLng(7.585612, -4.717733))
                        add(LatLng(7.585625, -4.717748))
                        add(LatLng(7.585642, -4.717774))
                        add(LatLng(7.585634, -4.717788))
                        add(LatLng(7.585632, -4.717811))
                        add(LatLng(7.585637, -4.717841))
                        add(LatLng(7.585636, -4.71785))
                        add(LatLng(7.585654, -4.717874))
                        add(LatLng(7.585691, -4.71788))
                        add(LatLng(7.585705, -4.717887))
                        add(LatLng(7.585714, -4.717894))
                        add(LatLng(7.585742, -4.717898))
                        add(LatLng(7.585777, -4.717921))
                        add(LatLng(7.585797, -4.71793))
                        add(LatLng(7.585823, -4.717953))
                        add(LatLng(7.585878, -4.717988))
                        add(LatLng(7.585907, -4.718009))
                        add(LatLng(7.585987, -4.718012))
                        add(LatLng(7.586017, -4.718039))
                        add(LatLng(7.586021, -4.718065))
                        add(LatLng(7.586092, -4.718089))
                        add(LatLng(7.5861, -4.718092))
                        add(LatLng(7.586163, -4.718116))
                        add(LatLng(7.586195, -4.718144))
                        add(LatLng(7.586234, -4.718145))
                        add(LatLng(7.586290625510939, -4.7181546595656))
                        add(LatLng(7.586381161062833, -4.718148328408123))
                        add(LatLng(7.586381390342398, -4.718148312374589))
                        add(LatLng(7.586401, -4.718155))
                        add(LatLng(7.58647881972442, -4.718183539284703))
                        add(LatLng(7.586525154218799, -4.718201628779085))
                        add(LatLng(7.586497, -4.71815))
                        add(LatLng(7.586494, -4.718087))
                        add(LatLng(7.586492, -4.718039))
                        add(LatLng(7.586487, -4.718034))
                        add(LatLng(7.586447, -4.718021))
                        add(LatLng(7.586439, -4.718024))
                        add(LatLng(7.586435, -4.71802))
                        add(LatLng(7.586451, -4.718016))
                        add(LatLng(7.586468, -4.717963))
                        add(LatLng(7.586572, -4.717922))
                        add(LatLng(7.58658, -4.717851))
                        add(LatLng(7.586622, -4.717807))
                        add(LatLng(7.586692, -4.717829))
                        add(LatLng(7.586746, -4.717851))
                        add(LatLng(7.586793, -4.717824))
                        add(LatLng(7.586806, -4.717741))
                        add(LatLng(7.586867, -4.717683))
                        add(LatLng(7.586909, -4.717659))
                        add(LatLng(7.58698, -4.717664))
                        add(LatLng(7.587016, -4.717628))
                        add(LatLng(7.587021, -4.717554))
                        add(LatLng(7.587013, -4.71749))
                        add(LatLng(7.586986, -4.717433))
                        add(LatLng(7.586956, -4.717409))
                        add(LatLng(7.586931, -4.717369))
                        add(LatLng(7.586912, -4.717338))
                        add(LatLng(7.586889, -4.717301))
                        add(LatLng(7.586831, -4.717257))
                        add(LatLng(7.586792, -4.717264))
                        add(LatLng(7.586783, -4.717279))
                        add(LatLng(7.586761, -4.717274))
                        add(LatLng(7.586717, -4.717237))
                        add(LatLng(7.586654, -4.717194))
                        add(LatLng(7.586631, -4.717192))
                        add(LatLng(7.586595, -4.717182))
                        add(LatLng(7.586497, -4.717198))
                        add(LatLng(7.586463, -4.717195))
                        add(LatLng(7.586442, -4.717185))
                        add(LatLng(7.58642, -4.717156))
                        add(LatLng(7.586394, -4.717112))
                        fillColor(POLYGON_FILL_COLOR)
                    }
                )
            )
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
                System.out.println("Mitchs_: Cannot be clicked")
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
//        val lastItemId = currentClusterItems.last().id.toInt() + 1
        val idString = currentClusterItems.last().id
        val numericPart = idString.substringBefore("-")[1] // Assuming "-" separates numeric and non-numeric parts
        val lastItemId = numericPart.toInt() + 1
        val newClusterItem = ZoneClusterItem(
            id = "zone-$lastItemId",
            title = "Zone $lastItemId",
            snippet = "This is Zone $lastItemId.",
            polygonOptions = polygonOptions { add(LatLng(latitude, longitude)) }
        )
        currentClusterItems.add(newClusterItem)
        state.value = state.value.copy(clusterItems = currentClusterItems)
    }

}