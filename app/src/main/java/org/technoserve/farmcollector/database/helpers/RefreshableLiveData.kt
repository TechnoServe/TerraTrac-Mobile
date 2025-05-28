package org.technoserve.farmcollector.database.helpers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * This class represents the RefreshableLiveData that is used to refresh the data from the database
 */
class RefreshableLiveData<T>(
    private val source: () -> LiveData<T>
) : MediatorLiveData<T>() {

    private var liveData = source()

    init {
        this.addSource(liveData, ::observer)
    }

    private fun observer(data: T) {
        value = data
    }
}