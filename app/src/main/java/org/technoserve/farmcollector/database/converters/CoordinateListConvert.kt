package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
This class converts a list of latitude and longitude pair into a json string and vice versa
 *
 * Note: This converter assumes that the latitude and longitude are stored as Double values.
 * If they are stored as strings, you would need to modify this converter to handle that case.

 */
class CoordinateListConvert {
    private val gson = Gson()

    @TypeConverter
    fun fromCoordinateList(coordinates: List<Pair<Double?, Double?>>?): String? {
        return if (coordinates == null) null else gson.toJson(coordinates)
    }

    @TypeConverter
    fun toCoordinateList(data: String?): List<Pair<Double?, Double?>>? {
        if (data == null) return null
        val type = object : TypeToken<List<Pair<Double?, Double?>>>() {}.type
        return gson.fromJson(data, type)
    }
}