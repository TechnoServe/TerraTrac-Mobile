package org.technoserve.farmcollector.database.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import org.technoserve.farmcollector.database.converters.AccuracyListConvert
import org.technoserve.farmcollector.database.converters.CoordinateListConvert
import org.technoserve.farmcollector.database.converters.DateConverter
import java.util.UUID

/**
This class represents a Farm in the database. It includes details about the farmer
and the farm's characteristics.

@Entity: Indicates that this class is an entity that should be persisted in the database.
tableName: Specifies the name of the table in the database where this entity will be stored.
foreignKeys: Specifies the foreign key relationships with other entities. In this case, it references
the CollectionSite entity using the siteId column.
Parcelize: Indicates that this class can be parcelled, allowing it to be transferred between activities
or fragments using Android's Parcelable interface.
TypeConverters: Specifies the converters that will be used to convert certain data types to and from
the database.

 */
@Parcelize
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
@TypeConverters(CoordinateListConvert::class, AccuracyListConvert::class, DateConverter::class)
data class Farm(
    @ColumnInfo(name = "siteId") var siteId: Long = 0L,
    @ColumnInfo(name = "remote_id") var remoteId: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "farmerPhoto") var farmerPhoto: String = "",
    @ColumnInfo(name = "farmerName") var farmerName: String = "",
    @ColumnInfo(name = "memberId") var memberId: String = "",
    @ColumnInfo(name = "village") var village: String = "",
    @ColumnInfo(name = "district") var district: String = "",
    @ColumnInfo(name = "purchases") var purchases: Float? = null,
    @ColumnInfo(name = "size") var size: Float = 0f,
    @ColumnInfo(name = "latitude") var latitude: String = "",
    @ColumnInfo(name = "longitude") var longitude: String = "",
    @ColumnInfo(name = "coordinates") var coordinates: List<Pair<Double?, Double?>>? = emptyList(),
    @ColumnInfo(name = "accuracyArray") var accuracyArray: List<Float?>? = emptyList(),
    @ColumnInfo(name = "synced", defaultValue = "0") val synced: Boolean = false,
    @ColumnInfo(name = "scheduledForSync", defaultValue = "0") val scheduledForSync: Boolean = false,
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updatedAt") var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "needsUpdate", defaultValue = "0") var needsUpdate: Boolean = false,
    @PrimaryKey(autoGenerate = true) var id: Long = 0L
) : Parcelable


