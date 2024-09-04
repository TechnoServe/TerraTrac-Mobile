package org.technoserve.farmcollector.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import org.technoserve.farmcollector.database.converters.CoordinateListConvert
import org.technoserve.farmcollector.database.converters.DateConverter
import org.technoserve.farmcollector.ui.screens.ParcelablePair
import java.util.UUID

@Entity(
    tableName = "Farms",
    foreignKeys = [
        ForeignKey(
            entity = CollectionSite::class,
            parentColumns = ["siteId"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
@Parcelize
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
    var purchases: Float?,
    @ColumnInfo(name = "size")
    var size: Float,
    @ColumnInfo(name = "latitude")
    var latitude: String,
    @ColumnInfo(name = "longitude")
    var longitude: String,
    @ColumnInfo(name = "coordinates")
    var coordinates: List<Pair<Double?, Double?>>?,
    @ColumnInfo(name = "synced", defaultValue = "0")
    val synced: Boolean = false,
    @ColumnInfo(name = "scheduledForSync",defaultValue = "0")
    val scheduledForSync: Boolean = false,
    @ColumnInfo(name = "createdAt")
    @TypeConverters(DateConverter::class)
    val createdAt: Long,
    @ColumnInfo(name = "updatedAt")
    @TypeConverters(DateConverter::class)
    var updatedAt: Long,
    @ColumnInfo(name = "needsUpdate",defaultValue = "0")
    var needsUpdate: Boolean = false,
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Farm

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        UUID.fromString(parcel.readString()),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readFloat(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(ParcelablePair.CREATOR)?.map { Pair(it.first, it.second) },
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
        id = parcel.readLong()
    }

    override fun  describeContents(): Int = 0

    companion object : Parceler<Farm> {

        override fun Farm.write(parcel: Parcel, flags: Int) {
            parcel.writeLong(siteId)
            parcel.writeString(remoteId.toString())
            parcel.writeString(farmerPhoto)
            parcel.writeString(farmerName)
            parcel.writeString(memberId)
            parcel.writeString(village)
            parcel.writeString(district)
            parcel.writeValue(purchases)
            parcel.writeFloat(size)
            parcel.writeString(latitude)
            parcel.writeString(longitude)
            parcel.writeTypedList(coordinates?.map { it.first?.let { it1 -> it.second?.let { it2 ->
                ParcelablePair(it1,
                    it2
                )
            } } })
            parcel.writeByte(if (synced) 1 else 0)
            parcel.writeByte(if (scheduledForSync) 1 else 0)
            parcel.writeLong(createdAt)
            parcel.writeLong(updatedAt)
            parcel.writeByte(if (needsUpdate) 1 else 0)
            parcel.writeLong(id)
        }

        override fun create(parcel: Parcel): Farm {
            return Farm(parcel)
        }
    }
}



data class CollectionSiteDto(
    val local_cs_id: Long,
    val name: String,
    val agent_name: String,
    val phone_number: String?,
    val email: String?,
    val village: String?,
    val district: String?
)

data class FarmDetailDto(
    val remote_id: String,
    val farmer_name: String,
    val member_id: String,
    val village: String,
    val district: String,
    val size: Float,
    val latitude: Double,
    val longitude: Double,
    val coordinates: List<List<Double?>>? // Converted from `polygon`
)

data class DeviceFarmDto(
    val device_id: String,
    val collection_site: CollectionSiteDto,
    val farms: List<FarmDetailDto>
)


fun List<Farm>.toDeviceFarmDtoList(deviceId: String, farmDao: FarmDAO): List<DeviceFarmDto> {
    return this.groupBy { it.siteId } // Group by siteId
        .mapNotNull { (siteId, farms) ->
            val collectionSite = farmDao.getCollectionSiteById(siteId) ?: return@mapNotNull null

            // Map the collection site details
            val collectionSiteDto = CollectionSiteDto(
                local_cs_id = collectionSite.siteId,
                name = collectionSite.name,
                agent_name = collectionSite.agentName ?: "Unknown",
                phone_number = collectionSite.phoneNumber,
                email = collectionSite.email,
                village = collectionSite.village,
                district = collectionSite.district
            )

            // Map the farms
            val farmDtos = farms.mapNotNull { farm ->
                farm.remoteId?.let { remoteId ->
                    // Ensure latitude and longitude are not empty or null before parsing
                    val latitude = farm.latitude.takeIf { it.isNotBlank() }?.toDoubleOrNull() ?: 0.0
                    val longitude = farm.longitude.takeIf { it.isNotBlank() }?.toDoubleOrNull() ?: 0.0

                    FarmDetailDto(
                        remote_id = remoteId.toString(),
                        farmer_name = farm.farmerName,
                        member_id = farm.memberId,
                        village = farm.village,
                        district = farm.district,
                        size = farm.size,
                        latitude = latitude,
                        longitude = longitude,
                        coordinates = farm.coordinates?.map { listOf(it.first, it.second) } ?: emptyList() // Convert coordinate pairs
                    )
                }
            }

            DeviceFarmDto(
                device_id = deviceId,
                collection_site = collectionSiteDto,
                farms = farmDtos
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
    var updatedAt: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var siteId: Long = 0L
}
