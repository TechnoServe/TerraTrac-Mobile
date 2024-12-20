package org.technoserve.farmcollector.ui.components

/*
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
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

@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class KeepPolygonDialogKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create the configuration for WorkManager
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

//    @Test
//    fun testKeepPolygonDialogDisplaysCorrectly() {
//        composeTestRule.setContent {
//            KeepPolygonDialog(
//                onDismiss = {},
//                onKeepExisting = {},
//                onCaptureNew = {}
//            )
//        }
//
//        // Verify the dialog title and text are displayed
//        composeTestRule.onNodeWithText("Update Polygon").assertExists()
//        composeTestRule.onNodeWithText("Keep existing polygon or capture new").assertExists()
//
//        // Verify the buttons are displayed
//        composeTestRule.onNodeWithText("Keep Existing").assertExists()
//        composeTestRule.onNodeWithText("Capture New").assertExists()
//    }

    @Test
    fun testKeepPolygonDialogButtonActions() {
        var keepExistingClicked = false
        var captureNewClicked = false

        composeTestRule.setContent {
            KeepPolygonDialog(
                onDismiss = {},
                onKeepExisting = { keepExistingClicked = true },
                onCaptureNew = { captureNewClicked = true }
            )
        }

        // Click the "Keep Existing" button
        composeTestRule.onNodeWithText("Keep Existing").performClick()
        assert(keepExistingClicked)

        // Click the "Capture New" button
        composeTestRule.onNodeWithText("Capture New").performClick()
        assert(captureNewClicked)
    }
}

 */
