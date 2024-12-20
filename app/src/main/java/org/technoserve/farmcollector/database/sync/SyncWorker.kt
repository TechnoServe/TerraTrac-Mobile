package org.technoserve.farmcollector.database.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import org.technoserve.farmcollector.BuildConfig
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.mappers.toDeviceFarmDtoList
import org.technoserve.farmcollector.database.sync.remote.ApiService
import org.technoserve.farmcollector.utils.DeviceIdUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * This Worker class handles the synchronization process between the local database and the remote server.
 * It uses Retrofit to make API calls, processes the response, and updates the local database accordingly.
 * It Divides the data into chunks and processes them accordingly to ensure that they are properly processed before
 * synchronization is complete
 */
class SyncWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val TAG = "SyncWorker"
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable

    // Simulate total and synced items for progress calculation
    private var totalItems: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {

        println("Started sync worker")

        val db = AppDatabase.getInstance(applicationContext)
        val farmDao = db.farmsDAO()
        val unsyncedFarms = farmDao.getUnsyncedFarms()
        Log.d(TAG, "UNSYNCED FARMS ${unsyncedFarms.size}")

        totalItems = unsyncedFarms.size
        if (totalItems == 0) return Result.success()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as needed
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        val chunkSize = 20 // Number of farms to process in each batch
        val chunks = unsyncedFarms.chunked(chunkSize)

        try {
            val deviceId = DeviceIdUtil.getDeviceId(applicationContext)

            for (chunk in chunks) {
                val farmDtos = chunk.toDeviceFarmDtoList(deviceId, farmDao)
                // Display the farmDtoList
                Log.d(TAG, "FarmDtos: ${farmDtos}")


                val response = api.syncFarms(farmDtos)

                if (response.isSuccessful) {
                    chunk.forEach { farm ->
                        farmDao.updateFarmSyncStatus(farm.remoteId, true)
                    }
                } else {
                    Log.e(TAG, "Sync failed for chunk with size: ${chunk.size}")
                    createSyncFailedNotification() // Notify sync failure
                    return Result.failure() // Exit on the first failed batch
                }
            }

            createNotificationChannelAndShowCompleteNotification() // Notify sync success

        } catch (e: Exception) {
            Log.e(TAG, "Exception during sync: ${e.message}", e)
            createSyncFailedNotification() // Notify sync failure
            return Result.retry() // Retry if an exception occurred
        }

        return Result.success()
    }

    private fun createSyncFailedNotification() {
        if (!checkNotificationPermission()) {
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync_failed)
            .setContentTitle(applicationContext.getString(R.string.sync_failed))
            .setContentText(applicationContext.getString(R.string.syncronization_failed))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(4, builder.build())
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions below Android 13 are handled in the manifest
        }
    }

    private fun createNotificationChannelAndShowCompleteNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sync Channel"
            val descriptionText = "Channel for sync notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("SYNC_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync_complete)
            .setContentTitle(applicationContext.getString(R.string.sync_complete))
            .setContentText(applicationContext.getString(R.string.successfully_syncronized))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (checkNotificationPermission()) {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(3, builder.build())
            }
        }

        // Stop the handler when sync is complete
        if (::handler.isInitialized && ::updateRunnable.isInitialized) {
            handler.removeCallbacks(updateRunnable)
        }
    }
}
