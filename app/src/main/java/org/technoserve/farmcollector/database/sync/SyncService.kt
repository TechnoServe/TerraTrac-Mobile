package org.technoserve.farmcollector.database.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.technoserve.farmcollector.R
import java.util.concurrent.TimeUnit

class SyncService : Service() {

    private val syncWorkTag = "sync_work_tag"

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "SyncServiceChannel"
        private const val NOTIFICATION_ID = 12345
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSyncWork()
        // If the service is killed while starting, it will be restarted
        return START_STICKY
    }

    private fun startSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request to sync data every 24 hours
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(syncWorkTag)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            syncWorkTag,
            ExistingPeriodicWorkPolicy.REPLACE,  // Replace existing work with the new work
            syncRequest
        )
    }
    override fun onBind(intent: Intent?): IBinder? {
        // Return null as this service doesn't need to bind with an activity
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sync Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, getString(R.string.sync_service_channel_id))
            .setContentTitle(getString(R.string.sync_service_notification_title))
            .setContentText(getString(R.string.sync_service_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_sync)
            .build()
    }

}