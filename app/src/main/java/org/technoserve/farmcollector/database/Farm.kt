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
    @ColumnInfo(name = "farmerPhoto")
    var farmerPhoto: String,

    @ColumnInfo(name = "farmerName")
    var farmerName: String,

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