package org.technoserve.farmcollector.ui.composes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


fun toggleDialog(showDialog: MutableState<Boolean>) {
    showDialog.value = false
}

fun executeCallback(callback: () -> Unit) {
    callback()
}

class ConfirmDialogKtTest {
    @Test
    fun `toggleDialog sets showDialog to false`() {
        val showDialog = mutableStateOf(true)

        toggleDialog(showDialog)

        assertFalse(showDialog.value)
    }

    @Test
    fun `executeCallback triggers the provided callback`() {
        var callbackCalled = false

        executeCallback { callbackCalled = true }

        assertTrue(callbackCalled)
    }
}