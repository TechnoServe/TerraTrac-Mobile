package org.technoserve.farmcollector.ui.components
/*
import android.content.Context
import androidx.compose.runtime.mutableStateOf
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
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.CollectionSite

@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class SiteCardKtTest{
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
//    fun siteCardDisplaysContentCorrectly() {
//        val testSite = CollectionSite(
//                name = "Sample Site",
//                agentName = "John Agent",
//                phoneNumber = "",
//                email = "john@example.com",
//                village = "Sample Village",
//                district = "Sample District",
//                createdAt = System.currentTimeMillis(),
//                updatedAt = System.currentTimeMillis()
//        )
//        val totalFarms = 10
//        val farmsWithIncompleteData = 2
//
//        composeTestRule.setContent {
//            SiteCard(
//                site = testSite,
//                onCardClick = {},
//                totalFarms = totalFarms,
//                farmsWithIncompleteData = farmsWithIncompleteData,
//                onDeleteClick = {},
//                farmViewModel = mock()
//            )
//        }
//
//        // Verify site name, agent name, and phone number
//        composeTestRule.onNodeWithText("Sample Site").assertExists()
//        composeTestRule.onNodeWithText("Agent name: John Agent").assertExists()
//        composeTestRule.onNodeWithText("Phone number: 1234567890").assertExists()
//
//        // Verify total farms and incomplete farms
//        composeTestRule.onNodeWithText("Total farms: 10").assertExists()
//        composeTestRule.onNodeWithText("Farms with incomplete data: 2").assertExists()
//    }

//    @Test
//    fun siteCardHandlesCardClick() {
//        var cardClicked = false
//
//        composeTestRule.setContent {
//            SiteCard(
//                site = CollectionSite(
//                    name = "Sample Site",
//                    agentName = "John Agent",
//                    phoneNumber = "",
//                    email = "john@example.com",
//                    village = "Sample Village",
//                    district = "Sample District",
//                    createdAt = System.currentTimeMillis(),
//                    updatedAt = System.currentTimeMillis()
//                ),
//                onCardClick = { cardClicked = true },
//                totalFarms = 10,
//                farmsWithIncompleteData = 2,
//                onDeleteClick = {},
//                farmViewModel = mock()
//            )
//        }
//
//        // Perform click on the card
//        composeTestRule.onNodeWithText("Sample Site").performClick()
//
//        // Verify the click handler is called
//        assert(cardClicked)
//    }

//    @Test
//    fun siteCardHandlesDeleteClick() {
//        var deleteClicked = false
//
//        composeTestRule.setContent {
//            SiteCard(
//                site = CollectionSite(
//                    name = "Sample Site",
//                    agentName = "John Agent",
//                    phoneNumber = "",
//                    email = "john@example.com",
//                    village = "Sample Village",
//                    district = "Sample District",
//                    createdAt = System.currentTimeMillis(),
//                    updatedAt = System.currentTimeMillis()
//                ),
//                onCardClick = {},
//                totalFarms = 10,
//                farmsWithIncompleteData = 2,
//                onDeleteClick = { deleteClicked = true },
//                farmViewModel = mock()
//            )
//        }
//
//        // Perform click on the delete button
//        composeTestRule.onNodeWithContentDescription("Delete").performClick()
//
//        // Verify the delete click handler is called
//        assert(deleteClicked)
//    }

//    @Test
//    fun siteCardOpensUpdateDialogOnEditClick() {
//        val showDialog = mutableStateOf(false)
//
//        composeTestRule.setContent {
//            SiteCard(
//                site = CollectionSite(
//                    name = "Sample Site",
//                    agentName = "John Agent",
//                    phoneNumber = "",
//                    email = "john@example.com",
//                    village = "Sample Village",
//                    district = "Sample District",
//                    createdAt = System.currentTimeMillis(),
//                    updatedAt = System.currentTimeMillis()
//                ),
//                onCardClick = {},
//                totalFarms = 10,
//                farmsWithIncompleteData = 2,
//                onDeleteClick = {},
//                farmViewModel = mock()
//            )
//        }
//
//        // Perform click on the edit button
//        composeTestRule.onNodeWithContentDescription("Update").performClick()
//
//        // Assert the dialog is now displayed
//        composeTestRule.onNodeWithText("Update Collection Dialog").assertExists() // Adjust this to match the dialog's content
//    }

//    @Test
//    fun siteCardHidesPhoneNumberIfEmpty() {
//        val testSite = CollectionSite(
//            name = "Sample Site",
//            agentName = "John Agent",
//            phoneNumber = "",
//            email = "john@example.com",
//            village = "Sample Village",
//            district = "Sample District",
//            createdAt = System.currentTimeMillis(),
//            updatedAt = System.currentTimeMillis()
//        )
//
//        composeTestRule.setContent {
//            SiteCard(
//                site = testSite,
//                onCardClick = {},
//                totalFarms = 10,
//                farmsWithIncompleteData = 2,
//                onDeleteClick = {},
//                farmViewModel = mock()
//            )
//        }
//
//        // Verify the phone number does not exist
//        composeTestRule.onNodeWithText("Phone number: 1234567890").assertDoesNotExist()
//    }
}

 */