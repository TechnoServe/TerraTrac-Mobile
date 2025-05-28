package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter


/**
 *  @TypeConverter
 * fun fromAccuracyList(value: List<Float?>?): String?
 * This Method is used to convert Accuracy list values into list to a string representation, enclosed in brackets and Remove the brackets and split the string into a list
 * @TypeConverter
 *  fun toAccuracyList(value: String?): List<Float?>?
 *  This method is used to convert the string representation of accuracy list back to a list of Float?
 *  @param value - the string representation of accuracy list
 *  @return List<Float?>? - the list of accuracy values
 *  Note: If the string representation is empty or null, it returns an empty list.
 *
 */

class AccuracyListConvert {
    @TypeConverter
    fun fromAccuracyList(value: List<Float?>?): String? {
        return value?.let {
            "[" + it.joinToString(separator = ",") { it?.toString() ?: "null" } + "]"
        }
    }
    @TypeConverter
    fun toAccuracyList(value: String?): List<Float?>? {
        if (value == "[]") {
            return emptyList()
        }
        return value?.removePrefix("[")?.removeSuffix("]")?.split(",")?.map {
            it.trim().toFloatOrNull()
        }
    }
}