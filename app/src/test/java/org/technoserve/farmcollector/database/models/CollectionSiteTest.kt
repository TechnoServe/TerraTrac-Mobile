package org.technoserve.farmcollector.database.models
/*
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.TestDatabase
import org.technoserve.farmcollector.database.dao.CollectionSiteDAO
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CollectionSiteTest{

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData

    private lateinit var database: TestDatabase
    private lateinit var collectionSiteDao: CollectionSiteDAO

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestDatabase::class.java
        )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        collectionSiteDao = database.collectionSiteDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve CollectionSite`() {
        val collectionSite = CollectionSite(
            name = "Test Site",
            agentName = "Agent Smith",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Test Village",
            district = "Test District",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Insert the entity
        val id = collectionSiteDao.insertSite(collectionSite)
        assertTrue(id > 0) // Ensure the ID was auto-generated

        // Retrieve and verify the entity
        val retrievedSite = collectionSiteDao.getCollectionSiteById(id)
        assertNotNull(retrievedSite)
        assertEquals(collectionSite.name, retrievedSite?.name)
        assertEquals(collectionSite.agentName, retrievedSite?.agentName)
        assertEquals(collectionSite.phoneNumber, retrievedSite?.phoneNumber)
        assertEquals(collectionSite.email, retrievedSite?.email)
        assertEquals(collectionSite.village, retrievedSite?.village)
        assertEquals(collectionSite.district, retrievedSite?.district)
    }

    @Test
    fun `update CollectionSite`() {
        val collectionSite = CollectionSite(
            name = "Test Site",
            agentName = "Agent Smith",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Test Village",
            district = "Test District",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Insert and retrieve ID
        val id = collectionSiteDao.insertSite(collectionSite)
        assertTrue(id > 0)

        // Update entity
        val updatedSite = collectionSite.copy(name = "Updated Site")
        collectionSiteDao.updateSite(updatedSite)

        // Retrieve and verify update
        val retrievedSite = collectionSiteDao.getCollectionSiteById(id)
        assertNotNull(retrievedSite)
        assertEquals("Updated Site", retrievedSite?.name)
    }

    @Test
    fun `delete CollectionSite`() {
        val collectionSite = CollectionSite(
            name = "Test Site",
            agentName = "Agent Smith",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Test Village",
            district = "Test District",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Insert and retrieve ID
        val id = collectionSiteDao.insertSite(collectionSite)
        assertTrue(id > 0)

        // Delete the entity
        collectionSiteDao.delete(collectionSite)

        // Verify deletion
        val retrievedSite = collectionSiteDao.getCollectionSiteById(id)
        assertNull(retrievedSite)
    }

    @Test
    fun `verify DateConverter works with CollectionSite`() {
        val createdAt = System.currentTimeMillis()
        val updatedAt = System.currentTimeMillis()

        val collectionSite = CollectionSite(
            name = "Test Site",
            agentName = "Agent Smith",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Test Village",
            district = "Test District",
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Insert and retrieve ID
        val id = collectionSiteDao.insertSite(collectionSite)
        val retrievedSite = collectionSiteDao.getCollectionSiteById(id)

        assertNotNull(retrievedSite)
        assertEquals(createdAt, retrievedSite?.createdAt)
        assertEquals(updatedAt, retrievedSite?.updatedAt)
    }
}
*/