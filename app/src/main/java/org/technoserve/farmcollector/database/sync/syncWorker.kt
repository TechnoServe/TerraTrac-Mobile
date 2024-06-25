package org.technoserve.farmcollector.database.sync


import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.database.remote.ApiService
import org.technoserve.farmcollector.database.toDtoList
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



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
            sendSyncNotification()
            showSyncNotification()
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

        val retrofit = Retrofit.Builder()
            .baseUrl("https://8e00-154-72-7-234.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        try {
            val deviceId = DeviceIdUtil.getDeviceId(applicationContext)
            val farmDtos = unsyncedFarms.toDtoList(deviceId,farmDao)
            Log.d("YourTag", "Device ID: $deviceId")

            Log.d(TAG, "Syncing Farms: $farmDtos")

            val response = api.syncFarms(farmDtos)
            if (response.isSuccessful) {
                unsyncedFarms.forEach { farm ->
                    farmDao.updateFarmSyncStatus(farm.copy(synced = true))
                }
                Log.d(TAG, "Farms synced successfully.")
            } else {
                Log.d(TAG, "Failed to sync farms: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing farms: ${e.message}", e)
            return Result.retry()
        }

        Log.d(TAG, "SyncWorker completed successfully.")
        if (checkNotificationPermission()) {
            createNotificationChannelAndShowCompleteNotification()
        } else {
            Log.d(TAG, "Notification permission not granted.")
        }
        return Result.success()
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

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync_complete)
            .setContentTitle("Sync Complete")
            .setContentText("Farms have been successfully synchronized with the server.")
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


