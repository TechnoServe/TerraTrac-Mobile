package org.technoserve.farmcollector.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.technoserve.farmcollector.database.converters.DateConverter
/*
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
) {
    @PrimaryKey(autoGenerate = true)
    var siteId: Long = 0L
}


