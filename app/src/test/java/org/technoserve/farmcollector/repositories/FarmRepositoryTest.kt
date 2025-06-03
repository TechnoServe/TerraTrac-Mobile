package org.technoserve.farmcollector.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.technoserve.farmcollector.database.dao.FarmDAO
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Farm

class FarmRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockFarmDAO: FarmDAO
    private lateinit var farmRepository: FarmRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)  // Use openMocks instead of initMocks
        farmRepository = FarmRepository(mockFarmDAO)
    }

    @Test
    fun `readAllFarms returns farms for specific site`() {
        val siteId = 1L
        val expectedFarms = MutableLiveData<List<Farm>>()
        `when`(mockFarmDAO.getAll(siteId)).thenReturn(expectedFarms)

        val farms = farmRepository.readAllFarms(siteId)
        assertEquals(expectedFarms, farms)
    }

    @Test
    fun `addSite inserts site if not duplicate`(): Unit = runBlocking {
        val site = CollectionSite(
            district = "Test District", name = "Test Site", village = "Test Village",
            agentName = "TEST Agent",
            phoneNumber = "1234555",
            email = "test@email.com",
            createdAt = 17000,
            updatedAt = 17000
        )
        `when`(
            mockFarmDAO.getSiteByDetails(
                anyLong(),
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(null)

        val result = farmRepository.addSite(site)
        assertTrue(result)
        verify(mockFarmDAO).insertSite(site)
    }

    @Test
    fun `deleteFarmById deletes farm by remote id`() = runBlocking {
        val farm = Farm(
            siteId = 1L,
            farmerPhoto = "photo.jpg",
            farmerName = "Old Farmer",
            memberId = "12345",
            village = "Village A",
            district = "District X",
            purchases = 10f,
            size = 100f,
            latitude = "12.34",
            longitude = "56.78",
            coordinates = listOf(Pair(12.34, 56.78)),
            accuracyArray = listOf(5.0f),
            synced = false,
            scheduledForSync = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            needsUpdate = true
        )

        farmRepository.deleteFarmById(farm)
        verify(mockFarmDAO).deleteFarmByRemoteId(farm.remoteId)
    }

    @Test
    fun `getTotalFarmsForSite returns total farms for a site`() {
        val siteId = 1L
        val expectedTotal = MutableLiveData(10)
        `when`(mockFarmDAO.getTotalFarmsForSite(siteId)).thenReturn(expectedTotal)

        val total = farmRepository.getTotalFarmsForSite(siteId)
        assertEquals(expectedTotal, total)
    }

    @Test
    fun `getFarmsWithIncompleteDataForSite returns incomplete farms count`() {
        val siteId = 1L
        val expectedCount = MutableLiveData(2)
        `when`(mockFarmDAO.getFarmsWithIncompleteDataForSite(siteId)).thenReturn(expectedCount)

        val incompleteCount = farmRepository.getFarmsWithIncompleteDataForSite(siteId)
        assertEquals(expectedCount, incompleteCount)
    }
}