package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// This class converts long list of latitude and longitude pair into json format so that can be kept in database easily!
class CoordinateListConvert {
    @TypeConverter
    fun fromCoordinates(coordinates: List<Pair<Double, Double>>?): String {
        if (coordinates == null) {
            return ""
        }
        val gson = Gson()
        return gson.toJson(coordinates)
    }

    @TypeConverter
    fun toCoordinates(data: String): List<Pair<Double, Double>> {
        if (data.isEmpty()) {
            return emptyList()
        }
        val gson = Gson()
        val listType = object : TypeToken<List<Pair<Double, Double>>>() {}.type
        return gson.fromJson(data, listType)
    }
}