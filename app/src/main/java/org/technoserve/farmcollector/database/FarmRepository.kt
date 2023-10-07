package org.technoserve.farmcollector.database

import androidx.lifecycle.LiveData

class FarmRepository(private val farmDAO: FarmDAO) {

    val readAllFarms: LiveData<List<Farm>> = farmDAO.getAll()

    fun readFarm(farmId: Long): LiveData<List<Farm>> {
        return farmDAO.getFarmById(farmId)
    }

    suspend fun addFarm(farm: Farm) {
        farmDAO.insert(farm)
    }

    fun getLastFarm(): LiveData<List<Farm>> {
        return farmDAO.getLastFarm()
    }

    suspend fun updateFarm(farm: Farm) {
        farmDAO.update(farm)
    }

    suspend fun deleteFarm(farm: Farm) {
        farmDAO.delete(farm)
    }

    suspend fun deleteAllFarms() {
        farmDAO.deleteAll()
    }

    suspend fun updateSyncStatus(id: Long){
        farmDAO.updateSyncStatus(id)
    }

    suspend fun updateSyncListStatus(ids: List<Long>){
        farmDAO.updateSyncListStatus(ids)
    }

    suspend fun deleteList(ids: List<Long>){
        farmDAO.deleteList(ids)
    }


}