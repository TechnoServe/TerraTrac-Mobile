package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.ui.composes.UpdateCollectionDialog
import org.technoserve.farmcollector.viewmodels.FarmViewModel
/**
 * A site card in the FarmList screen. Displays the site name, number of farms, and a button to edit or delete the site.
 *
 * @param site The site to display
 * @param onCardClick A callback function to be called when the card is clicked
 * @param totalFarms The total number of farms in the site
 * @param farmsWithIncompleteData The number of farms with incomplete data
 * @param onDeleteClick
 * @param farmViewModel
 */
@Composable
fun SiteCard(
    site: CollectionSite,
    onCardClick: () -> Unit,
    totalFarms: Int,
    farmsWithIncompleteData: Int,
    onDeleteClick: () -> Unit,
    farmViewModel: FarmViewModel,
    snackbarHostState: SnackbarHostState
) {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        UpdateCollectionDialog(
            site = site,
            showDialog = showDialog,
            farmViewModel = farmViewModel,
        )
    }
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.onBackground

    val showDeleteDialog = remember { mutableStateOf(false) }
    val showUndoSnackbar = remember { mutableStateOf(false) }


    // Handle the delete dialog and undo snackbar
    SiteDeleteAllDialogPresenter(
        showDeleteDialog = showDeleteDialog,
        site = site,
        farmViewModel = farmViewModel,
        snackbarHostState = snackbarHostState,
        onProceedFn = {
            // Proceed with deleting the site
            farmViewModel.deleteListSite(listOf(site.siteId))
        },
        showUndoSnackbar = showUndoSnackbar
    )


    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ElevatedCard(
            elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
            modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                onCardClick()
            },
        ) {
            Column(
                modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier =
                        Modifier
                            .weight(1.1f)
                            .padding(bottom = 4.dp),
                    ) {
                        Text(
                            text = site.name,
                            style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            ),
                            modifier =
                            Modifier
                                .padding(bottom = 1.dp),
                        )
                        Text(
                            text = "${stringResource(id = R.string.agent_name)}: ${site.agentName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            modifier =
                            Modifier
                                .padding(bottom = 1.dp),
                        )
                        if (site.phoneNumber.isNotEmpty()) {
                            Text(
                                text = "${stringResource(id = R.string.phone_number)}: ${site.phoneNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            )
                        }

                        Text(
                            text = stringResource(
                                id = R.string.total_farms,
                                totalFarms
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        Text(
                            text = stringResource(
                                id = R.string.total_farms_with_incomplete_data,
                                farmsWithIncompleteData
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Blue
                            ),
                        )
                    }
                    IconButton(
                        onClick = {
                            showDialog.value = true
                        },
                        modifier =
                        Modifier
                            .size(24.dp)
                            .padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update",
                            tint = iconColor,
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    IconButton(
                        onClick = {
                            onDeleteClick()
                        },
                        modifier =
                        Modifier
                            .size(24.dp)
                            .padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                        )
                    }
                }
            }
        }
    }
}