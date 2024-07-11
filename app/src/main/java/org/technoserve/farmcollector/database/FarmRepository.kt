package org.technoserve.farmcollector.database

import androidx.lifecycle.LiveData

class FarmRepository(private val farmDAO: FarmDAO) {

    val readAllSites: LiveData<List<CollectionSite>> = farmDAO.getSites()
    val readData: LiveData<List<Farm>> = farmDAO.getData()
    fun readAllFarms(siteId: Long): LiveData<List<Farm>> {
        return farmDAO.getAll(siteId)
    }

    fun readFarm(farmId: Long): LiveData<List<Farm>> {
        return farmDAO.getFarmById(farmId)
    }

    suspend fun addFarm(farm: Farm) {
        farmDAO.insert(farm)
    }

    private suspend fun addFarms(farms: List<Farm>) {
        farmDAO.insertAll(farms)
    }

    suspend fun addSite(site: CollectionSite) {
        farmDAO.insertSite(site)
    }

    fun getLastFarm(): LiveData<List<Farm>> {
        return farmDAO.getLastFarm()
    }

    suspend fun updateFarm(farm: Farm) {
        farmDAO.update(farm)
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

    private suspend fun isFarmDuplicate(farm: Farm): Boolean {
        return farmDAO.getFarmByRemoteId(farm.remoteId) != null
    }

    suspend fun importFarms(farms: List<Farm>) {
        // Check for duplicates and filter them out
        val nonDuplicateFarms = farms.filterNot { isFarmDuplicate(it) }
        addFarms(nonDuplicateFarms)
    }

}