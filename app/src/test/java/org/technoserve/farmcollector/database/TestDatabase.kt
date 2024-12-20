package org.technoserve.farmcollector.database

import androidx.room.Database
import androidx.room.RoomDatabase
import org.technoserve.farmcollector.database.dao.CollectionSiteDAO
import org.technoserve.farmcollector.database.models.CollectionSite

@Database(entities = [CollectionSite::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase() {
    abstract fun collectionSiteDAO(): CollectionSiteDAO
}
