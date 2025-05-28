package org.technoserve.farmcollector.ui.components
/*
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.Farm

@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class FarmCardKtTest{
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

//    @Test
//    fun farmCardDisplaysContentCorrectly() {
//        val testFarm = Farm(
//            siteId = 1L,
//            farmerPhoto = "photo.jpg",
//            farmerName = "John Doe",
//            memberId = "12345",
//            village = "Sample Village",
//            district = "Sample District",
//            purchases = 10f,
//            size = 5.0f,
//            latitude = "12.34",
//            longitude = "56.78",
//            coordinates = listOf(Pair(12.34, 56.78)),
//            accuracyArray = listOf(5.0f),
//            synced = false,
//            scheduledForSync = false,
//            createdAt = System.currentTimeMillis(),
//            updatedAt = System.currentTimeMillis(),
//            needsUpdate = true
//        )
//
//        composeTestRule.setContent {
//            FarmCard(
//                farm = testFarm,
//                onCardClick = {},
//                onDeleteClick = {}
//            )
//        }
//
//        // Verify farmer name
//        composeTestRule.onNodeWithText("John Doe").assertExists()
//
//        // Verify farm size with formatting
//        composeTestRule.onNodeWithText("Size: 5.0 ha").assertExists()
//
//        // Verify village and district
//        composeTestRule.onNodeWithText("Village: Sample Village").assertExists()
//        composeTestRule.onNodeWithText("District: Sample District").assertExists()
//
//        // Verify the "needs update" label
//        composeTestRule.onNodeWithText("Needs update").assertExists()
//    }

    @Test
    fun farmCardHandlesCardClick() {
        var cardClicked = false
        composeTestRule.setContent {
            FarmCard(
                farm = Farm(
                    siteId = 1L,
                    farmerPhoto = "photo.jpg",
                    farmerName = "John Doe",
                    memberId = "12345",
                    village = "Sample Village",
                    district = "Sample District",
                    purchases = 10f,
                    size = 5.0f,
                    latitude = "12.34",
                    longitude = "56.78",
                    coordinates = listOf(Pair(12.34, 56.78)),
                    accuracyArray = listOf(5.0f),
                    synced = false,
                    scheduledForSync = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    needsUpdate = false
                ),
                onCardClick = { cardClicked = true },
                onDeleteClick = {}
            )
        }

        // Perform click on the card
        composeTestRule.onNodeWithText("John Doe").performClick()

        // Verify the click handler is called
        assert(cardClicked)
    }

    @Test
    fun farmCardHandlesDeleteClick() {
        var deleteClicked = false
        composeTestRule.setContent {
            FarmCard(
                farm = Farm(
                    siteId = 1L,
                    farmerPhoto = "photo.jpg",
                    farmerName = "John Doe",
                    memberId = "12345",
                    village = "Sample Village",
                    district = "Sample District",
                    purchases = 10f,
                    size = 5.0f,
                    latitude = "12.34",
                    longitude = "56.78",
                    coordinates = listOf(Pair(12.34, 56.78)),
                    accuracyArray = listOf(5.0f),
                    synced = false,
                    scheduledForSync = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    needsUpdate = false
                ),
                onCardClick = {},
                onDeleteClick = { deleteClicked = true }
            )
        }

        // Perform click on the delete button
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Verify the delete click handler is called
        assert(deleteClicked)
    }

    @Test
    fun farmCardDoesNotShowNeedsUpdateIfFalse() {
        composeTestRule.setContent {
            FarmCard(
                farm = Farm(
                    siteId = 1L,
                    farmerPhoto = "photo.jpg",
                    farmerName = "John Doe",
                    memberId = "12345",
                    village = "Sample Village",
                    district = "Sample District",
                    purchases = 10f,
                    size = 5.0f,
                    latitude = "12.34",
                    longitude = "56.78",
                    coordinates = listOf(Pair(12.34, 56.78)),
                    accuracyArray = listOf(5.0f),
                    synced = false,
                    scheduledForSync = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    needsUpdate = false
                ),
                onCardClick = {},
                onDeleteClick = {}
            )
        }

        // Verify the "needs update" label does not exist
        composeTestRule.onNodeWithText("Needs update").assertDoesNotExist()
    }
}

 */