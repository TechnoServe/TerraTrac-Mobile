package org.technoserve.farmcollector.database.converters



import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinateListConvertTest {

    private val converter = CoordinateListConvert()


    @Test
    fun `fromCoordinates with null input returns empty string`() {
        val result = converter.fromCoordinateList(null)
        assertEquals(null, result)
    }

    @Test
    fun `fromCoordinates with empty list returns JSON array`() {
        val emptyList = emptyList<Pair<Double, Double>>()
        val result = converter.fromCoordinateList(emptyList)
        assertEquals("[]", result)
    }

    @Test
    fun `fromCoordinates with single coordinate pair`() {
        val coordinates = listOf(Pair(45.0, -122.0))
        val result = converter.fromCoordinateList(coordinates)

        // Using Gson to parse back and verify the structure
        val gson = Gson()
        val listType = object : TypeToken<List<Pair<Double, Double>>>() {}.type
        val parsedResult: List<Pair<Double, Double>> = gson.fromJson(result, listType)

        assertEquals(1, parsedResult.size)

        // Use a delta to compare floating-point numbers
        val delta = 0.0001
        assertEquals(45.0, parsedResult[0].first, delta)
        assertEquals(-122.0, parsedResult[0].second, delta)
    }


    @Test
    fun `fromCoordinates with multiple coordinate pairs`() {
        val coordinates = listOf(
            Pair(45.0, -122.0),
            Pair(47.0, -123.0),
            Pair(46.0, -121.0)
        )
        val result = converter.fromCoordinateList(coordinates)
        val gson = Gson()
        val listType = object : TypeToken<List<Pair<Double, Double>>>() {}.type
        val parsedResult: List<Pair<Double, Double>> = gson.fromJson(result, listType)

        assertEquals(3, parsedResult.size)
        assertEquals(coordinates, parsedResult)
    }

    @Test
    fun `toCoordinates with empty string returns empty list`() {
        val result = converter.toCoordinateList("")
        if (result != null) {
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `toCoordinates with empty JSON array returns empty list`() {
        val result = converter.toCoordinateList("[]")
        if (result != null) {
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `toCoordinates with single coordinate pair`() {
        val json = """[{"first":45.0,"second":-122.0}]"""
        val result = converter.toCoordinateList(json)

        if (result != null) {
            assertEquals(1, result.size)
        }
        assertEquals(45.0, result?.get(0)?.first)
        assertEquals(-122.0, result?.get(0)?.second)
    }

    @Test
    fun `toCoordinates with multiple coordinate pairs`() {
        val json = """[
            {"first":45.0,"second":-122.0},
            {"first":47.0,"second":-123.0},
            {"first":46.0,"second":-121.0}
        ]"""
        val result = converter.toCoordinateList(json)

        if (result != null) {
            assertEquals(3, result.size)
        }
        assertEquals(45.0, result?.get(0)?.first)
        assertEquals(-122.0, result?.get(0)?.second)
        assertEquals(47.0, result?.get(1)?.first)
        assertEquals(-123.0, result?.get(1)?.second)
        assertEquals(46.0, result?.get(2)?.first)
        assertEquals(-121.0, result?.get(2)?.second)
    }
}