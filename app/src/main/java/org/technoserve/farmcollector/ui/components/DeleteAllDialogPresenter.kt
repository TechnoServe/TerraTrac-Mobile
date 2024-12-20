package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
/**
 *  This function is used to display a warning dialog when the user tries to delete a farm
 *  It shows a warning icon, a message, and two buttons: "Yes" and "No"
 *  When the user clicks "Yes", the onProceedFn function is called to perform the deletion action
 *  When the user clicks "No", the delete dialog is dismissed by setting showDeleteDialog to false
 *  The containerColor and textColors are set to match the Material3 theme's background and error colors respectively
 *  The modifier is set to adjust the padding and size of the dialog to better fit the layout and adhere to the design guidelines of the Material3 component library
 *  The text and button texts are localized using the stringResource function to support different languages
 *  Note: This is a simplified version of the DeleteAllDialogPresenter function. In a real-world application, you may want to add additional logic and features to handle
 * deleting all resources associated with this dialog
 */
@Composable
fun DeleteAllDialogPresenter(
    showDeleteDialog: MutableState<Boolean>,
    onProceedFn: () -> Unit,
) {
    if (showDeleteDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning, // Use a built-in warning icon
                        contentDescription = stringResource(id = R.string.warning),
                        tint = MaterialTheme.colorScheme.error, // Use error color for the icon
                        modifier = Modifier.size(24.dp) // Adjust the size of the icon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.delete_this_farm))
                }
            },
            text = {
                Column {
                    Text(stringResource(id = R.string.are_you_sure))
                    Text(stringResource(id = R.string.farm_will_be_deleted))
                }
            },
            confirmButton = {
                TextButton(onClick = { onProceedFn() }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            },
            containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
            tonalElevation = 6.dp // Adds a subtle shadow for better UX
        )
    }
}