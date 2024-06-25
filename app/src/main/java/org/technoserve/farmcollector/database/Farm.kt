package org.technoserve.farmcollector.database

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.technoserve.farmcollector.database.converters.CoordinateListConvert
import org.technoserve.farmcollector.database.converters.DateConverter
import org.technoserve.farmcollector.database.sync.DeviceIdUtil
import org.technoserve.farmcollector.ui.screens.siteID
import  java.util.UUID

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

    @ColumnInfo(name = "remote_id")
    var remoteId: UUID = UUID.randomUUID(),

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


data class FarmDto(
    val remote_id: UUID,
    val farmer_name: String,
    val farm_village: String,
    val farm_district: String,
    val farm_size: Float,
    val latitude: String,
    val longitude: String,
    val polygon: List<Pair<Double, Double>>,
    val device_id: String,
    val collection_site: Long,
    val agent_name: String
)

fun List<Farm>.toDtoList(deviceId: String, farmDao: FarmDAO): List<FarmDto> {
    return this.map { farm ->
        val collectionSite = farmDao.getCollectionSiteById(farm.siteId)
        val agentName = collectionSite?.agentName ?: "Unknown"

        FarmDto(
            remote_id = farm.remoteId,
            farmer_name = farm.farmerName,
            farm_village = farm.village,
            farm_district = farm.district,
            farm_size = farm.size,
            latitude = farm.latitude,
            longitude = farm.longitude,
            polygon = farm.coordinates ?: emptyList(),
            device_id = deviceId,
            collection_site = farm.siteId,
            agent_name = agentName
        )
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