package org.technoserve.farmcollector.database

import android.graphics.Bitmap
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.technoserve.farmcollector.database.converters.BitmapConverter
import org.technoserve.farmcollector.database.converters.DateConverter

@Entity(tableName = "Farms")
data class Farm(
    @ColumnInfo(name = "farmerPhoto", typeAffinity = ColumnInfo.BLOB)
    @TypeConverters(BitmapConverter::class)
    val farmerPhoto: Bitmap,

    @ColumnInfo(name = "farmerName")
    val farmerName: String,

    @ColumnInfo(name = "village")
    val village: String,

    @ColumnInfo(name = "district")
    val district: String,

    @ColumnInfo(name = "purchases")
    val purchases: Float,

    @ColumnInfo(name = "size")
    val size: Float,

    @ColumnInfo(name = "latitude")
    val latitude: String,

    @ColumnInfo(name = "longitude")
    val longitude: String,

    @ColumnInfo(name = "synced")
    val synced: Boolean = false,

    @ColumnInfo(name = "scheduledForSync")
    val scheduledForSync: Boolean = false,

    @ColumnInfo(name = "createdAt")
    @TypeConverters(DateConverter::class)
    val createdAt: Long
) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Long = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Farm

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}