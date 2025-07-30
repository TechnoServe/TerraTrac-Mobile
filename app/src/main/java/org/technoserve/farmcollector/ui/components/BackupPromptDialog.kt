package org.technoserve.farmcollector.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import org.technoserve.farmcollector.utils.BackupPreferences
import org.technoserve.farmcollector.R

/**
 * A dialog that prompts the user to enable or disable data backup.
 *
 * @param context The context of the application.
 * @param navController The navigation controller to handle navigation actions.
 * @param showDialog A boolean indicating whether the dialog should be shown.
 * @param onDismiss A callback function to be called when the dialog is dismissed.
 */


@Composable
fun BackupPromptDialog(
    context: Context,
    navController: NavController,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* User must make a choice */ },
            title = { Text(stringResource(id = R.string.enable_data_backup_title)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.enable_backup_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.disable_backup_message))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            BackupPreferences.saveBackupChoice(context, true) // Save user's choice
                            onDismiss() // Close the dialog
                            navController.navigate("siteList") // Navigate after selection
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.enable_backup))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            BackupPreferences.saveBackupChoice(context, false) // Save user's choice
                            onDismiss() // Close the dialog
                            navController.navigate("siteList") // Navigate after selection
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.disable_backup))
                }
            }
        )
    }
}
