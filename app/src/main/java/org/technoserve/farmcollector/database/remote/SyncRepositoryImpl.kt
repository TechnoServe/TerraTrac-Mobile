package org.technoserve.farmcollector.database.remote


import android.content.Context
import androidx.work.*
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.technoserve.farmcollector.database.sync.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
/*
class SyncRepositoryImpl @Inject constructor(
    private val context: Context
) : SyncRepository {

    private val syncWorkTag = "sync_work_tag"
    override suspend fun syncFarms(): Boolean {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request to sync data every 2 Hours
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(syncWorkTag)
            .build()

        return try {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(syncRequest)

            // Wait for the work to complete and get the result
            withContext(Dispatchers.IO) {
                val workInfo = workManager.getWorkInfoById(syncRequest.id).get()
                workInfo.state == WorkInfo.State.SUCCEEDED
            }
        } catch (e: Exception) {
            false
        }
    }
}
*/
