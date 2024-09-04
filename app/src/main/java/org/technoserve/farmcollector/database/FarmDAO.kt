package org.technoserve.farmcollector.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.UUID

@Dao
interface FarmDAO {

    @Transaction
    @Query("SELECT * FROM Farms WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun getAll(siteId: Long): LiveData<List<Farm>>

    @Transaction
    @Query("SELECT * FROM Farms WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun getAllSync(siteId: Long): List<Farm>

    @Transaction
    @Query("SELECT * FROM Farms ORDER BY createdAt DESC")
    fun getAllFarms(): List<Farm>

    @Transaction
    @Query("SELECT * FROM CollectionSites ORDER BY createdAt DESC")
    fun getAllSites(): List<CollectionSite>

    @Transaction
    @Query("SELECT * FROM CollectionSites ORDER BY createdAt DESC")
    fun getSites(): LiveData<List<CollectionSite>>

    @Transaction
    @Query("SELECT * FROM Farms ORDER BY createdAt DESC")
    fun getData(): LiveData<List<Farm>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(farm: Farm):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(farms: List<Farm>)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSite(site: CollectionSite)

    @Update
    fun updateSite(site: CollectionSite)

    @Transaction
    @Query("SELECT * FROM Farms WHERE id = :id ORDER BY id DESC")
    fun getFarmById(id: Long): LiveData<List<Farm>>

    @Transaction
    @Query("SELECT * FROM Farms WHERE id = :id ORDER BY id DESC")
    fun getRawFarmById(id: Long): List<Farm>

    @Transaction
    @Query("SELECT * FROM Farms ORDER BY id DESC LIMIT 1")
    fun getLastFarm(): LiveData<List<Farm>>

    @Update
    fun update(farm: Farm)

    @Delete
    suspend fun delete(farm: Farm)

    @Query("DELETE FROM Farms")
    fun deleteAll()

    @Query("UPDATE Farms SET synced=1 WHERE id = :id")
    fun updateSyncStatus(id: Long)

    @Query("SELECT * FROM CollectionSites WHERE siteId = :siteId LIMIT 1")
    fun getCollectionSiteById(siteId: Long): CollectionSite?

//    @Update
//    suspend fun updateFarmSyncStatus(farm: Farm)

    @Query("UPDATE farms SET synced = :synced WHERE id = :remoteId")
    suspend fun updateFarmSyncStatus(remoteId: UUID, synced: Boolean)

    @Query("UPDATE Farms SET scheduledForSync=1 WHERE id IN (:ids)")
    fun updateSyncListStatus(ids: List<Long>)

    @Query("DELETE FROM Farms WHERE id IN (:ids)")
    fun deleteList(ids: List<Long>)

    @Query("DELETE FROM CollectionSites WHERE siteId IN (:ids)")
    fun deleteListSite(ids: List<Long>)

    @Query("SELECT * FROM Farms WHERE synced = 1")
    suspend fun getUnsyncedFarms(): List<Farm>

    @Query("SELECT * FROM Farms WHERE remote_id=:remoteId LIMIT 0")
    suspend fun getFarmByRemoteId(remoteId: UUID): Farm?

    @Query("SELECT * FROM Farms WHERE  siteId = :siteId LIMIT 1")
    suspend fun getFarmBySiteId(siteId: Long): Farm?

    @Query("DELETE FROM farms WHERE remote_id = :remoteId")
    suspend fun deleteFarmByRemoteId(remoteId: UUID)

    @Query("SELECT * FROM farms WHERE remote_id = :remoteId OR (farmerName = :farmerName AND village = :village AND district = :district) LIMIT 1")
    suspend fun getFarmByDetails(remoteId: UUID, farmerName: String, village: String, district: String): Farm?

    @Transaction
    suspend fun insertAllIfNotExists(farms: List<Farm>) {
        farms.forEach { farm ->
            if (farm.remoteId?.let { getFarmByRemoteId(it) } == null) {
                insertAll(listOf(farm))
            }
        }
    }

    @Transaction
    suspend fun updateSyncStatusForFarms(farms: List<Farm>) {
        farms.forEach { farm ->
            updateFarmSyncStatus(farm.remoteId, true)
        }
    }


}