package org.technoserve.farmcollector.ui.components

import android.content.Context
import org.junit.Assert.*




import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.mockito.Mockito.mock

/*
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class RestoreDataAlertKtTest{

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

    @Test
    fun restoreDataAlertDisplaysContentCorrectly() {
        composeTestRule.setContent {
            RestoreDataAlert(
                showDialog = true,
                onDismiss = {},
                deviceId = "sampleDeviceId",
                farmViewModel = FarmViewModel(ApplicationProvider.getApplicationContext())
            )
        }

        // Assert dialog title and description
        composeTestRule.onNodeWithText("Data Restoration").assertExists()
        composeTestRule.onNodeWithText(
            "During restoration, you will recover some of the previously deleted records. Do you want to continue?"
        ).assertExists()

        // Assert buttons exist
        composeTestRule.onNodeWithText("Continue").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

//    @Test
//    fun restoreDataAlertCallsRestoreDataOnContinueClick() {
//        val mockFarmViewModel = mockk<FarmViewModel>(relaxed = true)
//        var isDismissed = false
//
//        composeTestRule.setContent {
//            RestoreDataAlert(
//                showDialog = true,
//                onDismiss = { isDismissed = true },
//                deviceId = "sampleDeviceId",
//                farmViewModel = mockFarmViewModel
//            )
//        }
//
//        // Click the "Continue" button
//        composeTestRule.onNodeWithText("Continue").performClick()
//
//        // Verify `restoreData` is called
//        verify {
//            mockFarmViewModel.restoreData(
//                deviceId = "sampleDeviceId",
//                phoneNumber = "",
//                email = "",
//                farmViewModel = mockFarmViewModel,
//                onCompletion = any()
//            )
//        }
//
//        // Assert dialog is dismissed
//        assert(isDismissed)
//    }

    @Test
    fun restoreDataAlertCallsOnDismissOnCancelClick() {
        var isDismissed = false

        composeTestRule.setContent {
            RestoreDataAlert(
                showDialog = true,
                onDismiss = { isDismissed = true },
                deviceId = "sampleDeviceId",
                farmViewModel = FarmViewModel(ApplicationProvider.getApplicationContext())
            )
        }

        // Click the "Cancel" button
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Assert dialog is dismissed
        assert(isDismissed)
    }

    @Test
    fun restoreDataAlertShowsSuccessMessageWhenRestoreSucceeds() {
        val mockFarmViewModel = mockk<FarmViewModel>(relaxed = true)
        var isDismissed = false

        every {
            mockFarmViewModel.restoreData(
                any(),
                any(),
                any(),
                any(),
                captureLambda()
            )
        } answers {
            lambda<(Boolean) -> Unit>().invoke(true)
        }

        composeTestRule.setContent {
            RestoreDataAlert(
                showDialog = true,
                onDismiss = { isDismissed = true },
                deviceId = "sampleDeviceId",
                farmViewModel = mockFarmViewModel
            )
        }

        // Click the "Continue" button
        composeTestRule.onNodeWithText("Continue").performClick()

        // Assert dialog is dismissed and success message logic executes
        assert(isDismissed)
    }

//    @Test
//    fun restoreDataAlertShowsFailureMessageWhenRestoreFails() {
//        val mockFarmViewModel = mockk<FarmViewModel>(relaxed = true)
//        var isDismissed = false
//
//        every {
//            mockFarmViewModel.restoreData(
//                any(),
//                any(),
//                any(),
//                any(),
//                captureLambda()
//            )
//        } answers {
//            lambda<(Boolean) -> Unit>().invoke(false)
//        }
//
//        composeTestRule.setContent {
//            RestoreDataAlert(
//                showDialog = true,
//                onDismiss = { isDismissed = true },
//                deviceId = "sampleDeviceId",
//                farmViewModel = mockFarmViewModel
//            )
//        }
//
//        // Click the "Continue" button
//        composeTestRule.onNodeWithText("Continue").performClick()
//
//        // Assert failure message logic executes
//        composeTestRule.onNodeWithText("Data restoration failed").assertExists()
//        assert(isDismissed)
//    }
}

 */