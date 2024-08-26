package org.technoserve.farmcollector.database.sync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object DeviceIdUtil {

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getImei(context: Context): String? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // You need to request the permission from the user
            return null
        }
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.imei
    }

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    suspend fun getAdvertisingId(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                adInfo.id
            } catch (e: Exception) {
                Log.e("DeviceIdUtil", "Error getting Advertising ID", e)
                null
            }
        }
    }

    suspend fun getDeviceId(context: Context): String {
        val androidId = getAndroidId(context)
        val imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getImei(context) else null
        val advertisingId = getAdvertisingId(context)

        return advertisingId ?: imei ?: androidId
    }
}