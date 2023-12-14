package org.technoserve.farmcollector.database

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FarmRepository
    val readAllSites: RefreshableLiveData<List<CollectionSite>>
    val readData: RefreshableLiveData<List<Farm>>
    init {
        val farmDAO = AppDatabase.getInstance(application).farmsDAO()
        repository = FarmRepository(farmDAO)
        readAllSites = RefreshableLiveData{repository.readAllSites}
        readData = RefreshableLiveData{repository.readData}
    }

    fun readAllData(siteId: Long): LiveData<List<Farm>>{
        return repository.readAllFarms(siteId)
    }

    fun getSingleFarm(farmId: Long): LiveData<List<Farm>>{
        return repository.readFarm(farmId)
    }

    fun addFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFarm(farm)
        }
    }

    fun addSite(site: CollectionSite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSite(site)
        }
    }

    fun getLastFarm(): LiveData<List<Farm>> {
        return  repository.getLastFarm()
    }

    fun updateFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFarm(farm)
        }
    }

    fun deleteFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFarm(farm)
        }
    }

    fun deleteAllFarms() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllFarms()
        }
    }

    fun updateSyncStatus(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSyncStatus(id)
        }
    }

    fun updateSyncListStatus(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSyncListStatus(ids)
        }
    }

    fun deleteList(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteList(ids)
        }
    }

    fun deleteListSite(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteListSite(ids)
        }
    }
}

class FarmViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(FarmViewModel::class.java)) {
            return FarmViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}