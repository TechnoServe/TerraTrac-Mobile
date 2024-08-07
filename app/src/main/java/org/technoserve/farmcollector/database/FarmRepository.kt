package org.technoserve.farmcollector.database

import androidx.lifecycle.LiveData
import java.util.UUID

class FarmRepository(private val farmDAO: FarmDAO) {

    val readAllSites: LiveData<List<CollectionSite>> = farmDAO.getSites()
    val readData: LiveData<List<Farm>> = farmDAO.getData()
    fun readAllFarms(siteId: Long): LiveData<List<Farm>> {
        return farmDAO.getAll(siteId)
    }

    fun readAllFarmsSync(siteId: Long): List<Farm> {
        return farmDAO.getAllSync(siteId)
    }

    fun readFarm(farmId: Long): LiveData<List<Farm>> {
        return farmDAO.getFarmById(farmId)
    }

    suspend fun addFarm(farm: Farm) {
        val existingFarm = isFarmDuplicate(farm)
        if (existingFarm == null) {
            farmDAO.insert(farm)
        } else {
            // If the farm exists and needs an update, perform the update
            if (farmNeedsUpdate(existingFarm, farm)) {
                farmDAO.update(farm)
            }
        }
    }

    private suspend fun addFarms(farms: List<Farm>) {
        farmDAO.insertAllIfNotExists(farms)
    }

    suspend fun addSite(site: CollectionSite) {
        farmDAO.insertSite(site)
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

//    suspend fun isFarmDuplicateBoolean(farm: Farm): Boolean {
//        return farm.remoteId?.let { farmDAO.getFarmByRemoteId(it) } != null
//    }
//
//    suspend fun isFarmDuplicate(farm: Farm): Farm? {
//        return farm.remoteId?.let { farmDAO.getFarmByRemoteId(it) }
//    }
//
//    // Function to fetch a farm by remote ID
//    suspend fun getFarmByRemoteId(remoteId: UUID): Farm? {
//        return farmDAO.getFarmByRemoteId(remoteId)
//    }

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

    // Function to fetch a farm by remote ID, farmer name, and address
    suspend fun getFarmByDetails(farm: Farm): Farm? {
        return farmDAO.getFarmByDetails(
            farm.remoteId,
            farm.farmerName,
            farm.village,
            farm.district
        )
    }


//    suspend fun deleteFarmByRemoteId(remoteId: UUID) {
//        farmDAO.deleteFarmByRemoteId(remoteId)
//    }
//


    fun farmNeedsUpdate(existingFarm: Farm, newFarm: Farm): Boolean {
        return existingFarm.farmerName != newFarm.farmerName ||
                existingFarm.size != newFarm.size ||
                existingFarm.village != newFarm.village ||
                existingFarm.district != newFarm.district
    }

//    suspend fun importFarms(farms: List<Farm>): ImportResult {
//        val nonDuplicateFarms = mutableListOf<Farm>()
//        val duplicateFarms = mutableListOf<String>()
//        val farmsNeedingUpdate = mutableListOf<Farm>()
//
//        for (farm in farms) {
//            val existingFarm = isFarmDuplicate(farm)
//            duplicateFarms.add("Duplicate farm: ${farm.farmerName}, Site ID: ${farm.siteId}")
//            if (existingFarm?.let { farmNeedsUpdate(it, farm) } == true) {
//                farmsNeedingUpdate.add(farm)
//            }
//        }
//
//        addFarms(nonDuplicateFarms)
//        // Update farms that need updates
//        updateFarms(farmsNeedingUpdate)
//        return ImportResult(
//            success = nonDuplicateFarms.isNotEmpty(),
//            message = if (nonDuplicateFarms.isNotEmpty()) "Import successful" else "No farms were imported",
//            importedFarms = nonDuplicateFarms,
//            duplicateFarms = duplicateFarms,
//            farmsNeedingUpdate = farmsNeedingUpdate
//        )
//    }



}