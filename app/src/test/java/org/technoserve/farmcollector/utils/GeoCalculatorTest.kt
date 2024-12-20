package org.technoserve.farmcollector.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import org.junit.Assert.*
import org.junit.Test

class GeoCalculatorTest{

    @Test
    fun `calculateArea returns 0_0 for null polygon`() {
        val result = GeoCalculator.calculateArea(null)
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `calculateArea returns 0_0 for polygon with less than 3 points`() {
        val polygon = listOf(
            Pair(0.0, 0.0),
            Pair(1.0, 1.0)
        )
        val result = GeoCalculator.calculateArea(polygon)
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `calculateArea calculates correct area for a valid polygon`() {
        // Define a simple square polygon (coordinates in latitude and longitude)
        val polygon = listOf(
            Pair(0.0, 0.0),
            Pair(0.0, 1.0),
            Pair(1.0, 1.0),
            Pair(1.0, 0.0),
            Pair(0.0, 0.0) // Closing the polygon
        )

        // Mock the area calculation (optional if you use SphericalUtil directly)
        val expectedAreaInSquareMeters = SphericalUtil.computeArea(
            polygon.map { LatLng(it.first, it.second) }
        )
        val expectedAreaInHectares = expectedAreaInSquareMeters / 10000.0

        // Call the function
        val result = GeoCalculator.calculateArea(polygon)

        // Verify the result
        assertEquals(String.format("%.9f", expectedAreaInHectares).toDouble(), result, 1e-9)
    }

    @Test
    fun `calculateArea formats result to 9 decimal places`() {
        // Define a simple polygon
        val polygon = listOf(
            Pair(0.0, 0.0),
            Pair(0.0, 2.0),
            Pair(2.0, 2.0),
            Pair(2.0, 0.0),
            Pair(0.0, 0.0) // Closing the polygon
        )

        // Call the function
        val result = GeoCalculator.calculateArea(polygon)

        // Verify that the result is formatted to 9 decimal places
        val formattedResult = String.format("%.9f", result)
        assertEquals(9, formattedResult.split(".")[1].length)
    }
}