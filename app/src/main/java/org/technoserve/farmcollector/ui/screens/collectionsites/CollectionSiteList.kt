package org.technoserve.farmcollector.ui.screens.collectionsites

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Commodity
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.components.BackupConfirmationDialog
import org.technoserve.farmcollector.ui.components.CustomPaginationControls
import org.technoserve.farmcollector.ui.components.FarmListHeader
import org.technoserve.farmcollector.ui.components.RestoreDataAlert
import org.technoserve.farmcollector.ui.components.SiteCard
import org.technoserve.farmcollector.ui.components.SiteDeleteAllDialogPresenter
import org.technoserve.farmcollector.ui.components.SkeletonSiteCard
import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory
import org.technoserve.farmcollector.viewmodels.RestoreStatus
import org.technoserve.farmcollector.viewmodels.UndoDeleteSnackbar
import org.technoserve.farmcollector.utils.DeviceIdUtil
import org.technoserve.farmcollector.ui.composes.isValidPhoneNumber
import org.technoserve.farmcollector.utils.BackupPreferences
import org.technoserve.farmcollector.utils.isSystemInDarkTheme


/**
 * This is the main screen for the collection sites list.
 * It displays a list of collection sites, allows for search, select, and delete operations.
 * It also handles the restore data prompt and final message.
 *
 * @param navController the navigation controller for navigating between screens
 * @param farmViewModel the view model for interacting with the Farm database
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSiteList(navController: NavController) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel =
        viewModel(
            factory = FarmViewModelFactory(context.applicationContext as Application),
        )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val selectedSite = remember { mutableStateOf<CollectionSite?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val listItems by farmViewModel.readAllSites.observeAsState(listOf())
    var (searchQuery, setSearchQuery) = remember { mutableStateOf("") }
    fun onDelete() {
        val toDelete = mutableListOf<Long>()
        toDelete.addAll(selectedIds)
        farmViewModel.deleteListSite(toDelete)
        selectedIds.removeAll(selectedIds)
        showDeleteDialog.value = false
    }

    val isLoading = remember { mutableStateOf(true) }
    var deviceId by remember { mutableStateOf("") }
    val restoreStatus by farmViewModel.restoreStatus.observeAsState()
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var showRestorePrompt by remember { mutableStateOf(false) }
    var finalMessage by remember { mutableStateOf("") }
    var showFinalMessage by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val inputLabelColor = if (isDarkTheme) Color.White else Color.Black
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    val lazyPagingItems = farmViewModel.pager.collectAsLazyPagingItems()

    val pageSize = 3
    val pagedData = farmViewModel.pager.collectAsLazyPagingItems()
    var currentPage by remember { mutableIntStateOf(1) }

    var isSearchActive by remember { mutableStateOf(false) }

    val cwsListItems by farmViewModel.readAllSites.observeAsState(listOf())

    val filteredList = cwsListItems.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    var showRestoreAlert by remember { mutableStateOf(false) }
//    var showUndoSnackbar by remember { mutableStateOf(false) }
    val showUndoSnackbar = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    // ✅ Observe backup state
    val isBackupEnabled by BackupPreferences.isBackupEnabled(context).collectAsState(initial = false)

    // ✅ Observe last sync time
    val lastSyncTime by BackupPreferences.getLastBackupTime(context).collectAsState(initial = "Never")

    // ✅ Boolean state to control if Last Sync Time is shown
    var showLastSync by remember { mutableStateOf(true) } // Default is true, can be toggled

    // ✅ State for Confirmation Dialog
    var showDialog by remember { mutableStateOf(false) }
    var pendingBackupState by remember { mutableStateOf(isBackupEnabled) }



    LaunchedEffect(Unit) {
        deviceId = DeviceIdUtil.getDeviceId(context)
    }


    LaunchedEffect(Unit) {
        delay(500)
        isLoading.value = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FarmListHeader(
                title = stringResource(id = R.string.collection_site_list),
                onSearchQueryChanged = setSearchQuery,
                onBackClicked = { navController.navigate("home") },
                showSearch = true,
                showRestore = true,
                onRestoreClicked = {
                    showRestoreAlert = true
                },
                isBackupEnabled = isBackupEnabled, // ✅ Pass backup state
                showLastSync = showLastSync, // ✅ Boolean to toggle visibility
                lastSyncTime = lastSyncTime, // ✅ Pass last sync timestamp
                onBackupToggleClicked = { newState ->
                    pendingBackupState = newState // Store user's choice before confirmation
                    showDialog = true // Show confirmation dialog
                }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("addSite")
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(end = 0.dp, bottom = 48.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .align(BottomEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add a Site")
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Search field below the header
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        label = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }

                // Restore Alert Dialog
                // Show restore alert dialog
                RestoreDataAlert(
                    showDialog = showRestoreAlert,
                    onDismiss = { showRestoreAlert = false },
                    deviceId = deviceId,
                    farmViewModel = farmViewModel
                )

                // ✅ Show Confirmation Dialog when toggling backup
                if (showDialog) {
                    BackupConfirmationDialog(
                        isEnablingBackup = pendingBackupState,
                        onConfirm = {
                            coroutineScope.launch {
                                BackupPreferences.setBackupEnabled(context, pendingBackupState) // Save choice
                                if (pendingBackupState) {
                                    BackupPreferences.setLastBackupTime(context) // Update sync time
                                }
                            }
                            showDialog = false
                        },
                        onCancel = { showDialog = false }
                    )
                }
//
//                // Undo Delete Snackbar
//                UndoDeleteSnackbar(
//                    show = showUndoSnackbar,
//                    onDismiss = { showUndoSnackbar = false },
//                    onUndo = {
//                        // Implement undo logic here
//                        showUndoSnackbar = false
//                    }
//                )

                when {
                    pagedData.loadState.refresh is LoadState.Loading -> {
                        LazyColumn {
                            items(4) {
                                SkeletonSiteCard()
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    pagedData.loadState.refresh is LoadState.Error -> {
                        Text(
                            text = stringResource(id = R.string.error_loading_more_sites),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }

                    cwsListItems.isNotEmpty() -> {
                        Column(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isNotEmpty() && filteredList.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_results_found),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            } else {
                                LazyColumn {
                                    val pageSize = 4
                                    val startIndex = (currentPage - 1) * pageSize
                                    val endIndex = minOf(startIndex + pageSize, filteredList.size)

                                    items(endIndex - startIndex) { index ->
                                        val siteIndex = startIndex + index
                                        val site = filteredList[siteIndex]
                                        SiteCard(
                                            site = site,
                                            onCardClick = {
                                                navController.navigate("farmList/${site.siteId}")
                                            },
                                            totalFarms = farmViewModel.getTotalFarms(site.siteId)
                                                .observeAsState(0).value,
                                            farmsWithIncompleteData = farmViewModel.getFarmsWithIncompleteData(
                                                site.siteId
                                            )
                                                .observeAsState(0).value,
                                            onDeleteClick = {
                                                selectedIds.add(site.siteId)
                                                selectedSite.value = site
                                                showDeleteDialog.value = true
                                            },
                                            farmViewModel = farmViewModel,
                                            snackbarHostState= snackbarHostState
                                        )
                                        // Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    item {
                                        CustomPaginationControls(
                                            currentPage = currentPage,
                                            totalPages = (filteredList.size + pageSize - 1) / pageSize,
                                            onPageChange = { newPage ->
                                                currentPage = newPage
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp, 8.dp),
                                painter = painterResource(id = R.drawable.no_data2),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
    )



    when (restoreStatus) {
        is RestoreStatus.InProgress -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is RestoreStatus.Success -> {
            Column(
                modifier = Modifier
                    .padding(top = 72.dp)
                    .fillMaxSize()
            ) {
                val status = restoreStatus as RestoreStatus.Success
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.restoration_completed,
                        status.addedCount,
                        status.sitesCreated
                    ),
                    Toast.LENGTH_LONG
                ).show()
                showRestorePrompt = false
            }
        }

        is RestoreStatus.Error -> {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showRestorePrompt) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        if (showFinalMessage) {
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.no_data_found,
                                ),
                                Toast.LENGTH_LONG // Duration of the toast (LONG or SHORT)
                            ).show()
                        }

                        showFinalMessage = false
                        TextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = {
                                Text(
                                    stringResource(id = R.string.phone_number),
                                    color = inputLabelColor
                                )
                            },
                            supportingText = {
                                if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) Text(
                                    stringResource(R.string.error_invalid_phone_number, phone)
                                )
                            },
                            isError = phone.isNotEmpty() && !isValidPhoneNumber(phone),
                            colors = TextFieldDefaults.colors(
                                errorLeadingIconColor = Color.Red,
                                cursorColor = inputTextColor,
                                errorCursorColor = Color.Red,
                                focusedIndicatorColor = inputBorder,
                                unfocusedIndicatorColor = inputBorder,
                                errorIndicatorColor = Color.Red
                            )

                        )
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = {
                                Text(
                                    stringResource(id = R.string.email),
                                    color = inputLabelColor
                                )
                            },
                            supportingText = {
                                if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                                        email
                                    ).matches()
                                )
                                    Text(stringResource(R.string.error_invalid_email_address))
                            },
                            isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                                email
                            ).matches(),
                            colors = TextFieldDefaults.colors(
                                errorLeadingIconColor = Color.Red,
                                cursorColor = inputTextColor,
                                errorCursorColor = Color.Red,
                                focusedIndicatorColor = inputBorder,
                                unfocusedIndicatorColor = inputBorder,
                                errorIndicatorColor = Color.Red
                            ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    showRestorePrompt = false
                                    showFinalMessage = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(id = R.string.cancel))
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (phone.isNotBlank() || email.isNotBlank()) {
                                        showRestorePrompt =
                                            false
                                        farmViewModel.restoreData(
                                            deviceId = deviceId,
                                            phoneNumber = phone,
                                            email = email,
                                            farmViewModel = farmViewModel
                                        ) { success ->
                                            finalMessage = if (success) {
                                                context.getString(R.string.data_restored_successfully)
                                            } else {
                                                context.getString(R.string.no_data_found)
                                            }
                                            showFinalMessage = true
                                        }
                                    }
                                },
                                enabled = email.isNotBlank() || phone.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(context.getString(R.string.restore_data))
                            }
                        }
                    }
                } else {
                    if (showFinalMessage) {
                        // Show the toast
                        Toast.makeText(
                            context,
                            finalMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        null -> {
            if (isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {

                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(top = 48.dp)
                ) {

                }
            }
        }
    }

    if (showDeleteDialog.value) {
       // SiteDeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })

        selectedSite.value?.let {
            SiteDeleteAllDialogPresenter(
                showDeleteDialog = showDeleteDialog,
                site = it,
                farmViewModel = farmViewModel,
                snackbarHostState = snackbarHostState,
                onProceedFn = {
                    farmViewModel.deleteListSite(selectedIds)
                    // Show the undo snackbar
                },
                showUndoSnackbar = showUndoSnackbar

            )
        }

    }
}






