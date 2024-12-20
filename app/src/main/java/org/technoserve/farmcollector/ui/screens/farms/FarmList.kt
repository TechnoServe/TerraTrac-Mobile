package org.technoserve.farmcollector.ui.screens.farms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.database.models.ParcelableFarmData
import org.technoserve.farmcollector.database.models.ParcelablePair

import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory
import org.technoserve.farmcollector.viewmodels.RestoreStatus
import org.technoserve.farmcollector.utils.DeviceIdUtil
import org.technoserve.farmcollector.ui.components.CustomPaginationControls
import org.technoserve.farmcollector.ui.components.CustomizedConfirmationDialog
import org.technoserve.farmcollector.ui.components.DeleteAllDialogPresenter
import org.technoserve.farmcollector.ui.components.FarmCard
import org.technoserve.farmcollector.ui.components.FarmListHeaderPlots
import org.technoserve.farmcollector.ui.components.FormatSelectionDialog
import org.technoserve.farmcollector.ui.components.ImportFileDialog
import org.technoserve.farmcollector.ui.components.RestoreDataAlert
import org.technoserve.farmcollector.ui.composes.isValidPhoneNumber

import org.technoserve.farmcollector.utils.createFile
import org.technoserve.farmcollector.utils.createFileForSharing
import org.technoserve.farmcollector.utils.isSystemInDarkTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


var siteID = 0L
/**
 *
 * The Action enum represents the available actions for farm management.
 * Export represents exporting farms in a specific format (e.g., CSV, JSON).
 * Share represents sharing farms with other users or devices.
 */
enum class Action {
    Export,
    Share,
}

