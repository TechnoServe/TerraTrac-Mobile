package org.technoserve.farmcollector.ui.components

import org.junit.Assert.*

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class FakeMapViewModel : ViewModel() {
    var coordinatesCleared = false

    fun clearCoordinates() {
        coordinatesCleared = true
    }
}

@RunWith(AndroidJUnit4::class)
class KeepPolygonDialogIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testKeepPolygonDialogClearsCoordinatesAndCapturesNew() {
        val fakeViewModel = FakeMapViewModel()

        composeTestRule.setContent {
            KeepPolygonDialog(
                onDismiss = {},
                onKeepExisting = {},
                onCaptureNew = {
                    fakeViewModel.clearCoordinates()
                }
            )
        }

        // Click the "Capture New" button
        composeTestRule.onNodeWithText("Capture New").performClick()

        // Assert that coordinates were cleared
        assert(fakeViewModel.coordinatesCleared)
    }
}
