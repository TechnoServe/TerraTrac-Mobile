package org.technoserve.farmcollector.viewmodels


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import org.technoserve.farmcollector.database.models.Farm
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
    private val _showClearMapDialog: MutableState<Boolean> = mutableStateOf(false)
    val showClearMapDialog: MutableState<Boolean> = _showClearMapDialog


    fun showClearDialog() {
        _showClearMapDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearMapDialog.value = false
    }

    fun clearMap(webView: WebView ?) {

        if (webView == null) {
            Log.e("MapViewModel", "WebView is null! Cannot execute JavaScript.")
            return
        }

        Log.d("MapViewModel", "Clearing map...")

        Handler(Looper.getMainLooper()).post {
            Handler(Looper.getMainLooper()).post {
                webView.evaluateJavascript(
                    """ 
        (function() {
            if (typeof window.clearMapConfirmed === "function") {
                window.clearMapConfirmed();
                console.log("clearMapConfirmed() executed successfully");
                return "success";
            } else {
                console.error("clearMapConfirmed() is not defined");
                return "error: function not found";
            }
        })();
        """
                ) { result ->
                    Log.d("MapViewModel", "JavaScript execution result: $result")
                }
                Log.d("MapViewModel", "Clear map request sent to WebView")
            }
        }
            _showClearMapDialog.value = false
    }


    private val _showConfirmDialog = mutableStateOf(false)
    val showConfirmDialog: MutableState<Boolean> = _showConfirmDialog


    // Show Dialogs
    fun showConfirmPolygonDialog() {
        _showConfirmDialog.value = true
    }

    fun dismissConfirmPolygonDialog() {
        _showConfirmDialog.value = false
    }


    private val _showInvalidPolygonDialog = mutableStateOf(false)
    val showInvalidPolygonDialog: MutableState<Boolean> = _showInvalidPolygonDialog

    fun showInvalidPolygonDialog() {
        _showInvalidPolygonDialog.value = true
    }

    fun dismissInvalidPolygonDialog() {
        _showInvalidPolygonDialog.value = false
    }
    
    fun confirmFinishPolygon(webView: WebView?) {
        Log.d("MapViewModel", "confirmFinishPolygon() called")

        if (webView == null) {
            Log.e("MapViewModel", "WebView is null! Cannot execute JavaScript.")
            return
        }

        Handler(Looper.getMainLooper()).post {
            Log.d("MapViewModel", "Sending JavaScript execution request...")

            webView.evaluateJavascript(
                """ 
            (function() {
                if (typeof window.stopCaptureConfirmed === "function") {
                    console.log("Calling stopCaptureConfirmed()");
                    window.stopCaptureConfirmed();
                    return "success";
                } else {
                    console.error("stopCaptureConfirmed() is not defined");
                    return "error: function not found";
                }
            })();
            """
            ) { result ->
                Log.d("MapViewModel", "JavaScript execution result: $result")
            }

            Log.d("MapViewModel", "JavaScript execution request sent to WebView.")
        }

        dismissConfirmPolygonDialog()
        Log.d("MapViewModel", "confirmFinishPolygon() finished execution")
    }


    private val _showAlertDialog = mutableStateOf(false)
    val showAlertDialog: MutableState<Boolean> = _showAlertDialog

    fun showAlertDialog() {
        _showAlertDialog.value = true
    }

    fun dismissAlertDialog() {
        _showAlertDialog.value = false
    }







    private val _plotData = MutableStateFlow(Farm()) // colors Ensure non-null default value
    val plotData: StateFlow<Farm> = _plotData.asStateFlow()

    fun updatePlotData(
        siteId: Long? = null, // Allow siteId to be updated
        coordinates: List<Pair<Double, Double>>? = null,
        latitude: String? = null,
        longitude: String? = null,
        size: Float? = null,
        accuracyArray: List<Float>? = null,
        farmerName: String? = null,
        memberId: String? = null,
        farmerPhoto: String? = null,
        village: String? = null,
        district: String? = null
    ) {
        _plotData.value = _plotData.value.copy(
            siteId = siteId ?: _plotData.value.siteId, // colors Update siteId if provided
            coordinates = coordinates ?: _plotData.value.coordinates,
            latitude = latitude ?: _plotData.value.latitude,
            longitude = longitude ?: _plotData.value.longitude,
            size = size ?: _plotData.value.size,
            accuracyArray = accuracyArray ?: _plotData.value.accuracyArray,
            farmerName = farmerName ?: _plotData.value.farmerName,
            memberId = memberId ?: _plotData.value.memberId,
            farmerPhoto = farmerPhoto ?: _plotData.value.farmerPhoto,
            village = village ?: _plotData.value.village,
            district = district ?: _plotData.value.district
        )
    }

    fun submitForm() {
        // colors Perform form submission logic here (e.g., API call, database update)

        // colors Reset the form after successful submission
        _plotData.value = Farm() // Clears all values
    }


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

class MapViewModelFactory @Inject constructor() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
