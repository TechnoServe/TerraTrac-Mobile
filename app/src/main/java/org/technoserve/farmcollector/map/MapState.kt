package com.tns.lab.composegooglemaps

import android.location.Location
import com.tns.lab.composegooglemaps.clusters.ZoneClusterItem
import com.google.android.gms.maps.model.MarkerOptions

data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
    var markers : List<Pair<Double, Double>>?,
    var clearMap : Boolean
)
