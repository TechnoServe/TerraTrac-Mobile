package org.technoserve.farmcollector.database.sync

import android.app.Service
import android.app.Service.START_STICKY
import android.content.Intent
import android.os.IBinder
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SyncService : Service() {

    private val syncWorkTag = "sync_work_tag"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSyncWork()
        // If the service is killed while starting, it will be restarted
        return START_STICKY
    }

    private fun startSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request to sync data every 5 minutes
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
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
}