/**
 *
 * The FarmList screen displays a list of farms. Users can add, edit, delete, and share farms.
 * The screen includes a search bar, a tabbed view, and a floating action button for adding a new farm.
 * The farms are displayed in a paginated manner, with a custom pagination control implemented using the CustomPaginationControls component.
 * The screen also includes a floating action button for exporting farms in a specific format (e.g., CSV, JSON).
 * When the user selects a farm, they can view its details, edit it, or delete it.
 * The screen supports dark mode by using the MaterialTheme.isSystemInDarkTheme() function.
 *
 */

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun FarmList(
    navController: NavController,
    siteId: Long,
) {
    siteID = siteId
    val context = LocalContext.current
    val farmViewModel: FarmViewModel =
        viewModel(
            factory = FarmViewModelFactory(context.applicationContext as Application),
        )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val selectedFarm = remember { mutableStateOf<Farm?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val listItems by farmViewModel.readAllData(siteId).observeAsState(listOf())
    val cwsListItems by farmViewModel.readAllSites.observeAsState(listOf())
    var showFormatDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<Action?>(null) }
    val activity = context as Activity
    var exportFormat by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }

    val tabs =
        listOf(
            stringResource(id = R.string.all),
            stringResource(id = R.string.needs_update)
        )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    // State to manage the loading status
    val isLoading = remember { mutableStateOf(true) }
    var deviceId by remember { mutableStateOf("") }
    val restoreStatus by farmViewModel.restoreStatus.observeAsState()
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showRestorePrompt by remember { mutableStateOf(false) }
    var finalMessage by remember { mutableStateOf("") }
    var showFinalMessage by remember { mutableStateOf(false) }

    var showRestoreAlert by remember { mutableStateOf(false) }


    val isDarkTheme = isSystemInDarkTheme()
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray


    LaunchedEffect(Unit) {
        deviceId = DeviceIdUtil.getDeviceId(context)
    }

    // Simulate a network request or data loading
    LaunchedEffect(Unit) {
        delay(2000)
        isLoading.value = false
    }

    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (createFile(
                           context, uri,listItems,
                                exportFormat,
                                siteID ,
                                cwsListItems
                    )){
                        Toast.makeText(context, R.string.success_export_msg, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    fun initiateFileCreation() {
        val mimeType = if (exportFormat == "CSV") "text/csv" else "application/geo+json"
        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                val getSiteById = cwsListItems.find { it.siteId == siteID }
                val siteName = getSiteById?.name ?: "SiteName"
                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filename =
                    if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.geojson"
                putExtra(Intent.EXTRA_TITLE, filename)
            }
        createDocumentLauncher.launch(intent)
    }

    // Function to share the file
    fun shareFile(file: File) {
        val fileURI: Uri =
            context.let {
                FileProvider.getUriForFile(
                    it,
                    context.applicationContext.packageName.toString() + ".provider",
                    file,
                )
            }

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = if (exportFormat == "CSV") "text/csv" else "application/geo+json"
                putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
                putExtra(Intent.EXTRA_TEXT, "Sharing the farm data file.")
                putExtra(Intent.EXTRA_STREAM, fileURI)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        val chooserIntent = Intent.createChooser(shareIntent, "Share file")
        activity.startActivity(chooserIntent)
    }

    fun exportFile() {
        showConfirmationDialog = true
    }

    // Function to handle the share action
    fun shareFileAction() {
        showConfirmationDialog = true
    }

    if (showFormatDialog) {
        FormatSelectionDialog(
            onDismiss = { showFormatDialog = false },
            onFormatSelected = { format ->
                exportFormat = format
                showFormatDialog = false
                when (action) {
                    Action.Export -> exportFile()
                    Action.Share -> shareFileAction()
                    else -> {}
                }
            },
        )
    }
    if (showConfirmationDialog) {
        CustomizedConfirmationDialog(
            listItems,
            action = action!!, // Ensure action is not null
            onConfirm = {
                when (action) {
                    Action.Export -> initiateFileCreation()
                    Action.Share -> {
                        // file = createFileForSharing()
                        val file = createFileForSharing(
                            context,
                            listItems,
                        exportFormat,
                        siteID,
                        cwsListItems
                        )
                        if (file != null) {
                            shareFile(file)
                        }
                    }

                    else -> {}
                }
            },
            onDismiss = { showConfirmationDialog = false },
        )
    }
    if (showImportDialog) {
        ImportFileDialog(
            siteId,
            onDismiss = { showImportDialog = false },
            navController = navController
        )
    }

    fun onDelete() {
        selectedFarm.value?.let { farm ->
            val toDelete =
                mutableListOf<Long>().apply {
                    addAll(selectedIds)
                    add(farm.id)
                }
            farmViewModel.deleteList(toDelete)
            selectedIds.removeAll(selectedIds)
            farmViewModel.deleteFarmById(farm)
            selectedFarm.value = null
            selectedIds.removeAll(selectedIds)
            showDeleteDialog.value = false
        }
    }

    // Function to show data or no data message
    @Composable
    fun showDataContent() {
        val hasData = listItems.isNotEmpty() // Check if there's data available
        val pageSize = 5  // Set the page size (number of items per page)
        var currentPage by remember { mutableStateOf(1) } // Track the current page
        val currentCategoryIndex = pagerState.currentPage

        // Filter the list into two categories: farms that need updates and farms that do not need updates
        val filteredListItemsNeedUpdate = listItems.filter { it.needsUpdate }.filter {
            it.farmerName.contains(searchQuery, ignoreCase = true)
        }
        val filteredListItemsNoUpdate = listItems.filter { !it.needsUpdate }.filter {
            it.farmerName.contains(searchQuery, ignoreCase = true)
        }

        // Calculate the number of pages for each category
        val totalPagesNeedUpdate = (filteredListItemsNeedUpdate.size + pageSize - 1) / pageSize
        val totalPagesNoUpdate = (filteredListItemsNoUpdate.size + pageSize - 1) / pageSize

        if (hasData) {
            Column {
                // Only show the TabRow and HorizontalPager if there is data
                TabRow(
                    selectedTabIndex = currentCategoryIndex,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[currentCategoryIndex])
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.onPrimary // Color for the indicator
                        )
                    },
                    divider = { HorizontalDivider() }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = currentCategoryIndex == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    // Determine which category to display based on the current tab index
                    val filteredListItems = when (page) {
                        1 -> filteredListItemsNeedUpdate // Farms that need update
                        else -> filteredListItemsNoUpdate // Farms that do not need update
                    }
                    if (filteredListItems.isNotEmpty() || searchQuery.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 90.dp)
                        ) {
                            val pageSize = 5
                            val startIndex = maxOf(0, (currentPage - 1) * pageSize) // Ensure startIndex is non-negative
                            val endIndex = minOf(filteredListItems.size, startIndex + pageSize) // Ensure endIndex is within bounds

                            // Safeguard: Ensure indices are within bounds
                            if (filteredListItems.isNotEmpty()) {
                                // Show the items for the current page
                                items(endIndex - startIndex) { index ->
                                    val item = filteredListItems[startIndex + index]
                                    FarmCard(
                                        farm = item,
                                        onCardClick = {
                                            navController.currentBackStackEntry?.arguments?.apply {
                                                putParcelableArrayList(
                                                    "coordinates",
                                                    item.coordinates?.map {
                                                        it.first?.let { it1 ->
                                                            it.second?.let { it2 ->
                                                                ParcelablePair(it1, it2)
                                                            }
                                                        }
                                                    }?.let { ArrayList(it) }
                                                )
                                                putParcelable(
                                                    "farmData",
                                                    ParcelableFarmData(item, "view")
                                                )
                                            }
                                            navController.navigate(route = "setPolygon")
                                        },
                                        onDeleteClick = {
                                            selectedIds.add(item.id)
                                            selectedFarm.value = item
                                            showDeleteDialog.value = true
                                        }
                                    )
                                   // Spacer(modifier = Modifier.height(16.dp))
                                }

                                item {
                                    CustomPaginationControls(
                                        currentPage = currentPage,
                                        totalPages = when (currentCategoryIndex) {
                                            0 -> totalPagesNoUpdate // Pages for farms that do not need updates
                                            1 -> totalPagesNeedUpdate // Pages for farms needing updates
                                            else -> 0
                                        },
                                        onPageChange = { newPage ->
                                            currentPage = newPage
                                        }
                                    )
                                }
                            }

                            else {
                                item {
                                    Text(
                                        text = stringResource(R.string.no_results_found),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp, 8.dp),
                            painter = painterResource(id = R.drawable.no_data2),
                            contentDescription = null
                        )
                    }
                }
            }
        } else {
            // Display a message or image indicating no data available
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 8.dp),
                    painter = painterResource(id = R.drawable.no_data2),
                    contentDescription = null
                )
            }
        }
    }
    Scaffold(
        topBar = {
            FarmListHeaderPlots(
                title = stringResource(id = R.string.farm_list),
                onBackClicked = { navController.navigate("siteList") },
                onExportClicked = {
                    action = Action.Export
                    showFormatDialog = true
                },
                onShareClicked = {
                    action = Action.Share
                    showFormatDialog = true
                },
                onSearchQueryChanged = setSearchQuery,
                onImportClicked = { showImportDialog = true },
                showExport = listItems.isNotEmpty(),
                showShare = listItems.isNotEmpty(),
                showSearch = listItems.isNotEmpty(),
                onRestoreClicked = {
//                    farmViewModel.restoreData(
//                        deviceId = deviceId,
//                        phoneNumber = "",
//                        email = "",
//                        farmViewModel = farmViewModel
//                    ) { success ->
//                        if (success) {
//                            finalMessage = context.getString(R.string.data_restored_successfully)
//                            showFinalMessage = true
//                        } else {
//                            showFinalMessage = true
//                            showRestorePrompt = true
//                        }
//                    }
                    showRestoreAlert = true
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
                        val sharedPref =
                            context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
                        sharedPref.edit().remove("plot_size").remove("selectedUnit").apply()
                        navController.navigate("addFarm/${siteId}")
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(end = 0.dp, bottom = 48.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .align(BottomEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Farm in a Site")
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {

                // Restore Alert Dialog
                // Show restore alert dialog
                RestoreDataAlert(
                    showDialog = showRestoreAlert,
                    onDismiss = { showRestoreAlert = false },
                    deviceId = deviceId,
                    farmViewModel = farmViewModel
                )

                showDataContent()
            }
        }
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
        DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
    }
}

fun updateFarm(
    farmViewModel: FarmViewModel,
    item: Farm,
) {
    farmViewModel.updateFarm(item)
}
