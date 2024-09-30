package org.technoserve.farmcollector.database

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData

class FarmRepository(private val farmDAO: FarmDAO) {

    val readAllSites: LiveData<List<CollectionSite>> = farmDAO.getSites()
    val readData: LiveData<List<Farm>> = farmDAO.getData()
    fun readAllFarms(siteId: Long): LiveData<List<Farm>> {
        return farmDAO.getAll(siteId)
    }

    fun getAllFarms(): List<Farm> {
        return farmDAO.getAllFarms()
    }

    fun getAllSites(): List<CollectionSite>{
        return farmDAO.getAllSites()
    }

    fun readAllFarmsSync(siteId: Long): List<Farm> {
        return farmDAO.getAllSync(siteId)
    }

    fun readFarm(farmId: Long): LiveData<List<Farm>> {
        return farmDAO.getFarmById(farmId)
    }

//    suspend fun addFarm(farm: Farm) {
//        val existingFarm = isFarmDuplicate(farm)
//
//     // Check if the farm already exists
//        if (existingFarm == null) {
//            Log.d(TAG, "Attempting to insert new farm: $farm")
//            val insertResult = farmDAO.insert(farm)
//            Log.d(TAG, "Insert operation result: $insertResult")
//            if (insertResult != -1L) {
//                Log.d(TAG, "New farm inserted: $farm")
//            } else {
//                Log.d(TAG, "Insertion was ignored (likely due to conflict strategy)")
//            }
//        } else {
//            Log.d(TAG, "Farm already exists: $existingFarm")
//
//            if (farmNeedsUpdate(existingFarm, farm)) {
//                Log.d(TAG, "Updating existing farm: $farm")
//                farmDAO.update(farm)
//            } else {
//                Log.d(TAG, "No update needed for farm: $farm")
//            }
//        }
//    }

    suspend fun addFarm(farm: Farm) {
        try {
            // Step 1: Ensure that the CollectionSite exists for the farm's siteId
            val collectionSite = farmDAO.getCollectionSiteById(farm.siteId)
            if (collectionSite == null) {
                Log.e(TAG, "Failed to insert farm. CollectionSite with siteId ${farm.siteId} does not exist.")
                return  // Exit if the CollectionSite doesn't exist
            }

            // Step 2: Check if the farm already exists
            val existingFarm = isFarmDuplicate(farm)
            if (existingFarm == null) {
                Log.d(TAG, "Attempting to insert new farm: $farm")
                val insertResult = farmDAO.insert(farm)

                if (insertResult != -1L) {
                    Log.d(TAG, "New farm inserted successfully: $farm")
                } else {
                    Log.e(TAG, "Farm insertion failed, insertResult: $insertResult")
                }
            } else {
                Log.d(TAG, "Farm already exists: $existingFarm")

                // Step 3: Check if the farm needs an update
                if (farmNeedsUpdate(existingFarm, farm)) {
                    Log.d(TAG, "Updating existing farm: $farm")
                    farmDAO.update(farm)
                } else {
                    Log.d(TAG, "No update needed for farm: $farm")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during farm insertion or update: ${e.message}", e)
        }
    }




    private suspend fun addFarms(farms: List<Farm>) {
        farmDAO.insertAllIfNotExists(farms)
    }


    suspend fun addSite(site: CollectionSite) : Boolean {
        // Check if the site already exists
        val existingSite = isSiteDuplicate(site)

        if (existingSite == null) {
            Log.d(TAG, "Attempting to insert new site: $site")
            val insertResult = farmDAO.insertSite(site)
            Log.d(TAG, "Insert operation result: $insertResult")
            if (insertResult != -1L) {
                Log.d(TAG, "New site inserted: $site")
                return true
            } else {
                Log.d(TAG, "Insertion was ignored (likely due to conflict strategy)")
                return false
            }
        } else {
            Log.d(TAG, "Site already exists: $existingSite")
            return false
        }
    }


    fun getLastFarm(): LiveData<List<Farm>> {
        return farmDAO.getLastFarm()
    }
    suspend fun getFarmBySiteId(siteId: Long): Farm? {
        return farmDAO.getFarmBySiteId(siteId)
    }


    suspend fun updateFarm(farm: Farm) {
        farmDAO.update(farm)
    }
    private suspend fun updateFarms(farms: List<Farm>) {
        farms.forEach { updateFarm(it) }
    }

    suspend fun updateSite(site: CollectionSite) {
        farmDAO.updateSite(site)
    }


    suspend fun deleteFarm(farm: Farm) {
        farmDAO.delete(farm)
    }

    suspend fun deleteFarmById(farm: Farm) {
        farmDAO.deleteFarmByRemoteId(farm.remoteId)
    }


    suspend fun deleteAllFarms() {
        farmDAO.deleteAll()
    }

    suspend fun updateSyncStatus(id: Long) {
        farmDAO.updateSyncStatus(id)
    }

    suspend fun updateSyncListStatus(ids: List<Long>) {
        farmDAO.updateSyncListStatus(ids)
    }

    suspend fun deleteList(ids: List<Long>) {
        farmDAO.deleteList(ids)
    }

    suspend fun deleteListSite(ids: List<Long>) {
        farmDAO.deleteListSite(ids)
    }

    suspend fun isFarmDuplicateBoolean(farm: Farm): Boolean {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        ) != null
    }

    suspend fun isFarmDuplicate(farm: Farm): Farm? {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        )
    }

    suspend fun isSiteDuplicate(collectionSite: CollectionSite): CollectionSite? {
        return farmDAO.getSiteByDetails(
           collectionSite.siteId,
            collectionSite.district,
            collectionSite.name,
            collectionSite.village
        )
    }

    // Function to fetch a farm by remote ID, farmer name, and address
    suspend fun getFarmByDetails(farm: Farm): Farm? {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        )
    }

    fun farmNeedsUpdate(existingFarm: Farm, newFarm: Farm): Boolean {
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
                newFarm.remoteId.toString().isEmpty() ||
                newFarm.coordinates.isNullOrEmpty()
    }
}