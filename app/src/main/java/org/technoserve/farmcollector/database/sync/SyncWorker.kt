package org.technoserve.farmcollector.database.sync

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.OkHttpClient
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.technoserve.farmcollector.BuildConfig
import org.technoserve.farmcollector.database.toDeviceFarmDtoList
import java.util.concurrent.TimeUnit


class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val TAG = "SyncWorker"
    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private var startTime: Long = 0

    // Simulate total and synced items for progress calculation
    private var totalItems: Int = 0
    private var syncedItems: Int = 0

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {

        if (checkNotificationPermission()) {
            // sendSyncNotification()
        } else {
            Log.d(TAG, "Notification permission not granted.")
            // Handle the case where notification permission is not granted
            // For example, inform the user through other means or log the event
        }

        val db = AppDatabase.getInstance(applicationContext)
        val farmDao = db.farmsDAO()
        val unsyncedFarms = farmDao.getUnsyncedFarms()

        totalItems = unsyncedFarms.size
        Log.d(TAG, "Found ${unsyncedFarms.size} unsynced farms.")


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

        try {
            val deviceId = DeviceIdUtil.getDeviceId(applicationContext)
            val farmDtos = unsyncedFarms.toDeviceFarmDtoList(deviceId, farmDao)

            Log.d("YourTag", "Device ID: $deviceId")

            // Log the payload
            Log.d(TAG, "Payload to send: ${farmDtos.joinToString(separator = "\n") { it.toString() }}")

            Log.d(TAG, "Farms before sync: ${unsyncedFarms.map { it.synced }}")


           // Log.d(TAG, "Syncing Farms: ${farmDtos.size}")

            val response = api.syncFarms(farmDtos)
           // Log.d(TAG, "Response: $response")

            if (response.isSuccessful) {
                unsyncedFarms.forEach { farm ->
                    farmDao.updateFarmSyncStatus(farm.remoteId, true)
                }
                if(totalItems>0) {
                    Log.d(TAG, "Farms synced successfully.")
                    createNotificationChannelAndShowCompleteNotification() // Notify sync success
                }
            } else {
                Log.d(TAG, "Failed to sync farms: ${response.message()}")
                createSyncFailedNotification() // Notify sync failure
                return Result.failure() // Return failure result
            }
            Log.d(TAG, "Farms After sync: ${unsyncedFarms.map { it.synced }}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing farms: ${e.message}", e)
            createSyncFailedNotification() // Notify sync failure
            return Result.retry() // Retry if an exception occurred
        }

        // Log.d(TAG, "SyncWorker completed successfully.")
        return Result.success()
    }

    private fun createSyncFailedNotification() {
        if (!checkNotificationPermission()) {
// Log.d(TAG, "Notification permission not granted.")
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync_failed)
            .setContentTitle("Sync Failed")
            .setContentText("Failed to synchronize Farms Data with the server.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(4, builder.build())
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions below Android 13 are handled in the manifest
        }
    }

    private fun sendSyncNotification() {
        // Create notification channel if necessary
        createNotificationChannel()

        if (!checkNotificationPermission()) {
            Log.d(TAG, "Notification permission not granted.")
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync)
            .setContentTitle("Sync Started")
            .setContentText("Synchronizing Farms Data with the server.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
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
    }

    private fun showSyncNotification() {
        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync)
            .setContentTitle("Sync Data in Progress")
            .setContentText("Synchronizing Farms Data with the server.")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (checkNotificationPermission()) {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(2, builder.build())
            }
        }

        // Initialize the start time and handler
        startTime = System.currentTimeMillis()
        handler = Handler(Looper.getMainLooper())

        // Define the updateRunnable
        updateRunnable = object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                // Calculate elapsed time
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                // Calculate sync progress
                val syncProgress = if (totalItems > 0) (syncedItems * 100) / totalItems else 0

                // Update the notification content
                val timeText = String.format("Elapsed time: %02d:%02d:%02d", hours, minutes, seconds)
                val progressText = String.format("Data synchronized: %d%%", syncProgress)
                builder.setContentText("$timeText\n$progressText")

                // Notify the updated notification
                if (checkNotificationPermission()) {
                    with(NotificationManagerCompat.from(applicationContext)) {
                        notify(2, builder.build())
                    }
                }

                // Re-run the handler every second if sync is not complete
                if (syncedItems < totalItems) {
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // Start the handler to update the notification
        handler.post(updateRunnable)
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

        val builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.sync_channel_id))
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
