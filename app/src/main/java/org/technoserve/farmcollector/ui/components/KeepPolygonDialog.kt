package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.viewmodels.MapViewModel

/**
 *  This function is used to display the dialog to allow the user to either keep the existing polygon or capture a new polygon
 *  It also contains two buttons for choosing the action (keep existing or capture new)
 *
 *  @param onDismiss: This function is called when the dialog is dismissed
 *  @param onKeepExisting: This function is called when the user chooses to keep the existing polygon
 *  @param onCaptureNew: This function is called when the user chooses to capture a new polygon
 */

@Composable
fun KeepPolygonDialog(
    onDismiss: () -> Unit,
    onKeepExisting: () -> Unit,
    onCaptureNew: () -> Unit,
) {

    val mapViewModel: MapViewModel = viewModel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.update_polygon),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.keep_existing_polygon_or_capture_new),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        confirmButton = {
            Button(
                onClick = onKeepExisting,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(
                    text = stringResource(id = R.string.keep_existing),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    mapViewModel.clearCoordinates()
                    onCaptureNew()
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(
                    text = stringResource(id = R.string.capture_new),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp
    )
}
