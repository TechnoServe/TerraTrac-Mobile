package org.technoserve.farmcollector.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.technoserve.farmcollector.database.models.CollectionSite


/**
 * This interface provides methods to interact with the database
 */

@Dao
interface CollectionSiteDAO {


    @Transaction
    @Query("SELECT * FROM CollectionSites ORDER BY createdAt DESC")
    fun getAllSites(): List<CollectionSite>

    @Transaction
    @Query("SELECT * FROM CollectionSites ORDER BY createdAt DESC")
    fun getSites(): LiveData<List<CollectionSite>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSite(site: CollectionSite): Long

    @Query("SELECT * FROM CollectionSites WHERE siteId = :siteId")
    suspend fun getSiteById(siteId: Long): CollectionSite?

    @Update
    fun update(collectionSite: CollectionSite)

    @Delete
    fun delete(collectionSite: CollectionSite)

    @Update
    fun updateSite(site: CollectionSite)

    @Query("SELECT * FROM CollectionSites WHERE siteId = :siteId LIMIT 1")
    fun getCollectionSiteById(siteId: Long): CollectionSite?

    @Query("DELETE FROM CollectionSites WHERE siteId IN (:ids)")
    fun deleteListSite(ids: List<Long>)

    @Query("SELECT * FROM CollectionSites WHERE siteId = :localCsId OR (name = :siteName AND village = :village AND district = :district) LIMIT 1")
    suspend fun getSiteByDetails(
        localCsId: Long,
        siteName: String,
        village: String,
        district: String
    ): CollectionSite?


    @Query("SELECT * FROM CollectionSites LIMIT :limit OFFSET :offset")
    fun getCollectionSites(offset: Int, limit: Int): List<CollectionSite>
}