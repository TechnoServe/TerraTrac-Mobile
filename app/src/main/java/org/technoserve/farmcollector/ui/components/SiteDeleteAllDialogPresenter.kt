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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.viewmodels.FarmViewModel

/**
 * This component presents a confirmation dialog for deleting a site.
 *
 * @param showDeleteDialog: MutableState indicating whether the dialog should be shown.
 * @param site: The site to be deleted.
 * @param farmViewModel: The FarmViewModel for interacting with the database.
 * @param snackbarHostState: The SnackbarHostState for displaying snackbars.
 * @param onProceedFn Called when the application is about to proceed to the next page.
 */
@Composable
fun SiteDeleteAllDialogPresenter(
    showDeleteDialog: MutableState<Boolean>,
    site: CollectionSite,
    farmViewModel: FarmViewModel,
    snackbarHostState: SnackbarHostState,
    onProceedFn: () -> Unit,
    showUndoSnackbar: MutableState<Boolean> ,
) {
    val scope = rememberCoroutineScope()
    var deletedSite by remember { mutableStateOf<CollectionSite?>(null) }

    if (showDeleteDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(id = R.string.warning),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.delete_this_site))
                }
            },
            text = {
                Column {
                    Text(stringResource(id = R.string.are_you_sure))
                    Text(stringResource(id = R.string.site_will_be_deleted))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Store the site before deletion
                        deletedSite = site

                        // Proceed with the deletion action
                        onProceedFn()

                        // Show the Undo Delete Snackbar
                        showUndoSnackbar.value = true

                        showDeleteDialog.value = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
    // Handle Undo Snackbar
    if (showUndoSnackbar.value && deletedSite != null) {
        LaunchedEffect(deletedSite) {
            snackbarHostState.showSnackbar(
                message = "Item deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            ).apply {
                if (this == SnackbarResult.ActionPerformed) {
                    // If user clicked "UNDO", restore the site
                    farmViewModel.restoreDeletedSites()
                    showUndoSnackbar.value = false
                }
            }
        }
    }
}