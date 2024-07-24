package org.technoserve.farmcollector.ui.composes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R

/**
 * This popup dialog allows you to confirm and deny moving forward.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    showDialog: MutableState<Boolean>,
    onProceedFn: () -> Unit,
) {
    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = title) },
            text = {
                Column {
                    Text(text = message)
                }
            },
            confirmButton = {
                TextButton(onClick = { onProceedFn() }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            },
        )
    }
}
