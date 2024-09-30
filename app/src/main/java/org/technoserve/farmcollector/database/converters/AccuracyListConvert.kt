package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter

class AccuracyListConvert {

    @TypeConverter
    fun fromAccuracyList(value: List<Float?>?): String? {
        // Convert the list to a string representation, enclosed in brackets
        return value?.let {
            "[" + it.joinToString(separator = ",") { it?.toString() ?: "null" } + "]"
        }
    }

    @TypeConverter
    fun toAccuracyList(value: String?): List<Float?>? {
        // Remove the brackets and split the string into a list
        return value?.removePrefix("[")?.removeSuffix("]")?.split(",")?.map {
            it.trim().toFloatOrNull()
        }
    }
}
