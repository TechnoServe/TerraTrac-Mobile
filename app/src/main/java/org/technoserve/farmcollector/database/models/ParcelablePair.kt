package org.technoserve.farmcollector.database.models

import android.os.Parcel
import android.os.Parcelable
/**
 * This class is used to transfer data between activities/fragments and Parcelable objects.
 *
 * @param first First value
 * @param second Second value
 *
 *
 */
data class ParcelablePair(val first: Double, val second: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(first)
        parcel.writeDouble(second)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelablePair> {
        override fun createFromParcel(parcel: Parcel): ParcelablePair {
            return ParcelablePair(parcel)
        }

        override fun newArray(size: Int): Array<ParcelablePair?> {
            return arrayOfNulls(size)
        }
    }
}
