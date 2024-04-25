package org.technoserve.farmcollector.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.technoserve.farmcollector.database.converters.CoordinateListConvert
import org.technoserve.farmcollector.database.converters.DateConverter

@Entity(
    tableName = "Farms",
    foreignKeys = [
        ForeignKey(
            entity = CollectionSite::class,
            parentColumns = ["siteId"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(CoordinateListConvert::class)
data class Farm(
    @ColumnInfo(name = "siteId")
    var siteId: Long,

    @ColumnInfo(name = "farmerPhoto")
    var farmerPhoto: String,

    @ColumnInfo(name = "farmerName")
    var farmerName: String,

    @ColumnInfo(name = "memberId")
    var memberId: String,

    @ColumnInfo(name = "village")
    var village: String,

    @ColumnInfo(name = "district")
    var district: String,

    @ColumnInfo(name = "purchases")
    var purchases: Float,

    @ColumnInfo(name = "size")
    var size: Float,

    @ColumnInfo(name = "latitude")
    var latitude: String,

    @ColumnInfo(name = "longitude")
    var longitude: String,

    @ColumnInfo(name = "coordinates")
    var coordinates: List<Pair<Double, Double>>?,

    @ColumnInfo(name = "synced")
    val synced: Boolean = false,

    @ColumnInfo(name = "scheduledForSync")
    val scheduledForSync: Boolean = false,

    @ColumnInfo(name = "createdAt")
    @TypeConverters(DateConverter::class)
    val createdAt: Long,

    @ColumnInfo(name = "updatedAt")
    @TypeConverters(DateConverter::class)
    var updatedAt: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Farm

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

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
    var updatedAt: Long
) {
    @PrimaryKey(autoGenerate = true)
    var siteId: Long = 0L
}