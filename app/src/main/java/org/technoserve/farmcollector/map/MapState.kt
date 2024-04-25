package org.technoserve.farmcollector.map

import android.location.Location
import com.google.maps.android.compose.MapType

data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
    var markers : List<Pair<Double, Double>>?,
    var clearMap : Boolean,
    var mapType: MapType,
    val onMapTypeChange: (MapType) -> Unit,
)
