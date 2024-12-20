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
import java.util.concurrent.TimeUnit
/**
 *
 * This class initializes WorkManager and sets up a periodic sync job to fetch and update data from the server.
 *
 */

//class FarmCollectorApp : Application(), Configuration.Provider {
class FarmCollectorApp : Application(){

    override fun onCreate() {
        super.onCreate()
        ContextProvider.initialize(this)
        initializeWorkManager()
    }

    private fun initializeWorkManager() {
        // Build the constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requires a connected network
            .build()

        // Create the periodic work request
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // Enqueue the periodic work with a unique name to avoid duplicate schedules
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_work_tag", // Unique name for the work
            ExistingPeriodicWorkPolicy.UPDATE, // Replace if already exists
            workRequest
        )

        Log.d("WorkManager", "WorkManager is initialized successfully")
    }

//    // Provide the WorkManager configuration
//    override val workManagerConfiguration: Configuration
//        get() = Configuration.Builder()
//            .setMinimumLoggingLevel(Log.DEBUG) // Set logging level
//            .build()
}