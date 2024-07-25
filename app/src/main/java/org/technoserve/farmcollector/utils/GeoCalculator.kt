package org.technoserve.farmcollector.utils

import android.annotation.SuppressLint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.math.BigDecimal
import java.math.RoundingMode

object GeoCalculator {
    // The function to calculate the Area using the captured Polygons
    @SuppressLint("DefaultLocale")
    fun calculateArea(polygon: List<Pair<Double, Double>>?): Double {
        if (polygon == null || polygon.size < 3) {
            return 0.0
        }
        // Convert the polygon vertices to LatLng objects expected by SphericalUtil
        val latLngList = polygon.map { LatLng(it.first, it.second) }

        // Calculate the area in square meters
        val areaInSquareMeters = SphericalUtil.computeArea(latLngList)

        // Convert area to hectares (1 hectare = 10,000 square meters)
        val areaInHectares = areaInSquareMeters / 10000.0

//        // Format the result to 6 decimal places
//        return String.format("%.6f", areaInHectares).toDouble()
        // Format the result to 6 decimal places and ensure the correct format
        return String.format("%.6f", areaInHectares).replace(',', '.').toDouble()
//
//        // Round the result to 5 decimal places
//        val roundedAreaInHectares = BigDecimal(areaInHectares).setScale(5, RoundingMode.HALF_UP).toDouble()
//
//        return roundedAreaInHectares
    }
}
