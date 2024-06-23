package org.technoserve.farmcollector.database.sync


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val TAG = "SyncWorker"

    override suspend fun doWork(): Result {

        sendSyncNotification()

        val db = AppDatabase.getInstance(applicationContext)
        val farmDao = db.farmsDAO()
        val unsyncedFarms = farmDao.getUnsyncedFarms()

        Log.d(TAG, "Found ${unsyncedFarms.size} unsynced farms.")

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        unsyncedFarms.forEach { farm ->
            try {
                Log.d(TAG, "Syncing Farm: ${farm.id}")

                val response = api.syncFarm(farm)
                if (response.isSuccessful) {
                    farmDao.updateFarmSyncStatus(farm.copy(synced = true))
                    Log.d(TAG, "Note ${farm.id} synced successfully.")
                } else {
                    Log.d(TAG, "Failed to sync farm ${farm.id}: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing farm ${farm.id}: ${e.message}", e)
                return Result.retry()
            }
        }

        Log.d(TAG, "SyncWorker completed successfully.")
        return Result.success()
    }


    private fun sendSyncNotification() {
        // Create notification channel if necessary
        createNotificationChannel()

        val builder = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL_ID_2")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sync Started")
            .setContentText("Synchronizing Farms Data with the server.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            with(NotificationManagerCompat.from(applicationContext)) {
                // Use a unique notification ID to avoid clashing with other notifications
                notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Handle the exception if there is a security issue
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sync Channel"
            val descriptionText = "Channel for sync notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("SYNC_CHANNEL_ID_2", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
