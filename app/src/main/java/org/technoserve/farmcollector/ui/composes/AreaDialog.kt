package org.technoserve.farmcollector.ui.composes

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.technoserve.farmcollector.R

@SuppressLint("DefaultLocale")
@Composable
fun AreaDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    calculatedArea: Double,
    enteredArea: Double
) {
    if (showDialog) {
        val options = listOf(
            "Calculated Area (${String.format("%.2f", calculatedArea)} hectares)",
            "Entered Size (${String.format("%.2f", enteredArea)} hectares)"
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = stringResource(id = R.string.choose_area))

            },
            text = {
                Text(text =stringResource(id = R.string.please_choose_area) )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(options[0]) }) {
                    Text(text = options[0])
                }
                TextButton(onClick = { onConfirm(options[1]) }) {
                    Text(text = options[1])
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

