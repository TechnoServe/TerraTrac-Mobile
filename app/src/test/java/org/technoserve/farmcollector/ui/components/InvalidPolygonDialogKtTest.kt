package org.technoserve.farmcollector.ui.components

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
/*
@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class InvalidPolygonDialogKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
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
//    fun invalidPolygonDialogDisplaysCorrectly() {
//        // Create a mutable state to control the visibility of the dialog
//        val showDialog = mutableStateOf(true)
//
//        // Set up the dialog
//        composeTestRule.setContent {
//            InvalidPolygonDialog(
//                showDialog = showDialog,
//                onDismiss = {}
//            )
//        }
//
//        // Verify that the dialog title and message are displayed
//        composeTestRule.onNodeWithText("Invalid Polygon").assertExists() // Assuming string resource is "invalid_polygon_title"
//        composeTestRule.onNodeWithText("The polygon is invalid. Please check the coordinates.").assertExists() // Assuming string resource is "invalid_polygon_message"
//    }

    @Test
    fun invalidPolygonDialogDismissesWhenConfirmButtonClicked() {
        var dismissed = false
        val showDialog = mutableStateOf(true)

        // Set up the dialog with the dismiss callback
        composeTestRule.setContent {
            InvalidPolygonDialog(
                showDialog = showDialog,
                onDismiss = { dismissed = true }
            )
        }

        // Verify the dialog is visible initially
        composeTestRule.onNodeWithText("Invalid Polygon").assertExists()

        // Click the confirm button
        composeTestRule.onNodeWithText("OK").performClick()

        // Verify that the dismiss callback is called
        assert(dismissed)
    }

    @Test
    fun invalidPolygonDialogDoesNotShowWhenShowDialogIsFalse() {
        val showDialog = mutableStateOf(false)

        // Set up the dialog
        composeTestRule.setContent {
            InvalidPolygonDialog(
                showDialog = showDialog,
                onDismiss = {}
            )
        }

        // Verify that the dialog is not shown
        composeTestRule.onNodeWithText("Invalid Polygon").assertDoesNotExist()
    }

//    @Test
//    fun invalidPolygonDialogDismissesWhenDialogIsDismissed() {
//        val showDialog = mutableStateOf(true)
//        var dismissed = false
//
//        // Set up the dialog with the dismiss callback
//        composeTestRule.setContent {
//            InvalidPolygonDialog(
//                showDialog = showDialog,
//                onDismiss = { dismissed = true }
//            )
//        }
//
//        // Click outside the dialog to dismiss it
//        composeTestRule.onNodeWithTag("DialogBackground").performClick()
//
//        // Verify that the dialog is dismissed when the background is clicked
//        assert(dismissed)
//    }

    @Test
    fun invalidPolygonDialogRetainsStateWhenShowDialogIsTrue() {
        val showDialog = mutableStateOf(true)

        // Set up the dialog
        composeTestRule.setContent {
            InvalidPolygonDialog(
                showDialog = showDialog,
                onDismiss = {}
            )
        }

        // Verify the dialog is displayed initially
        composeTestRule.onNodeWithText("Invalid Polygon").assertExists()

        // Now set showDialog to false and verify the dialog is no longer visible
        showDialog.value = false
        composeTestRule.onNodeWithText("Invalid Polygon").assertDoesNotExist()
    }
}

 */
