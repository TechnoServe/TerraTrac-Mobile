package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.technoserve.farmcollector.R

/**
 *  This function is used to display the header for the farm list with search, export, share, and import buttons
 *  @param title: The title of the header
 *  @param onBackClicked: A function to be called when the back button is clicked
 *  @param onExportClicked: A function to be called when the export button is clicked
 *  @param onShareClicked: A function to be called when the share button is clicked
 *  @param onImportClicked: A function to be called when the import button is clicked
 * @param onSearchQueryChanged : A function to be called when the search query is changed
 * @param showExport: A function to be called when the export button is clicked and the export button is clicked
 * @param showShar: A function to be called when the export button is clicked
 * @param showSearch: A function to be called when the export button is clicked
 * @param onRestoreClicked: A function to be called when the restore button is clicked and the restore button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListHeaderPlots(
    title: String,
    onBackClicked: () -> Unit,
    onExportClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onImportClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    showExport: Boolean,
    showShare: Boolean,
    showSearch: Boolean,
    onRestoreClicked: () -> Unit,
    isBackupEnabled: Boolean,
    showLastSync: Boolean,
    lastSyncTime: String,
    onBackupToggleClicked: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isImportDisabled by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) } // ✅ Controls dropdown visibility
    var isInfoExpanded by remember { mutableStateOf(false) } // ✅ Controls info dropdown visibility

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Adjust sizes based on screen width
    val iconSize = if (screenWidth < 450.dp) 24.dp else 24.dp
    val switchScale = if (screenWidth < 450.dp) 0.6f else 0.8f
    val horizontalPadding = if (screenWidth < 450.dp) 8.dp else 12.dp
    val titleFontSize = if (screenWidth < 450.dp) 16.sp else 18.sp
    val backupTextStyle = if (screenWidth < 450.dp) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.bodyMedium
    }

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = horizontalPadding),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = titleFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .statusBarsPadding(),
//                        .padding(WindowInsets.safeDrawing.asPaddingValues()) // Ensures title is within the safe area
                )

                // ✅ Info Icon for Last Sync
                if (showLastSync) {
                    if (screenWidth >= 450.dp) {
                        // Show regular column on larger screens
                        Column(
                            modifier = Modifier.padding(end = 4.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.last_synced),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = lastSyncTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { isInfoExpanded = !isInfoExpanded },
                                modifier = Modifier.size(iconSize)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(id = R.string.last_synced),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = isInfoExpanded,
                                onDismissRequest = { isInfoExpanded = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.last_synced),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lastSyncTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                if (isSearchVisible) {
                    searchQuery = ""
                    onSearchQueryChanged("")
                    isSearchVisible = false
                } else {
                    onBackClicked()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            // ✅ Always Visible Icons: Backup, Restore, and Search
            if (showLastSync) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp) // ✅ Reduced spacing
                ) {
                    Text(
                        text = stringResource(id = R.string.backup_now),
                        style = backupTextStyle,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Switch(
                        checked = isBackupEnabled,
                        onCheckedChange = onBackupToggleClicked,
                        thumbContent = {
                            Icon(
                                imageVector = if (isBackupEnabled) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = stringResource(id = R.string.backup_now),
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.scale(switchScale),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.error,
                            uncheckedTrackColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )
                }
                if (screenWidth >= 450.dp) {
                    if (showExport) {
                        IconButton(onClick = onExportClicked, modifier = Modifier.size(36.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.save),
                                contentDescription = "Export",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                    if (showShare) {
                        IconButton(onClick = onShareClicked, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!isImportDisabled) {
                                onImportClicked()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icons8_import_file_48),
                            contentDescription = "Import",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                IconButton(onClick = onRestoreClicked, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.restore),
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }



                if (showSearch) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search),
                            modifier = Modifier.size(iconSize),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (screenWidth <= 450.dp) {

                    // ✅ Move Export, Share, and Import to a dropdown on small screens
                    Box {
                        IconButton(
                            onClick = { isMenuExpanded = !isMenuExpanded },
                            modifier = Modifier.size(iconSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface) // ✅ Dropdown aligned to the right
                        ) {
                            if (showExport) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.export)) },
                                    onClick = {
                                        onExportClicked()
                                        isMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.save),
                                            contentDescription = stringResource(id = R.string.export),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                )
                            }
                            if (showShare) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.share)) },
                                    onClick = {
                                        onShareClicked()
                                        isMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = stringResource(id = R.string.share),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.import_)) },
                                onClick = {
                                    onImportClicked()
                                    isMenuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icons8_import_file_48),
                                        contentDescription = stringResource(id = R.string.import_),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    )

    if (isSearchVisible) {
        Box(
            modifier = Modifier
                .padding(top = 54.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(0.dp)),
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            onSearchQueryChanged("")
                            isSearchVisible = false
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery != "") {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearchQueryChanged("")
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        errorCursorColor = Color.Red,
                        // ✅ Ensure Border Always Stays Visible
                        focusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant, // Border when focused
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant, // Border when unfocused
                        errorIndicatorColor = Color.Red, // Border when error state
                        // ✅ Add Background Colors
                        focusedContainerColor = MaterialTheme.colorScheme.background,  // Background when focused
                        unfocusedContainerColor = MaterialTheme.colorScheme.background, // Background when not focused
                        errorContainerColor = Color.Red// Light red for error state
                    ),
                    shape = RoundedCornerShape(0.dp)
                )

            }
        }
    }
}

