package org.technoserve.farmcollector.ui.components
/*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.work.Configuration
import androidx.work.WorkManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.screens.farms.Action

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CustomizedConfirmationDialogKtTest {

//    @Before
//    fun setUp() {
//        val config = Configuration.Builder()
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//        WorkManager.initialize(RuntimeEnvironment.application, config)
//    }


    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleFarms = listOf(


        Farm(
            siteId = 1L,
            farmerPhoto = "photo.jpg",
            farmerName = "John Doe",
            memberId = "12345",
            village = "Village1",
            district = "District1",
            purchases = 10f,
            size = 1.0f,
            latitude = "1.234",
            longitude = "2.345",
            coordinates = listOf(Pair(12.34, 56.78)),
            accuracyArray = listOf(5.0f),
            synced = false,
            scheduledForSync = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            needsUpdate = true
        ),
        Farm(
            siteId = 1L,
            farmerPhoto = "photo.jpg",
            farmerName = "",
            memberId = "12345",
            village = "Village1",
            district = "District1",
            purchases = 10f,
            size = 1.0f,
            latitude = "0.0",
            longitude = "0.0",
            coordinates = listOf(Pair(0.0, 0.0)),
            accuracyArray = listOf(5.0f),
            synced = false,
            scheduledForSync = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            needsUpdate = true
        ), // Incomplete
        Farm(
            siteId = 1L,
            farmerPhoto = "photo.jpg",
            farmerName = "Jane Doe",
            memberId = "12345",
            village = "Village3",
            district = "District3",
            purchases = 10f,
            size = 2.0f,
            latitude = "3.456",
            longitude = "4.567",
            coordinates = listOf(Pair(3.456, 4.567)),
            accuracyArray = listOf(1.0f),
            synced = false,
            scheduledForSync = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            needsUpdate = true
        )
    )

//    @Test
//    fun customizedConfirmationDialogDisplaysCorrectMessageForExport() {
//        composeTestRule.setContent {
//            CustomizedConfirmationDialog(
//                listItems = sampleFarms,
//                action = Action.Export,
//                onConfirm = {},
//                onDismiss = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Confirm").assertExists()
//        composeTestRule.onNodeWithText("You are about to export 3 farms, including 1 incomplete farm.")
//            .assertExists() // Assuming proper string resources
//    }

//    @Test
//    fun customizedConfirmationDialogDisplaysCorrectMessageForShare() {
//        composeTestRule.setContent {
//            CustomizedConfirmationDialog(
//                listItems = sampleFarms,
//                action = Action.Share,
//                onConfirm = {},
//                onDismiss = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("Confirm").assertExists()
//        composeTestRule.onNodeWithText("You are about to share 3 farms, including 1 incomplete farm.")
//            .assertExists() // Assuming proper string resources
//    }

    @Test
    fun customizedConfirmationDialogTriggersOnConfirm() {
        var confirmed = false
        var dismissed = false

        composeTestRule.setContent {
            CustomizedConfirmationDialog(
                listItems = sampleFarms,
                action = Action.Export,
                onConfirm = { confirmed = true },
                onDismiss = { dismissed = true }
            )
        }

        // Click the confirm button
        composeTestRule.onNodeWithText("Yes").performClick()

        // Verify that onConfirm and onDismiss are triggered
        assert(confirmed)
        assert(dismissed)
    }

    @Test
    fun customizedConfirmationDialogTriggersOnDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            CustomizedConfirmationDialog(
                listItems = sampleFarms,
                action = Action.Share,
                onConfirm = {},
                onDismiss = { dismissed = true }
            )
        }

        // Click the dismiss button
        composeTestRule.onNodeWithText("No").performClick()

        // Verify that onDismiss is triggered
        assert(dismissed)
    }

    @Test
    fun customizedConfirmationDialogValidatesFarmsCorrectly() {
        val incompleteFarms = sampleFarms.filter {
            it.farmerName.isEmpty() ||
                    it.district.isEmpty() ||
                    it.village.isEmpty() ||
                    it.latitude == "0.0" ||
                    it.longitude == "0.0" ||
                    it.size == 0.0f ||
                    it.remoteId.toString().isEmpty()
        }

        assert(incompleteFarms.size == 1)
    }
}

 */