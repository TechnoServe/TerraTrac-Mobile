package org.technoserve.farmcollector.database.mappers

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.technoserve.farmcollector.database.dao.FarmDAO
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Farm

class FarmMappersKtTest{
    private lateinit var mockFarmDAO: FarmDAO

    @Before
    fun setUp() {
        mockFarmDAO = mock(FarmDAO::class.java)
    }

    @Test
    fun `toDeviceFarmDtoList with valid input`() {
        val deviceId = "device123"
        val collectionSite = CollectionSite(
            name = "Site Name",
            agentName = "Agent Name",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Village A",
            district = "District A",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val farms = listOf(
            Farm(
                siteId = 1L,
                farmerPhoto = "photo.jpg",
                farmerName = "Farmer A",
                memberId = "M001",
                village = "Village A",
                district = "District X",
                purchases = 10f,
                size = 1.5f,
                latitude = "12.34",
                longitude = "56.78",
                coordinates = listOf(Pair(12.34, 56.78)),
                accuracyArray = listOf(5.0f, null, 3.5f),
                synced = false,
                scheduledForSync = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                needsUpdate = true
            ),
            Farm(
                siteId = 1L,
                farmerPhoto = "photo.jpg",
                farmerName = "Farmer B",
                memberId = "M002",
                village = "Village B",
                district = "District B",
                purchases = 10f,
                size = 2.0f,
                latitude = "23.45",
                longitude = "67.89",
                coordinates = listOf(Pair(23.45, 67.89)),
                accuracyArray = null,
                synced = false,
                scheduledForSync = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                needsUpdate = true
            )
        )
        `when`(mockFarmDAO.getCollectionSiteById(1L)).thenReturn(collectionSite)

        val result = farms.toDeviceFarmDtoList(deviceId, mockFarmDAO)

        assertEquals(1, result.size) // Only one DeviceFarmDto because farms are grouped by siteId
        val deviceFarmDto = result.first()
        assertEquals(deviceId, deviceFarmDto.device_id)
        assertEquals("Site Name", deviceFarmDto.collection_site.name)
        assertEquals(2, deviceFarmDto.farms.size)
        assertEquals("Farmer A", deviceFarmDto.farms[0].farmer_name)
        assertEquals(listOf(12.34, 56.78), deviceFarmDto.farms[0].coordinates?.first() ?: "null")
    }

    @Test
    fun `toDeviceFarmDtoList with missing collection site`() {
        val deviceId = "device123"
        val farms = listOf(
            Farm(
                siteId = 1L,
                farmerPhoto = "photo.jpg",
                farmerName = "Farmer A",
                memberId = "M001",
                village = "Village A",
                district = "District X",
                purchases = 10f,
                size = 1.5f,
                latitude = "12.34",
                longitude = "56.78",
                coordinates = listOf(Pair(12.34, 56.78)),
                accuracyArray = null,
                synced = false,
                scheduledForSync = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                needsUpdate = true
            )
        )
        `when`(mockFarmDAO.getCollectionSiteById(1L)).thenReturn(null)

        val result = farms.toDeviceFarmDtoList(deviceId, mockFarmDAO)

        assertTrue(result.isEmpty()) // No DeviceFarmDto should be created
    }

    @Test
    fun `toDeviceFarmDtoList with invalid latitude and longitude`() {
        val deviceId = "device123"
        val collectionSite = CollectionSite(
            name = "Site Name",
            agentName = "Agent Name",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Village A",
            district = "District A",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val farms = listOf(
            Farm(
                siteId = 1L,
                farmerPhoto = "photo.jpg",
                farmerName = "Farmer A",
                memberId = "M001",
                village = "Village A",
                district = "District X",
                purchases = 10f,
                size = 1.5f,
                latitude = "invalid",
                longitude = "",
                coordinates = null,
                accuracyArray = null,
                synced = false,
                scheduledForSync = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                needsUpdate = true
            )
        )
        `when`(mockFarmDAO.getCollectionSiteById(1L)).thenReturn(collectionSite)

        val result = farms.toDeviceFarmDtoList(deviceId, mockFarmDAO)

        assertEquals(1, result.size)
        val farmDto = result.first().farms.first()
        assertEquals(0.0, farmDto.latitude, 0.0)
        assertEquals(0.0, farmDto.longitude, 0.0)
    }

    @Test
    fun `toDeviceFarmDtoList with empty input list`() {
        val deviceId = "device123"
        val farms = emptyList<Farm>()

        val result = farms.toDeviceFarmDtoList(deviceId, mockFarmDAO)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDeviceFarmDtoList handles null or empty accuracyArray and coordinates`() {
        val deviceId = "device123"
        val collectionSite = CollectionSite(
            name = "Site Name",
            agentName = "Agent Name",
            phoneNumber = "1234567890",
            email = "agent@example.com",
            village = "Village A",
            district = "District A",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val farms = listOf(
            Farm(
                siteId = 1L,
                farmerPhoto = "photo.jpg",
                farmerName = "Farmer A",
                memberId = "M001",
                village = "Village A",
                district = "District X",
                purchases = 10f,
                size = 1.5f,
                latitude = "invalid",
                longitude = "",
                coordinates = null,
                accuracyArray = null,
                synced = false,
                scheduledForSync = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                needsUpdate = true
            )
        )
        `when`(mockFarmDAO.getCollectionSiteById(1L)).thenReturn(collectionSite)

        val result = farms.toDeviceFarmDtoList(deviceId, mockFarmDAO)

        assertEquals(1, result.size)
        val farmDto = result.first().farms.first()
        farmDto.coordinates?.let { assertTrue(it.isEmpty()) }
        assertNull(farmDto.accuracies)
    }
}