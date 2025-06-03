package org.technoserve.farmcollector

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.technoserve.farmcollector.database.helpers.ContextProvider
import org.technoserve.farmcollector.database.sync.SyncWorker
import org.technoserve.farmcollector.utils.BackupPreferences
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
/**
 *
 * This class initializes WorkManager and sets up a periodic sync job to fetch and update data from the server.
 *
 */

class FarmCollectorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ContextProvider.initialize(this)
        observeBackupPreference() // colors Start observing backup setting
    }

    /**
     * colors Observe user's backup setting and enable WorkManager only when backup is ON
     */
    private fun observeBackupPreference() {
        val isBackupEnabled = runBlocking { BackupPreferences.isBackupEnabled(this@FarmCollectorApp).first() }

        if (isBackupEnabled) {
            initializeWorkManager() // colors Start WorkManager if backup is enabled
        } else {
            cancelWorkManager() // colors Stop WorkManager if backup is disabled
        }
    }

    /**
     * colors Initialize WorkManager when backup is enabled
     */
    private fun initializeWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_work_tag",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d("WorkManager", "WorkManager started because backup is enabled")
    }

    /**
     * colors Cancel WorkManager when backup is disabled
     */
    private fun cancelWorkManager() {
        WorkManager.getInstance(this).cancelUniqueWork("sync_work_tag")
        Log.d("WorkManager", "WorkManager canceled because backup is disabled")
    }
}
