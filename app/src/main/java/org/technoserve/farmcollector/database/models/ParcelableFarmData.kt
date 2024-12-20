package org.technoserve.farmcollector.database.models

import android.os.Parcel
import android.os.Parcelable
/*
 * Parcelable class representing a pair of a Farm and a String representing the view.
 * Used to pass data between Activities or Fragments.
 *
 * @property farm Farm the Farm associated with this data.
 * @property view String representing the view associated with this data.
 */
data class ParcelableFarmData(val farm: Farm, val view: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Farm::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(farm, flags)
        parcel.writeString(view)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ParcelableFarmData> {
        override fun createFromParcel(parcel: Parcel): ParcelableFarmData {
            return ParcelableFarmData(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableFarmData?> {
            return arrayOfNulls(size)
        }
    }
}
