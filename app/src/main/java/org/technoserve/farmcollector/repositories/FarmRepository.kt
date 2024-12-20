package org.technoserve.farmcollector.repositories

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.technoserve.farmcollector.database.dao.FarmDAO
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Farm


/**
 *  This class provides methods to interact with the database
 *  It uses the FarmDAO to perform operations
 *  The methods are wrapped in try-catch blocks to handle potential exceptions
 *  It uses Coroutines for asynchronous operations
 *  It uses LiveData to notify observers about changes in the database
 *  It uses the Dispatchers.IO dispatcher for IO-bound tasks (like database operations)
 *
 */
class FarmRepository(private val farmDAO: FarmDAO) {

    val readAllSites: LiveData<List<CollectionSite>> = farmDAO.getSites()
    val readData: LiveData<List<Farm>> = farmDAO.getData()
    fun readAllFarms(siteId: Long): LiveData<List<Farm>> {
        return farmDAO.getAll(siteId)
    }

    fun getAllFarms(): List<Farm> {
        return farmDAO.getAllFarms()
    }

    fun getAllSites(): List<CollectionSite> {
        return farmDAO.getAllSites()
    }

    fun readAllFarmsSync(siteId: Long): List<Farm> {
        return farmDAO.getAllSync(siteId)
    }

    suspend fun addFarm(farm: Farm) {
        try {
            farmDAO.getCollectionSiteById(farm.siteId)
                ?: return
            val existingFarm = isFarmDuplicate(farm)
            if (existingFarm == null) {
                farmDAO.insert(farm)
            } else {
                if (farmNeedsUpdate(existingFarm, farm)) {
                    farmDAO.update(farm)
                } else {
                    Log.d(TAG, "No update needed for farm: $farm")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during farm insertion or update: ${e.message}", e)
        }
    }

    suspend fun addSite(site: CollectionSite): Boolean {
        val existingSite = isSiteDuplicate(site)
        return if (existingSite == null) {
            val insertResult = farmDAO.insertSite(site)
            insertResult != -1L
        } else {
            false
        }
    }

    fun updateFarm(farm: Farm) {
        farmDAO.update(farm)
    }

    fun updateSite(site: CollectionSite) {
        farmDAO.updateSite(site)
    }

    suspend fun deleteFarmById(farm: Farm) {
        farmDAO.deleteFarmByRemoteId(farm.remoteId)
    }

    fun deleteList(ids: List<Long>) {
        farmDAO.deleteList(ids)
    }

    fun deleteListSite(ids: List<Long>) {
        farmDAO.deleteListSite(ids)
    }
    // Method to restore a site (by inserting it back into the database)
    suspend fun restoreSite(site: CollectionSite) {
        farmDAO.restoreSite(site)
    }

    suspend fun isFarmDuplicateBoolean(farm: Farm): Boolean {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        ) != null
    }

    private suspend fun isFarmDuplicate(farm: Farm): Farm? {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        )
    }

    private suspend fun isSiteDuplicate(collectionSite: CollectionSite): CollectionSite? {
        return farmDAO.getSiteByDetails(
            collectionSite.siteId,
            collectionSite.district,
            collectionSite.name,
            collectionSite.village
        )
    }

    suspend fun getFarmByDetails(farm: Farm): Farm? {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        )
    }

    private fun farmNeedsUpdate(existingFarm: Farm, newFarm: Farm): Boolean {
        return existingFarm.farmerName != newFarm.farmerName ||
                existingFarm.size != newFarm.size ||
                existingFarm.village != newFarm.village ||
                existingFarm.district != newFarm.district
    }

    fun isDuplicateFarm(existingFarm: Farm, newFarm: Farm): Boolean {
        return existingFarm.farmerName == newFarm.farmerName &&
                existingFarm.size == newFarm.size &&
                existingFarm.village == newFarm.village &&
                existingFarm.district == newFarm.district
    }


    fun farmNeedsUpdateImport(newFarm: Farm): Boolean {
        return newFarm.farmerName.isEmpty() ||
                newFarm.district.isEmpty() ||
                newFarm.village.isEmpty() ||
                newFarm.latitude == "0.0" ||
                newFarm.longitude == "0.0" ||
                newFarm.size == 0.0f ||
                newFarm.remoteId.toString().isEmpty()
    }

    // Function to get the total number of farms for a site
    fun getTotalFarmsForSite(siteId: Long): LiveData<Int> {
        return farmDAO.getTotalFarmsForSite(siteId)
    }

    // Function to get the number of farms with incomplete data for a site
    fun getFarmsWithIncompleteDataForSite(siteId: Long): LiveData<Int> {
        return farmDAO.getFarmsWithIncompleteDataForSite(siteId)
    }

    suspend fun getCollectionSites(page: Int, pageSize: Int): List<CollectionSite> {
        val offset = (page - 1) * pageSize
        return withContext(Dispatchers.IO) {
            farmDAO.getCollectionSites(offset, pageSize)
        }
    }
}