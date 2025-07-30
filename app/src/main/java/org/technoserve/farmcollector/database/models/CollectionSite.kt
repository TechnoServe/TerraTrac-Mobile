package org.technoserve.farmcollector.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.technoserve.farmcollector.database.converters.DateConverter
import org.technoserve.farmcollector.database.mappers.CommodityConverter

/**
 *
 * This class represents a collection site, with additional fields for agent name, phone number, email, village,
 * district, and timestamps for created and updated at.
 *
 * The @Entity annotation is used to specify that this class is an entity that will be mapped to a table in the
 * database. The @ColumnInfo annotation is used to specify the column names for each field in the table.
 */
@Entity(tableName = "CollectionSites")
data class CollectionSite(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "agentName")
    var agentName: String,
    @ColumnInfo(name = "phoneNumber")
    var phoneNumber: String,
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "village")
    var village: String,
    @ColumnInfo(name = "district")
    var district: String,
    @ColumnInfo(name = "createdAt")
    @TypeConverters(DateConverter::class)
    val createdAt: Long,
    @ColumnInfo(name = "updatedAt")
    @TypeConverters(DateConverter::class)
    var updatedAt: Long,
    @ColumnInfo(name = "commodity")
    @TypeConverters(CommodityConverter::class)
    var commodity: Commodity = Commodity.COFFEE,
) {
    @PrimaryKey(autoGenerate = true)
    var siteId: Long = 0L
}

enum class Commodity(val displayName: String) {
    COFFEE("coffee"),
    COCOA("cocoa");

    companion object {
        fun fromDisplayName(name: String): Commodity {
            return Commodity.entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
                ?: COFFEE // default
        }

        fun displayNames(): List<String> {
            return Commodity.entries.map { it.displayName }
        }
    }
}

