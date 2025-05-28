package org.technoserve.farmcollector.database.mappers

import androidx.room.TypeConverter
import org.technoserve.farmcollector.database.models.Commodity

class CommodityConverter {
    @TypeConverter
    fun fromCommodity(commodity: Commodity): String {
        return commodity.displayName
    }

    @TypeConverter
    fun toCommodity(value: String): Commodity {
        return Commodity.fromDisplayName(value)
    }
}
