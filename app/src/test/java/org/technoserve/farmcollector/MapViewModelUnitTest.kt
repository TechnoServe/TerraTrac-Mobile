package org.technoserve.farmcollector
/*
import dagger.hilt.android.testing.HiltAndroidRule
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.technoserve.farmcollector.viewmodels.MapViewModel

@RunWith(RobolectricTestRunner::class)
class MapViewModelKtTest {


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        hiltRule.inject() // Inject dependencies
        viewModel = MapViewModel() // No dependency to mock since the constructor is empty
    }

    @Test
    fun testAddCoordinate() {
        val lat = 12.345678
        val lng = 98.765432

        viewModel.addCoordinate(lat, lng)

        val clusterItems = viewModel.state.value.clusterItems
        assert(clusterItems.isNotEmpty())
        assertEquals("zone-0", clusterItems.last().id)
    }
}
*/