package test.viewmodel

//import android.app.Application
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.Observer
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.runBlocking
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.Mockito
//import org.technoserve.farmcollector.database.Farm
//import org.technoserve.farmcollector.database.FarmRepository
//import org.technoserve.farmcollector.database.FarmViewModel
//
//class FarmViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var farmViewModel: FarmViewModel
//    private lateinit var farmRepository: FarmRepository
//    private val applicationContext = ApplicationProvider.getApplicationContext<Application>()
//    @Before
//    fun setup() {
//        farmRepository = Mockito.mock(FarmRepository::class.java)
//        farmViewModel = FarmViewModel(application = applicationContext)
//    }
//
//    fun createSampleFarmsLiveData(): LiveData<List<Farm>> {
//        // Create a sample list of Farm objects
//        val sampleFarms = listOf(
//            Farm(
//                siteId = 1,
//                farmerPhoto = "https://example.com/photos/farmer1.jpg",
//                farmerName = "Alice Smith",
//                memberId = "M001",
//                village = "Village A",
//                district = "District 1",
//                purchases = 10.5f,
//                size = 5.0f,
//                latitude = "12.345678",
//                longitude = "98.765432",
//                coordinates = listOf(Pair(12.345678, 98.765432)),
//                accuracyArray = listOf(0.95f),
//                createdAt = System.currentTimeMillis(),
//                updatedAt = System.currentTimeMillis()
//            ),
//            Farm(
//                siteId = 2,
//                farmerPhoto = "https://example.com/photos/farmer2.jpg",
//                farmerName = "Bob Johnson",
//                memberId = "M002",
//                village = "Village B",
//                district = "District 2",
//                purchases = 20.0f,
//                size = 10.0f,
//                latitude = "23.456789",
//                longitude = "87.654321",
//                coordinates = listOf(Pair(23.456789, 87.654321)),
//                accuracyArray = listOf(0.90f),
//                createdAt = System.currentTimeMillis(),
//                updatedAt = System.currentTimeMillis()
//            ),
//            Farm(
//                siteId = 3,
//                farmerPhoto = "https://example.com/photos/farmer3.jpg",
//                farmerName = "Charlie Brown",
//                memberId = "M003",
//                village = "Village C",
//                district = "District 3",
//                purchases = null,
//                size = 15.0f,
//                latitude = "34.567890",
//                longitude = "76.543210",
//                coordinates = listOf(Pair(34.567890, 76.543210)),
//                accuracyArray = null,
//                createdAt = System.currentTimeMillis(),
//                updatedAt = System.currentTimeMillis()
//            )
//        )
//
//        // Wrap the list in a MutableLiveData
//        val liveDataFarms = MutableLiveData<List<Farm>>()
//        liveDataFarms.value = sampleFarms
//
//        return liveDataFarms
//    }
//
//
//
//
//
//    @Test
//    fun `test readData returns list of farms`() = runBlocking {
//        // Arrange
//        val expectedFarms = listOf(
//            Farm(
//                siteId = 1,
//                farmerPhoto = "https://example.com/photos/farmer1.jpg",
//                farmerName = "Alice Smith",
//                memberId = "M001",
//                village = "Village A",
//                district = "District 1",
//                purchases = 10.5f,
//                size = 5.0f,
//                latitude = "12.345678",
//                longitude = "98.765432",
//                coordinates = listOf(Pair(12.345678, 98.765432)),
//                accuracyArray = listOf(0.95f),
//                createdAt = System.currentTimeMillis(),
//                updatedAt = System.currentTimeMillis()
//            )
//        )
//
//        // Mock LiveData to return expected list
//        val liveDataFarms = MutableLiveData(expectedFarms)
//        Mockito.`when`(farmRepository.readData).thenReturn(liveDataFarms)
//
//        // Create an observer to observe the LiveData
//        val observer = Mockito.mock(Observer::class.java) as Observer<List<Farm>>
//        farmViewModel.readData.observeForever(observer)
//
//        // Act & Assert
//        Mockito.verify(observer).onChanged(expectedFarms)
//    }
//
//}