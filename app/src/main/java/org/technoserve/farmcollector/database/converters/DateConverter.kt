package org.technoserve.farmcollector.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * This class converts a long value representing a date into a Date object and vice versa
 * This is used in the Farm entity to store and retrieve dates in the database.
 *
 * The @TypeConverter annotation is used to tell Room that this class is responsible for converting dates.
 * The toDate and fromDate methods are used to convert between the long value and the Date object.
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