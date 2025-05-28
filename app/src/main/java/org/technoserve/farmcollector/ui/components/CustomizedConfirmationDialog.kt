package org.technoserve.farmcollector.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.screens.farms.Action

/**
 *  This function is used to display a customized confirmation dialog for different actions like exporting, sharing, etc.
 *  @param listItems: A list of farms to be validated
 *  @param action: The action that needs to be confirmed (e.g., exporting, sharing)
 *  @param onConfirm: A function to be called when the user confirms the action
 *  @param onDismiss: A function to be called when the user dismisses the dialog
 */
@Composable
fun CustomizedConfirmationDialog(
    listItems: List<Farm>,
    action: Action,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    fun validateFarms(farms: List<Farm>): Pair<Int, List<Farm>> {
        val incompleteFarms =
            farms.filter { farm ->
                farm.farmerName.isEmpty() ||
                        farm.district.isEmpty() ||
                        farm.village.isEmpty() ||
                        farm.latitude == "0.0" ||
                        farm.longitude == "0.0" ||
                        farm.size == 0.0f ||
                        farm.remoteId.toString().isEmpty()
            }
        return Pair(farms.size, incompleteFarms)
    }
    val (totalFarms, incompleteFarms) = validateFarms(listItems)
    val message =
        when (action) {
            Action.Export -> stringResource(
                R.string.confirm_export,
                totalFarms,
                incompleteFarms.size
            )

            Action.Share -> stringResource(R.string.confirm_share, totalFarms, incompleteFarms.size)
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.confirm)) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.no))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp
    )
}