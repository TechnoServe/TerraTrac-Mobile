package org.technoserve.farmcollector.ui.components

/*
import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.testng.junit.JUnit4TestRunner




@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
//@Config(
//    sdk = [33],
//    manifest = Config.NONE,
//    instrumentedPackages = ["androidx.compose.ui.test"]
//)
class CustomPaginationControlsKtTest{

    @get:Rule
    val composeTestRule = createComposeRule()

//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//
//        // Create the configuration for WorkManager
//        val config = Configuration.Builder()
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//
//        // Initialize WorkManager for testing
//        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
//    }
//
//    @After
//    fun tearDown() {
//        WorkManagerTestInitHelper.getTestDriver()?.let {
//            WorkManager.getInstance(ApplicationProvider.getApplicationContext()).cancelAllWork()
//            WorkManager.getInstance(ApplicationProvider.getApplicationContext()).pruneWork()
//            System.gc() // Force garbage collection to release locked resources
//        }
//    }


//    private val mockWorkManager: WorkManager = mock(WorkManager::class.java)
//
//    @Before
//    fun setUp() {
//        // Create a mock Operation
//        val mockOperation = mock(Operation::class.java)
//
//        // Create a mock LiveData<Operation.State>
//        val mockStateLiveData = mock(LiveData::class.java) as LiveData<Operation.State>
//        val mockState = mock(Operation.State::class.java)
//
//        // Stub the state to return a LiveData with a mock state
//        `when`(mockOperation.state).thenReturn(mockStateLiveData)
//
//        // Stub the LiveData to return a specific state
//        `when`(mockStateLiveData.value).thenReturn(mockState)
//
//        // Mock WorkManager's enqueue method to return the mockOperation
//        `when`(mockWorkManager.enqueue(any(WorkRequest::class.java))).thenReturn(mockOperation)
//
//        // Mock cancelAllWork to do nothing
//        doNothing().`when`(mockWorkManager).cancelAllWork()
//    }

    @Before
    fun setUp() {
        // Set up any necessary configurations
        System.setProperty("robolectric.build.model", "device")
        System.setProperty("robolectric.enabledSdks", "33")
    }




    @Test
    fun paginationDisplaysCorrectCurrentAndTotalPages() {
        val currentPage = 3
        val totalPages = 5

        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = {}
            )
        }

        // Verify the displayed text for current and total pages
        composeTestRule.onNodeWithText("Page $currentPage of $totalPages").assertExists()
    }

    @Test
    fun previousButtonIsDisabledOnFirstPage() {
        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = 1,
                totalPages = 5,
                onPageChange = {}
            )
        }

        // Verify that the "Previous Page" button is disabled
        composeTestRule.onNodeWithContentDescription("Previous Page")
            .assertIsNotEnabled()
    }

    @Test
    fun nextButtonIsDisabledOnLastPage() {
        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = 5,
                totalPages = 5,
                onPageChange = {}
            )
        }

        // Verify that the "Next Page" button is disabled
        composeTestRule.onNodeWithContentDescription("Next Page")
            .assertIsNotEnabled()
    }

    @Test
    fun previousButtonTriggersOnPageChangeCorrectly() {
        var pageChangedTo = -1

        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = 3,
                totalPages = 5,
                onPageChange = { page -> pageChangedTo = page }
            )
        }

        // Click the "Previous Page" button
        composeTestRule.onNodeWithContentDescription("Previous Page")
            .performClick()

        // Verify the page changed to the correct value
        assert(pageChangedTo == 2)
    }

    @Test
    fun nextButtonTriggersOnPageChangeCorrectly() {
        var pageChangedTo = -1

        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = 3,
                totalPages = 5,
                onPageChange = { page -> pageChangedTo = page }
            )
        }

        // Click the "Next Page" button
        composeTestRule.onNodeWithContentDescription("Next Page")
            .performClick()

        // Verify the page changed to the correct value
        assert(pageChangedTo == 4)
    }

    @Test
    fun buttonsAreDisabledWhenOnlyOnePage() {
        composeTestRule.setContent {
            CustomPaginationControls(
                currentPage = 1,
                totalPages = 1,
                onPageChange = {}
            )
        }

        // Verify both "Previous Page" and "Next Page" buttons are disabled
        composeTestRule.onNodeWithContentDescription("Previous Page").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Next Page").assertIsNotEnabled()
    }
}
 */