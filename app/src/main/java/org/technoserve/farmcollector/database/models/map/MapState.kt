package org.technoserve.farmcollector.database.models.map

import android.location.Location
import com.google.maps.android.compose.MapType
/*
 * State object for the MapScreen.
 *
 * @param lastKnownLocation The last known location of the user.
 * @param clusterItems The list of ZoneClusterItem objects representing the zones.
 * @param markers The list of markers for the zones.
 * @param clearMap A boolean indicating whether the map should be cleared when the screen is navigated away from.
 * @param mapType The type of the map to be created and updated
 * @param onMapTypeChange A boolean indicating whether the map should be cleared when the screen is navigated away
 */
data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
    var markers: List<Pair<Double, Double>>?,
    var clearMap: Boolean,
    var mapType: MapType,
    val onMapTypeChange: (MapType) -> Unit,
)
