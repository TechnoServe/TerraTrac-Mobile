package org.technoserve.farmcollector.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.viewmodels.FarmViewModel

/**
 * This component displays a dialog asking the user if they want to restore deleted data.
 *
 * @param showDialog - Boolean state indicating whether the dialog should be displayed.
 * @param onDismiss - Function to call when the dialog is dismissed.
 * @param deviceId - String representing the device ID.
 * @param farmViewModel - ViewModel for the farm data.
 */
@Composable
fun RestoreDataAlert(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    deviceId: String,
    farmViewModel: FarmViewModel,
) {
    val context = LocalContext.current
    var finalMessage by remember { mutableStateOf("") }
    var showFinalMessage by remember { mutableStateOf(false) }
    var showRestorePrompt by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Data Restoration") },
            text = {
                Text("During restoration, you will recover some of the previously deleted records. Do you want to continue?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        farmViewModel.restoreData(
                            deviceId = deviceId,
                            phoneNumber = "",
                            email = "",
                            farmViewModel = farmViewModel
                        ) { success ->
                            if (success) {
                                finalMessage = context.getString(R.string.data_restored_successfully)
                            } else {
                                showFinalMessage = true
                                showRestorePrompt = true
                            }
                            onDismiss()
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}