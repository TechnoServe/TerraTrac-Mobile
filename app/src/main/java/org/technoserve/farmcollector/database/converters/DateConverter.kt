package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter
import java.util.Date


/**

This class converts a long value representing a date into a Date object and vice versa
 */

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}