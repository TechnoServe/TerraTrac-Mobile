package org.technoserve.farmcollector.ui.composes

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AreaDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val calculatedArea = 123.456789
    private val enteredArea = 100.0
    private val threshold = enteredArea * 0.30

    @Test
    fun dialogDisplaysCorrectTextAndButtons() {
        composeTestRule.setContent {
            AreaDialog(
                showDialog = true,
                onDismiss = {},
                onConfirm = {},
                calculatedArea = calculatedArea,
                enteredArea = enteredArea
            )
        }

        // Check dialog title
        composeTestRule.onNodeWithText("Choose Area") // Replace with localized string if using resources
            .assertExists()

        // Check warning message visibility
        val difference = Math.abs(calculatedArea - enteredArea)
        if (difference > threshold) {
            composeTestRule.onNodeWithText("Warning: Difference is $difference") // Replace with localized string
                .assertExists()
        } else {
            composeTestRule.onNodeWithText("Warning: Difference is $difference") // Replace with localized string
                .assertDoesNotExist()
        }

        // Verify buttons
        composeTestRule.onNodeWithText("Cancel") // Replace with localized string
            .assertExists()

        composeTestRule.onNodeWithText("Calculated Area: 123.456789") // Replace with localized string
            .assertExists()

        composeTestRule.onNodeWithText("Entered Area: 100.00") // Replace with localized string
            .assertExists()
    }

    @Test
    fun confirmButtonCallsOnConfirmWithCalculatedAreaOption() {
        var confirmedOption: String? = null

        composeTestRule.setContent {
            AreaDialog(
                showDialog = true,
                onDismiss = {},
                onConfirm = { confirmedOption = it },
                calculatedArea = calculatedArea,
                enteredArea = enteredArea
            )
        }

        // Click the calculated area button
        composeTestRule.onNodeWithText("Calculated Area: 123.456789") // Replace with localized string
            .performClick()

        // Verify the onConfirm callback was called with the correct option
        assertEquals(CALCULATED_AREA_OPTION, confirmedOption)
    }

    @Test
    fun dismissButtonCallsOnDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            AreaDialog(
                showDialog = true,
                onDismiss = { dismissed = true },
                onConfirm = {},
                calculatedArea = calculatedArea,
                enteredArea = enteredArea
            )
        }

        // Click the dismiss button
        composeTestRule.onNodeWithText("Cancel") // Replace with localized string
            .performClick()

        // Verify the onDismiss callback was called
        assertTrue(dismissed)
    }
}
