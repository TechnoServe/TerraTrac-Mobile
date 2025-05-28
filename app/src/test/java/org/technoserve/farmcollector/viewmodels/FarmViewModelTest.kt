package org.technoserve.farmcollector.viewmodels

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.repositories.FarmRepository

/*
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
class FarmViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ensures LiveData updates occur synchronously

    @Mock
    private lateinit var mockFarmRepository: FarmRepository

    private lateinit var farmViewModel: FarmViewModel
    private lateinit var liveDataFarms: MutableLiveData<List<Farm>>

    @Mock
    private lateinit var mockApplication: Application

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Initialize LiveData
        liveDataFarms = MutableLiveData()
        `when`(mockFarmRepository.readAllFarms(anyLong())).thenReturn(liveDataFarms)


        // Initialize ViewModel
        farmViewModel = FarmViewModel(mockApplication).apply {
            farmRepository = mockFarmRepository // Inject mock repository
        }
    }

    @Test
    fun `addFarm adds farm if not duplicate`() = runBlocking {
        // Given
        val newFarm = createTestFarm()
        val siteId = newFarm.siteId
        liveDataFarms.value = emptyList() // Initial state

        // When
        `when`(mockFarmRepository.isFarmDuplicateBoolean(newFarm)).thenReturn(false)
        farmViewModel.addFarm(newFarm, siteId)

        // Simulate repository adding farm
        liveDataFarms.value = listOf(newFarm)

        // Then
        verify(mockFarmRepository).addFarm(newFarm)
        val updatedFarms = farmViewModel.farms.value
        assertNotNull(updatedFarms)
        assertTrue(updatedFarms!!.contains(newFarm))
    }

    @Test
    fun `addFarm returns error if duplicate farm exists`() = runBlocking {
        // Given
        val duplicateFarm = createTestFarm()
        val siteId = duplicateFarm.siteId
        liveDataFarms.value = listOf(duplicateFarm) // Simulate duplicate

        // When
        `when`(mockFarmRepository.isFarmDuplicateBoolean(duplicateFarm)).thenReturn(true)

        farmViewModel.addFarm(duplicateFarm, siteId)

        // Then
        verify(mockFarmRepository, never()).addFarm(duplicateFarm)
        val updatedFarms = farmViewModel.farms.value
        assertNotNull(updatedFarms)
        assertFalse(updatedFarms!!.contains(duplicateFarm)) // Farm was not added
    }

    @Test
    fun `updateFarm updates farm successfully`() = runBlocking {
        // Given
        val existingFarm = createTestFarm()
        val updatedFarm = existingFarm.copy(farmerName = "Updated Farmer")
        liveDataFarms.value = listOf(existingFarm)

        // When
        `when`(mockFarmRepository.updateFarm(updatedFarm)).thenReturn(Unit)
        farmViewModel.updateFarm(updatedFarm)

        // Simulate repository updating farm
        liveDataFarms.value = listOf(updatedFarm)

        // Then
        verify(mockFarmRepository).updateFarm(updatedFarm)
        val updatedFarms = farmViewModel.farms.value
        assertNotNull(updatedFarms)
        assertTrue(updatedFarms!!.contains(updatedFarm))
    }

    @Test
    fun `deleteFarmById deletes farm successfully`() = runBlocking {
        // Given
        val farmToDelete = createTestFarm()
        liveDataFarms.value = listOf(farmToDelete)

        // When
        `when`(mockFarmRepository.deleteFarmById(farmToDelete)).thenReturn(Unit)
        farmViewModel.deleteFarmById(farmToDelete)

        // Simulate repository deleting farm
        liveDataFarms.value = emptyList()

        // Then
        verify(mockFarmRepository).deleteFarmById(farmToDelete)
        val updatedFarms = farmViewModel.farms.value
        assertNotNull(updatedFarms)
        assertTrue(updatedFarms!!.isEmpty()) // Farm was deleted
    }

    @Test
    fun `addFarm updates LiveData`() = runBlocking {
        // Given
        val newFarm = createTestFarm()
        val siteId = newFarm.siteId
        liveDataFarms.value = emptyList() // Initial state

        // When
        `when`(mockFarmRepository.isFarmDuplicateBoolean(newFarm)).thenReturn(false)
        farmViewModel.addFarm(newFarm, siteId)

        // Simulate repository adding farm
        liveDataFarms.value = listOf(newFarm)

        // Then
        val updatedFarms = farmViewModel.farms.value
        assertNotNull(updatedFarms)
        assertTrue(updatedFarms!!.contains(newFarm))
    }

    private fun createTestFarm(): Farm {
        return Farm(
            siteId = 1L,
            farmerPhoto = "photo.jpg",
            farmerName = "New Farmer",
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
    }
}

 */
