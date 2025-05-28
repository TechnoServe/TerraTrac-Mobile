package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R

//@Composable
//fun BackupConfirmationDialog(
//    isEnablingBackup: Boolean,
//    onConfirm: () -> Unit,
//    onCancel: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onCancel,
//        title = { Text(if (isEnablingBackup) "Enable Backup?" else "Disable Backup?") },
//        text = {
//            Column {
//                Text(
//                    if (isEnablingBackup) {
//                        "Enabling backup will store your data securely on the server. You can restore it if your device is lost."
//                    } else {
//                        "Disabling backup means your data will only be stored on your device. If lost, it cannot be restored."
//                    }
//                )
//                Spacer(modifier = Modifier.height(10.dp))
//                Text("Do you want to proceed?")
//            }
//        },
//        confirmButton = {
//            Button(onClick = onConfirm) {
//                Text("Confirm")
//            }
//        },
//        dismissButton = {
//            Button(onClick = onCancel) {
//                Text("Cancel")
//            }
//        }
//    )
//}


@Composable
fun BackupConfirmationDialog(
    isEnablingBackup: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(
                    id = if (isEnablingBackup) R.string.enable_backup_title else R.string.disable_backup_title
                )
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        id = if (isEnablingBackup) R.string.enable_backup_message else R.string.disable_backup_message
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(id = R.string.proceed_question))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}
