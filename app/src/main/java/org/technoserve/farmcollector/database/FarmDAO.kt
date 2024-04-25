package org.technoserve.farmcollector.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface FarmDAO {

    @Transaction
    @Query("SELECT * FROM Farms WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun getAll(siteId: Long): LiveData<List<Farm>>

    @Transaction
    @Query("SELECT * FROM CollectionSites ORDER BY createdAt DESC")
    fun getSites(): LiveData<List<CollectionSite>>

    @Transaction
    @Query("SELECT * FROM Farms ORDER BY createdAt DESC")
    fun getData(): LiveData<List<Farm>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(farm: Farm)

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
    fun delete(farm: Farm)

    @Query("DELETE FROM Farms")
    fun deleteAll()

    @Query("UPDATE Farms SET synced=1 WHERE id = :id")
    fun updateSyncStatus(id: Long)

    @Query("UPDATE Farms SET scheduledForSync=1 WHERE id IN (:ids)")
    fun updateSyncListStatus(ids: List<Long>)

    @Query("DELETE FROM Farms WHERE id IN (:ids)")
    fun deleteList(ids: List<Long>)

    @Query("DELETE FROM CollectionSites WHERE siteId IN (:ids)")
    fun deleteListSite(ids: List<Long>)

}