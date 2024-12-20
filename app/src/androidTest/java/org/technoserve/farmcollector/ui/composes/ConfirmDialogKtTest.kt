package org.technoserve.farmcollector.ui.composes

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ConfirmDialogKtTest{
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confirmDialog_showsTitleAndMessage_whenDialogVisible() {
        val showDialog = mutableStateOf(true)
        val title = "Confirm Action"
        val message = "Are you sure you want to proceed?"

        composeTestRule.setContent {
            ConfirmDialog(
                title = title,
                message = message,
                showDialog = showDialog,
                onProceedFn = {},
                onCancelFn = {}
            )
        }

        // Verify the title and message are displayed
        composeTestRule.onNodeWithText(title).assertExists()
        composeTestRule.onNodeWithText(message).assertExists()
    }

    @Test
    fun confirmDialog_callsOnProceedFn_whenYesButtonClicked() {
        val showDialog = mutableStateOf(true)
        var onProceedCalled = false

        composeTestRule.setContent {
            ConfirmDialog(
                title = "Confirm Action",
                message = "Are you sure?",
                showDialog = showDialog,
                onProceedFn = { onProceedCalled = true },
                onCancelFn = {}
            )
        }

        // Click the "Yes" button
        composeTestRule.onNodeWithText("Yes").performClick()

        // Verify the onProceed function is called
        assert(onProceedCalled)
    }

    @Test
    fun confirmDialog_callsOnCancelFn_whenNoButtonClicked() {
        val showDialog = mutableStateOf(true)
        var onCancelCalled = false

        composeTestRule.setContent {
            ConfirmDialog(
                title = "Confirm Action",
                message = "Are you sure?",
                showDialog = showDialog,
                onProceedFn = {},
                onCancelFn = { onCancelCalled = true }
            )
        }

        // Click the "No" button
        composeTestRule.onNodeWithText("No").performClick()

        // Verify the onCancel function is called
        assert(onCancelCalled)
    }

    @Test
    fun confirmDialog_doesNotShow_whenShowDialogIsFalse() {
        val showDialog = mutableStateOf(false)

        composeTestRule.setContent {
            ConfirmDialog(
                title = "Confirm Action",
                message = "Are you sure?",
                showDialog = showDialog,
                onProceedFn = {},
                onCancelFn = {}
            )
        }

        // Verify that the dialog content does not exist
        composeTestRule.onNodeWithText("Confirm Action").assertDoesNotExist()
        composeTestRule.onNodeWithText("Are you sure?").assertDoesNotExist()
    }
}