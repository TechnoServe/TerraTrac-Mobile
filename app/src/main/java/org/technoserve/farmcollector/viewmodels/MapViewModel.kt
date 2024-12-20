package org.technoserve.farmcollector.viewmodels


import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.MapType
import com.google.maps.android.ktx.model.polygonOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.technoserve.farmcollector.database.models.map.MapState
import org.technoserve.farmcollector.database.models.map.ZoneClusterItem
import org.technoserve.farmcollector.database.helpers.map.ZoneClusterManager
import org.technoserve.farmcollector.utils.GeoCalculator
import org.technoserve.farmcollector.utils.map.calculateCameraViewPoints
import org.technoserve.farmcollector.utils.map.getCenterOfPolygon
import javax.inject.Inject


/**
 * ViewModel for the Map screen.
 *
 * Contains state and methods to manage the map, including the location, markers, clustering, and area calculations.
 *
 */

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    val state: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = null,
            markers = mutableListOf(),
            clusterItems = emptyList(),
            clearMap = false,
            mapType = MapType.NORMAL,
            onMapTypeChange = {}
        )
    )

    // Properties for coordinates and areas
    private val _coordinates = MutableLiveData<List<Pair<Double, Double>>>()
    val coordinates: LiveData<List<Pair<Double, Double>>>
        get() = _coordinates

    private val _calculatedArea = MutableLiveData<Double>()
    private val _size = MutableStateFlow("")
    val size: StateFlow<String> = _size.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()


    // Method to set coordinates and calculated area
    fun calculateArea(coordinates: List<Pair<Double, Double>>?): Double {
        if(coordinates != null)
        {
            _coordinates.value = coordinates as List<Pair<Double, Double>>
            val area = GeoCalculator.calculateArea(coordinates)
            _calculatedArea.value = area
            return area
        }
        return 0.0
    }

    // Method to show dialog for choosing area
    fun showAreaDialog(calculatedArea: String, enteredArea: String) {
        val areaNum = enteredArea.toDoubleOrNull()
        if (areaNum != null) {
            _calculatedArea.value = calculatedArea.toDouble()
            _size.value = enteredArea
            _showDialog.value = true
        } else {
            _size.value = "Invalid input."
        }
    }

    fun dismissDialog() {
        _showDialog.value = false
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
        try {
            val latLongs = state.value.clusterItems.map { it.polygonOptions }
                .map { it.points.map { LatLng(it.latitude, it.longitude) } }.flatten()
            return latLongs.calculateCameraViewPoints().getCenterOfPolygon()
        } catch (e: IllegalStateException) {
            println("Cannot calculate the view coordinates of nothing. : ${e.message}")
        }
        return LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
    }


    companion object {
        private val POLYGON_FILL_COLOR = Color.parseColor("#ABF44336")
    }

    fun addCoordinate(latitude: Double, longitude: Double) {
        val currentClusterItems = state.value.clusterItems.toMutableList()
        val lastItemId =
            currentClusterItems.lastOrNull()?.id?.substringAfter("-")?.toIntOrNull() ?: 0
        val newClusterItem = ZoneClusterItem(
            id = "zone-$lastItemId",
            title = "Coords:",
            snippet = "($latitude, $longitude)",
            polygonOptions = polygonOptions { add(LatLng(latitude, longitude)) }
        )
        currentClusterItems.add(newClusterItem)
        state.value = state.value.copy(clusterItems = currentClusterItems)
    }

    fun addCoordinates(coordinates: List<Pair<Double?, Double?>>) {
        // Add coordinates on the map, this list of of LatLong form a polygons
        if (coordinates.isEmpty()) {
            return  // Return early without performing any further actions
        }
        val currentClusterItems = state.value.clusterItems.toMutableList()
        val lastItemId =
            currentClusterItems.lastOrNull()?.id?.substringAfter("-")?.toIntOrNull() ?: 0

        val polygonOptions = polygonOptions {
            coordinates.forEach { (latitude, longitude) ->
                if (latitude != null && longitude != null)
                    add(LatLng(latitude, longitude))
            }
            fillColor(POLYGON_FILL_COLOR)
        }
        // This detail displays when user hit on the polygon
        val newClusterItem = ZoneClusterItem(
            id = "zone-${lastItemId + 1}",
            title = "Central Point",
            snippet = "(Lat: ${coordinates[0].first}, Long: ${coordinates[0].second})",
            polygonOptions = polygonOptions
        )

        currentClusterItems.add(newClusterItem)
        state.value = state.value.copy(clusterItems = currentClusterItems)
    }

    fun addMarker(coordinate: Pair<Double, Double>) {
        // Add marker or point on google map
        val currentMarkers = state.value.markers?.toMutableList()
            ?: mutableListOf() // If markers list is null, initialize it with a new mutable list
        currentMarkers.let {
            it.add(coordinate)
            state.value = state.value.copy(markers = it)
        }

        addCoordinate(coordinate.first, coordinate.second)
    }


    fun clearCoordinates() {
        // Clear everything on google map (poly-lines, polygons, markers,etc)
        try {
            state.value =
                state.value.copy(clusterItems = emptyList(), markers = emptyList(), clearMap = true)
        } catch (e: IllegalStateException) {
            println("Can't : ${e.message}")
        }
    }

    fun removeLastCoordinate() {
        // Remove the last added marker on the map
        if (state.value.markers?.isNotEmpty() == true) {
            val currentMarker = state.value.markers?.dropLast(1)
            state.value = state.value.copy(markers = currentMarker)
        }
    }

    fun onMapTypeChange(mapType: MapType) {
        state.value = state.value.copy(mapType = mapType)
    }

}