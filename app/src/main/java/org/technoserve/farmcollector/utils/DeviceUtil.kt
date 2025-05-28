package org.technoserve.farmcollector.utils


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *  This function is used to get the advertisingId or AndroidId for a specific device
 *  It uses Google's AdvertisingIdClient to get the advertisingId
 *  If an error occurs while getting the advertisingId, it logs the error and returns null
 *  If the advertisingId is successfully retrieved, it returns it
 *  It also uses the AndroidId to get the advertisingId if the advertisingId is not available
 *  If both the advertisingId and AndroidId are not available, it returns null
 *
 *  Note: This implementation is suitable for Android devices only. For other platforms, you may need to use a different approach to get the advertisingId or AndroidId.
 *
 *  Example usage:
 *  val deviceId = DeviceIdUtil.getDeviceId(context)
 *  Log.d("DeviceIdUtil", "Device ID: $deviceId")
 *
 *  Output:
 *  Device ID: [advertising id]
 */

object DeviceIdUtil {

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    suspend fun getAdvertisingId(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                Log.d(TAG, "Advertsing Id: $adInfo.id")
                adInfo.id
            } catch (e: Exception) {
                Log.e("DeviceIdUtil", "Error getting Advertising ID", e)
                null
            }
        }
    }

    suspend fun getDeviceId(context: Context): String {
        val androidId = getAndroidId(context)
        val advertisingId = getAdvertisingId(context)
        return advertisingId ?: androidId
    }
}