package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import org.technoserve.farmcollector.utils.convertSize
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.regex.Pattern
import org.technoserve.farmcollector.database.RestoreStatus
import org.technoserve.farmcollector.database.sync.DeviceIdUtil
import org.technoserve.farmcollector.ui.composes.isValidPhoneNumber
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.draw.clip



var siteID = 0L

enum class Action {
    Export,
    Share,
}

data class ParcelablePair(val first: Double, val second: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(first)
        parcel.writeDouble(second)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelablePair> {
        override fun createFromParcel(parcel: Parcel): ParcelablePair {
            return ParcelablePair(parcel)
        }

        override fun newArray(size: Int): Array<ParcelablePair?> {
            return arrayOfNulls(size)
        }
    }
}

data class ParcelableFarmData(val farm: Farm, val view: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Farm::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(farm, flags)
        parcel.writeString(view)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ParcelableFarmData> {
        override fun createFromParcel(parcel: Parcel): ParcelableFarmData {
            return ParcelableFarmData(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableFarmData?> {
            return arrayOfNulls(size)
        }
    }
}


@Composable
fun isSystemInDarkTheme(): Boolean {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("theme_mode", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("dark_mode", false)
}

@Composable
fun FormatSelectionDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit,
) {
    var selectedFormat by remember { mutableStateOf("CSV") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.select_file_format)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFormat == "CSV",
                        onClick = { selectedFormat = "CSV" },
                    )
                    Text(stringResource(R.string.csv))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFormat == "GeoJSON",
                        onClick = { selectedFormat = "GeoJSON" },
                    )
                    Text(stringResource(R.string.geojson))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFormatSelected(selectedFormat)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
        tonalElevation = 6.dp // Adds a subtle shadow for better UX
    )
}

@Composable
fun ConfirmationDialog(
    listItems: List<Farm>,
    action: Action,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    fun validateFarms(farms: List<Farm>): Pair<Int, List<Farm>> {
        val incompleteFarms =
            farms.filter { farm ->
                farm.farmerName.isEmpty() ||
                        farm.district.isEmpty()||
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
            Action.Export -> stringResource(R.string.confirm_export, totalFarms, incompleteFarms.size)
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
        containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
        tonalElevation = 6.dp // Adds a subtle shadow for better UX
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    // Create a mutable state for the selected farm
    val selectedFarm = remember { mutableStateOf<Farm?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val listItems by farmViewModel.readAllData(siteId).observeAsState(listOf())
    val cwsListItems by farmViewModel.readAllSites.observeAsState(listOf())
    // var showExportDialog by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<Action?>(null) }
    val activity = context as Activity
    var exportFormat by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs =
        listOf(
            stringResource(id = R.string.all),
            stringResource(id = R.string.needs_update),
//            stringResource(id = R.string.no_update_needed),
        )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()


    // State to manage the loading status
    val isLoading = remember { mutableStateOf(true) }
    var deviceId by remember { mutableStateOf("") }
    // State variable to observe restore status
    val restoreStatus by farmViewModel.restoreStatus.observeAsState()

    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showRestorePrompt by remember { mutableStateOf(false) }
    var finalMessage by remember { mutableStateOf("") }
    var showFinalMessage by remember { mutableStateOf(false) }


    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    LaunchedEffect(Unit) {
        deviceId = DeviceIdUtil.getDeviceId(context)
    }

    // Simulate a network request or data loading
    LaunchedEffect(Unit) {
        // Simulate a delay for loading
        delay(2000) // Adjust the delay as needed
        // After loading data, set isLoading to false
        isLoading.value = false

    }

    fun createFileForSharing(): File? {
        // Get the current date and time
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val getSiteById = cwsListItems.find { it.siteId == siteID }
        val siteName = getSiteById?.name ?: "SiteName"
        val filename =
            if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.geojson"
        val mimeType = if (exportFormat == "CSV") "text/csv" else "application/geo+json"
        // Get the Downloads directory
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)

        try {
            file.bufferedWriter().use { writer ->
                if (exportFormat == "CSV") {
                    writer.write(
                        "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n",
                    )
                    listItems.forEach { farm ->
                        val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                        val matches = regex.findAll(farm.coordinates.toString())
                        val reversedCoordinates =
                            matches
                                .map { match ->
                                    val (lat, lon) = match.destructured
                                    "[$lon, $lat]"
                                }.toList()
                                .let { coordinates ->
                                    if (coordinates.isNotEmpty()) {
                                        // Always include brackets, even for a single point
                                        coordinates.joinToString(", ", prefix = "[", postfix = "]")
                                    } else {
                                        val lon = farm.longitude ?: "0.0"
                                        val lat = farm.latitude ?: "0.0"
                                        "[$lon, $lat]"
                                    }
                                }

                        val line =
                            "${farm.remoteId},${farm.farmerName},${farm.memberId},${getSiteById?.name},${getSiteById?.agentName},${farm.village},${farm.district},${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",${
                                Date(
                                    farm.createdAt,
                                )
                            },${Date(farm.updatedAt)}\n"
                        writer.write(line)
                    }
                } else {
                    val geoJson =
                        buildString {
                            append("{\"type\": \"FeatureCollection\", \"features\": [")
                            listItems.forEachIndexed { index, farm ->
                                val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                                val matches = regex.findAll(farm.coordinates.toString())
                                val geoJsonCoordinates =
                                    matches
                                        .map { match ->
                                            val (lat, lon) = match.destructured
                                            "[$lon, $lat]"
                                        }.joinToString(", ", prefix = "[", postfix = "]")
                                val latitude =
                                    farm.latitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0
                                val longitude =
                                    farm.longitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0

                                val feature =
                                    """
                                    {
                                        "type": "Feature",
                                        "properties": {
                                            "remote_id": "${farm.remoteId ?: ""}",
                                            "farmer_name": "${farm.farmerName ?: ""}",
                                            "member_id": "${farm.memberId ?: ""}",
                                            "collection_site": "${getSiteById?.name ?: ""}",
                                            "agent_name": "${getSiteById?.agentName ?: ""}",
                                            "farm_village": "${farm.village ?: ""}",
                                            "farm_district": "${farm.district ?: ""}",
                                             "farm_size": ${farm.size ?: 0.0},
                                            "latitude": $latitude,
                                            "longitude": $longitude,
                                            "created_at": "${farm.createdAt?.let { Date(it) } ?: "null"}",
                                            "updated_at": "${farm.updatedAt?.let { Date(it) } ?: "null"}"
                                            
                                        },
                                        "geometry": {
                                            "type": "${if ((farm.coordinates?.size ?: 0) > 1) "Polygon" else "Point"}",
                                            "coordinates": ${if ((farm.coordinates?.size ?: 0) > 1) "[$geoJsonCoordinates]" else "[$latitude,$longitude]"}
                                        }
                                    }
                                    """.trimIndent()
                                append(feature)
                                if (index < listItems.size - 1) append(",")
                            }
                            append("]}")
                        }
                    writer.write(geoJson)
                }
            }
            return file
        } catch (e: IOException) {
            Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
            return null
        }
    }

    fun createFile(
        context: Context,
        uri: Uri,
    ): Boolean {
        // Get the current date and time
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val getSiteById = cwsListItems.find { it.siteId == siteID }
        val siteName = getSiteById?.name ?: "SiteName"
        val filename =
            if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.geojson"

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    if (exportFormat == "CSV") {
                        writer.write(
                            "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n",
                        )
                        listItems.forEach { farm ->
                            val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                            val matches = regex.findAll(farm.coordinates.toString())
                            val reversedCoordinates =
                                matches
                                    .map { match ->
                                        val (lat, lon) = match.destructured
                                        "[$lon, $lat]"
                                    }.toList()
                                    .let { coordinates ->
                                        if (coordinates.isNotEmpty()) {
                                            // Always include brackets, even for a single point
                                            coordinates.joinToString(
                                                ", ",
                                                prefix = "[",
                                                postfix = "]"
                                            )
                                        } else {
                                            val lon = farm.longitude ?: "0.0"
                                            val lat = farm.latitude ?: "0.0"
                                            "[$lon, $lat]"
                                        }
                                    }

                            val line =
                                "${farm.remoteId},${farm.farmerName},${farm.memberId},${getSiteById?.name},${getSiteById?.agentName},${farm.village},${farm.district},${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",${
                                    Date(farm.createdAt)
                                },${Date(farm.updatedAt)}\n"
                            writer.write(line)
                        }
                    } else {
                        val geoJson =
                            buildString {
                                append("{\"type\": \"FeatureCollection\", \"features\": [")
                                listItems.forEachIndexed { index, farm ->
                                    val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                                    val matches = regex.findAll(farm.coordinates.toString())
                                    val geoJsonCoordinates =
                                        matches
                                            .map { match ->
                                                val (lat, lon) = match.destructured
                                                "[$lon, $lat]"
                                            }.joinToString(", ", prefix = "[", postfix = "]")
                                    // Ensure latitude and longitude are not null
                                    val latitude =
                                        farm.latitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0
                                    val longitude =
                                        farm.longitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0

                                    val feature =
                                        """
                                        {
                                            "type": "Feature",
                                            "properties": {
                                                "remote_id": "${farm.remoteId ?: ""}",
                                                "farmer_name": "${farm.farmerName ?: ""}",
                                                "member_id": "${farm.memberId ?: ""}",
                                                "collection_site": "${getSiteById?.name ?: ""}",
                                                "agent_name": "${getSiteById?.agentName ?: ""}",
                                                "farm_village": "${farm.village ?: ""}",
                                                "farm_district": "${farm.district ?: ""}",
                                                 "farm_size": ${farm.size ?: 0.0},
                                                "latitude": $latitude,
                                                "longitude": $longitude,
                                                "created_at": "${farm.createdAt?.let { Date(it) } ?: "null"}",
                                                "updated_at": "${farm.updatedAt?.let { Date(it) } ?: "null"}"
                                                
                                            },
                                            "geometry": {
                                                "type": "${if ((farm.coordinates?.size ?: 0) > 1) "Polygon" else "Point"}",
                                                "coordinates": ${if ((farm.coordinates?.size ?: 0) > 1) "[$geoJsonCoordinates]" else "[$latitude,$longitude]"}
                                            }
                                        }
                                        """.trimIndent()
                                    append(feature)
                                    if (index < listItems.size - 1) append(",")
                                }
                                append("]}")
                            }
                        writer.write(geoJson)
                    }
                }
            }
            return true
        } catch (e: IOException) {
            Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
            return false
        }
    }

    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val context = activity?.applicationContext
                    if (context != null && createFile(context, uri)) {
                        Toast.makeText(context, R.string.success_export_msg, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    fun initiateFileCreation(activity: Activity) {
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

    fun exportFile(activity: Activity) {
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
                    Action.Export -> exportFile(activity)
                    Action.Share -> shareFileAction()
                    else -> {}
                }
            },
        )
    }
    if (showConfirmationDialog) {
        ConfirmationDialog(
            listItems,
            action = action!!, // Ensure action is not null
            onConfirm = {
                when (action) {
                    Action.Export -> initiateFileCreation(activity)
                    Action.Share -> {
                        val file = createFileForSharing()
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
        println("site ID am Using: $siteId")
        // ImportFileDialog( siteId,onDismiss = { showImportDialog = false ; refreshTrigger = !refreshTrigger},navController = navController)
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

//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {

    // Function to show data or no data message
    @Composable
    fun showDataContent() {
        val hasData = listItems.isNotEmpty() // Check if there's data available

        if (hasData) {
            Column {
                // Only show the TabRow and HorizontalPager if there is data
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
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
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) { page ->
                    val filteredListItems = when (page) {
                        1 -> listItems.filter { it.needsUpdate }
                        else -> listItems
                    }.filter {
                        it.farmerName.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredListItems.isNotEmpty() || searchQuery.isNotEmpty()) {
                        // Show the list only when loading is complete
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            val filteredList = filteredListItems.filter {
                                it.farmerName.contains(searchQuery, ignoreCase = true)
                            }

                            if (filteredList.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top,
                                    ) {
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
                            } else {
                                items(filteredList) { farm ->
                                    FarmCard(
                                        farm = farm,
                                        onCardClick = {
                                            navController.currentBackStackEntry?.arguments?.apply {
                                                putParcelableArrayList(
                                                    "coordinates",
                                                    farm.coordinates?.map {
                                                        it.first?.let { it1 ->
                                                            it.second?.let { it2 ->
                                                                ParcelablePair(
                                                                    it1, it2
                                                                )
                                                            }
                                                        }
                                                    }?.let { ArrayList(it) }
                                                )
                                                putParcelable(
                                                    "farmData",
                                                    ParcelableFarmData(farm, "view")
                                                )
                                            }
                                            navController.navigate(route = "setPolygon")
                                        },
                                        onDeleteClick = {
                                            selectedIds.add(farm.id)
                                            selectedFarm.value = farm
                                            showDeleteDialog.value = true
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
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
                onAddFarmClicked = { navController.navigate("addFarm/${siteId}") },
                onBackClicked = { navController.navigate("siteList") },
                onBackSearchClicked = { navController.navigate("farmList/${siteId}") },
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
                showAdd = true,
                showExport = listItems.isNotEmpty(),
                showShare = listItems.isNotEmpty(),
                showSearch = listItems.isNotEmpty(),
                onRestoreClicked = {
                    farmViewModel.restoreData(
                        deviceId = deviceId,
                        phoneNumber = "",
                        email = "",
                        farmViewModel = farmViewModel
                    ) { success ->
                        if (success) {
                            finalMessage = context.getString(R.string.data_restored_successfully)
                        } else {
                            showFinalMessage = true
                            showRestorePrompt = true
                        }
                    }
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
                    modifier = Modifier.padding(end = 0.dp, bottom = 48.dp)
                        .background(MaterialTheme.colorScheme.background).align(BottomEnd)
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
                    .padding(top=72.dp)
                    .fillMaxSize()
            ) {
                // Display a completion message
                val status = restoreStatus as RestoreStatus.Success
//                Text(
//                    text = stringResource(
//                        R.string.restoration_completed,
//                        status.addedCount,
//                        status.sitesCreated
//                    ),
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .fillMaxWidth(),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.bodyMedium
//                )
                // Show the toast
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.restoration_completed,
                        status.addedCount,
                        status.sitesCreated
                    ),
                    Toast.LENGTH_LONG
                ).show()
                showRestorePrompt = false // Hide the restore prompt if restoration is successful
                // showDataContent()
            }
        }

        is RestoreStatus.Error -> {
            // Display an error message
            val status = restoreStatus as RestoreStatus.Error

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showRestorePrompt) {
                    Column(
                        modifier = Modifier
//                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
//                        Text(
//                            text = stringResource(id = R.string.no_data_available),
//                            modifier = Modifier.padding(bottom = 16.dp),
//                            textAlign = TextAlign.Center,
//                            style = MaterialTheme.typography.bodyMedium
//                        )

                    if(showFinalMessage) {
                        // Show the toast with the final message
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
                                    stringResource(id = R.string.phone_number,),
                                    color = inputLabelColor
                                )
                            },
                            supportingText = {
                                if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) Text(
                                    stringResource(R.string.error_invalid_phone_number, phone)
                                )
                            },
                            isError = phone.isNotEmpty() && !isValidPhoneNumber(phone),
                            colors = TextFieldDefaults.textFieldColors(
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
                            colors = TextFieldDefaults.textFieldColors(
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
                                            false // Hide the restore prompt on retry
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
                    // Display a message indicating no data available
//                    Text(
//                        text = finalMessage,
//                        modifier = Modifier.padding(16.dp),
//                        textAlign = TextAlign.Center,
//                        style = MaterialTheme.typography.bodyMedium
//                    )

                    // Show the toast
                    Toast.makeText(
                        context,
                        finalMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    // showDataContent()
                }
            }
        }

        null -> {
            if (isLoading.value) {
                // Show loader while data is loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(top=48.dp)
//                        .fillMaxSize()
                ) {
                    // Display data or no data message if loading is complete
                    // showDataContent()
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
    }
}




@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ImportFileDialog(
    siteId: Long,
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val farmViewModel: FarmViewModel = viewModel()
    var selectedFileType by remember { mutableStateOf("") }
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }
    // var importCompleted by remember { mutableStateOf(false) }

    // Create a launcher to handle the file picker result
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        val result = farmViewModel.importFile(context, it, siteId)
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        navController.navigate("farmList/$siteId") // Navigate to the refreshed farm list
                        onDismiss() // Dismiss the dialog after import is complete
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.import_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    // Create a launcher to handle the file creation result
    val createDocumentLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(),
        ) { uri: Uri? ->
            uri?.let {
                // Get the template content based on the selected file type
                val templateContent = farmViewModel.getTemplateContent(selectedFileType)
                // Save the template content to the created document
                coroutineScope.launch {
                    try {
                        farmViewModel.saveFileToUri(context, it, templateContent)
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.template_download_failed, Toast.LENGTH_SHORT).show()
                    }
                    onDismiss() // Dismiss the dialog
                }
            }
        }

    // Function to download the template file
    fun downloadTemplate() {
        coroutineScope.launch {
            try {
                // Prompt the user to select where to save the file
                createDocumentLauncher.launch(
                    when (selectedFileType) {
                        "csv" -> "farm_template.csv"
                        "geojson" -> "farm_template.geojson"
                        else -> throw IllegalArgumentException("Unsupported file type: $selectedFileType")
                    },
                )
            } catch (e: Exception) {
                Toast.makeText(context, R.string.template_download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
//            onDismiss()
                           },
        title = { Text(text = stringResource(R.string.import_file)) },
        text = {
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .clickable { isDropdownMenuExpanded = true }
                            .padding(16.dp),
                ) {
                    Text(
                        text = if (selectedFileType.isNotEmpty()) selectedFileType else stringResource(R.string.select_file_type),
                        color = if (selectedFileType.isNotEmpty()) Color.Black else Color.Gray,
                    )
                    DropdownMenu(
                        expanded = isDropdownMenuExpanded,
                        onDismissRequest = { isDropdownMenuExpanded = false },
                    ) {
                        DropdownMenuItem(onClick = {
                            selectedFileType = "csv"
                            isDropdownMenuExpanded = false
                        }, text = { Text("CSV") })
                        DropdownMenuItem(onClick = {
                            selectedFileType = "geojson"
                            isDropdownMenuExpanded = false
                        }, text = { Text("GeoJSON") })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { downloadTemplate() },
                    enabled = selectedFileType.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                ) {
                    Text(stringResource(R.string.download_template))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_file_to_import),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                importLauncher.launch("*/*")
            }) {
                Text(stringResource(R.string.select_file))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
        tonalElevation = 6.dp // Adds a subtle shadow for better UX
    )
}



@Composable
fun DeleteAllDialogPresenter(
    showDeleteDialog: MutableState<Boolean>,
    onProceedFn: () -> Unit,
) {
    if (showDeleteDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = stringResource(id = R.string.delete_this_item)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.are_you_sure))
                    Text(stringResource(id = R.string.item_will_be_deleted))
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

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun FarmListHeader(
//    title: String,
//    onSearchQueryChanged: (String) -> Unit,
//    onAddFarmClicked: () -> Unit,
//    onBackClicked: () -> Unit,
//    onBackSearchClicked: () -> Unit,
//    showAdd: Boolean,
//    showSearch: Boolean,
//) {
//    // State for holding the search query
//    var searchQuery by remember { mutableStateOf("") }
//
//    var isSearchVisible by remember { mutableStateOf(false) }
//
//    TopAppBar(
//        modifier =
//            Modifier
//                .background(MaterialTheme.colorScheme.primary)
//                .fillMaxWidth(),
//        navigationIcon = {
//            IconButton(onClick = onBackClicked) {
//                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back",tint = MaterialTheme.colorScheme.onPrimary)
//            }
//        },
//        title = {
//            Text(
//                text = title,
//                color = MaterialTheme.colorScheme.onPrimary,
//                fontSize = 22.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        },
//        actions = {
//            if (showAdd) {
////                IconButton(onClick = onAddFarmClicked) {
////                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
////                }
//            }
//            if (showSearch) {
//                IconButton(onClick = {
//                    isSearchVisible = !isSearchVisible
//                }) {
//                    Icon(Icons.Default.Search, contentDescription = "Search")
//                }
//            }
//        },
//    )
//    // Conditional rendering of the search field
//    if (isSearchVisible && showSearch) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier =
//                Modifier
//                    .padding(horizontal = 16.dp)
//                    .fillMaxWidth(),
//        ) {
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = {
//                    searchQuery = it
//                    onSearchQueryChanged(it)
//                },
//                modifier =
//                    Modifier
//                        .padding(start = 8.dp)
//                        .weight(1f),
//                label = { Text(stringResource(R.string.search)) },
//                leadingIcon = {
//                    IconButton(onClick = {
//                        // onBackSearchClicked()
//                        searchQuery = ""
//                        onSearchQueryChanged("")
//                        isSearchVisible = !isSearchVisible
//                    }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                singleLine = true,
//                colors =
//                    TextFieldDefaults.outlinedTextFieldColors(
//                        cursorColor = MaterialTheme.colorScheme.onSurface,
//                    ),
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FarmListHeader(
    title: String,
    onSearchQueryChanged: (String) -> Unit,
    onAddFarmClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onBackSearchClicked: () -> Unit,
    showAdd: Boolean,
    showSearch: Boolean,
) {
    // State to hold the search query
    var searchQuery by remember { mutableStateOf("") }

    // State to determine if the search mode is active
    var isSearchVisible by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = {
                if (isSearchVisible) {
                    // Exit search mode, clear search query
                    searchQuery = ""
                    onSearchQueryChanged("")
                    isSearchVisible = false
                } else {
                    // Navigate back normally
                    onBackClicked()
                }
            }) {
//                if (!isSearchVisible) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
//                }
            }
        },
        title = {
//            if (!isSearchVisible) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
//            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = {
                    isSearchVisible = !isSearchVisible
                }) {
//                    if (!isSearchVisible) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
//                    }
                }
            }
        },
    )

    // Show search field when search mode is active
    if (isSearchVisible) {
        Box(
            modifier = Modifier
                .padding(top=54.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the Row within the Box
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center, // Center the contents within the Row
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Center with a smaller width
                        .padding(8.dp)
                        .clip(RoundedCornerShape(0.dp)), // Add rounded corners
                    placeholder = { Text(stringResource(R.string.search), color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = {
                        IconButton(onClick = {
                            // Exit search mode and clear search
                            searchQuery = ""
                            onSearchQueryChanged("")
                            isSearchVisible = false
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery != ""){
                            IconButton(onClick = {
                                // Exit search mode and clear search
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(0.dp) // Set the shape for the field to rounded
                )

            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListHeaderPlots(
    title: String,
    onAddFarmClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onBackSearchClicked: () -> Unit,
    onExportClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onImportClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    showAdd: Boolean,
    showExport: Boolean,
    showShare: Boolean,
    showSearch: Boolean,
    onRestoreClicked: () -> Unit
) {
    val context = LocalContext.current as Activity

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isImportDisabled by remember { mutableStateOf(false) }

   // Column {
        TopAppBar(
            title = {
//                if (!isSearchVisible) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
//                    }
            },
//            navigationIcon = {
//                IconButton(onClick = onBackClicked) {
//                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                }
//            },
            navigationIcon = {
                IconButton(onClick = {
                    if (isSearchVisible) {
                        // Exit search mode, clear search query
                        searchQuery = ""
                        onSearchQueryChanged("")
                        isSearchVisible = false
                    } else {
                        // Navigate back normally
                        onBackClicked()
                    }
                }) {
//                    if ( !isSearchVisible ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
//                    }
                }
            },
            actions = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
//                    if ( !isSearchVisible ){
                    IconButton(
                        onClick = { onRestoreClicked() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restore",
                            modifier = Modifier.size(24.dp)
                        )
                    }
//                }
                    if (showExport
//                        && !isSearchVisible
                        ) {
                        IconButton(onClick = onExportClicked, modifier = Modifier.size(36.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.save),
                                contentDescription = "Export",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    if (showShare
//                        && !isSearchVisible
                        ) {
                        IconButton(onClick = onShareClicked, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
//                    if ( !isSearchVisible ) {
                        IconButton(
                            onClick = {
                                if (!isImportDisabled) {
                                    onImportClicked()
                                    isImportDisabled = true
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            enabled = !isImportDisabled
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icons8_import_file_48),
                                contentDescription = "Import",
                                modifier = Modifier.size(24.dp),
                            )
                        }
//                    }
                    if (showAdd) {
//                        IconButton(onClick = {
//                            val sharedPref =
//                                context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
//                            sharedPref.edit().remove("plot_size").remove("selectedUnit").apply()
//                            onAddFarmClicked()
//                        }, modifier = Modifier.size(36.dp)) {
//                            Icon(
//                                Icons.Default.Add,
//                                contentDescription = "Add",
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
                    }

                    if (showSearch) {
                        IconButton(onClick = {
                            isSearchVisible = !isSearchVisible
                        },modifier = Modifier.size(36.dp)) {
//                            if (!isSearchVisible) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
//                            }
                        }
                    }
                }
            },
        )

    // Show search field when search mode is active
    if (isSearchVisible) {
        Box(
            modifier = Modifier
                .padding(top=54.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the Row within the Box
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center, // Center the contents within the Row
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Center with a smaller width
                        .padding(8.dp)
                        .clip(RoundedCornerShape(0.dp)), // Add rounded corners
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        IconButton(onClick = {
                            // Exit search mode and clear search
                            searchQuery = ""
                            onSearchQueryChanged("")
                            isSearchVisible = false
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery != ""){
                            IconButton(onClick = {
                                // Exit search mode and clear search
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(0.dp) // Set the shape for the field to rounded
                )

            }
        }
    }
    // }
}

@Composable
fun FarmCard(
    farm: Farm,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

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
                    Text(
                        text = farm.farmerName,
                        style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                        modifier =
                        Modifier
                            .weight(1.1f)
                            .padding(bottom = 4.dp),
                    )
                    Text(
                        text = "${stringResource(id = R.string.size)}: ${formatInput(farm.size.toString())} ${
                            stringResource(id = R.string.ha)
                        }",
                        style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                        modifier =
                        Modifier
                            .weight(0.9f)
                            .padding(bottom = 4.dp),
                    )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${stringResource(id = R.string.village)}: ${farm.village}",
                        style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${stringResource(id = R.string.district)}: ${farm.district}",
                        style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Show the label if the farm needs an update
                if (farm.needsUpdate) {
                    Text(
                        text = stringResource(id = R.string.needs_update),
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,  // Adjust font size
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


fun OutputStream.writeCsv(farms: List<Farm>) {
    val writer = bufferedWriter()
    writer.write(""""Farmer Name", "Village", "District"""")
    writer.newLine()
    farms.forEach {
        writer.write("${it.farmerName}, ${it.village}, \"${it.district}\"")
        writer.newLine()
    }
    writer.flush()
}

// on below line creating a method to write data to txt file.
private fun writeTextData(
    file: File,
    farms: List<Farm>,
    onDismiss: () -> Unit,
    format: String,
) {
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(file)

        fileOutputStream
            .write(
                """"Farmer Name", "Village", "District", "Size in Ha", "Cherry harvested this year in Kgs", "latitude", "longitude" , "createdAt", "updatedAt" """
                    .toByteArray(),
            )
        fileOutputStream.write(10)
        farms.forEach {
            fileOutputStream.write(
                "${it.farmerName}, ${it.village},${it.district},${it.size},${it.purchases},${it.latitude},${it.longitude},${
                    Date(
                        it.createdAt,
                    )
                }, \"${Date(it.updatedAt)}\"".toByteArray(),
            )
            fileOutputStream.write(10)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UpdateFarmForm(
    navController: NavController,
    farmId: Long?,
    listItems: List<Farm>,
) {
    val floatValue = 123.45f
    val item =
        listItems.find { it.id == farmId } ?: Farm(
//        id = 0,
            siteId = 0L,
            farmerName = "Default Farmer",
            memberId = "",
            farmerPhoto = "Default photo",
            village = "Default Village",
            district = "Default District",
            latitude = "Default Village",
            longitude = "Default Village",
            coordinates = null,
            size = floatValue,
            purchases = floatValue,
            createdAt = 1L,
            updatedAt = 1L,
        )
    val context = LocalContext.current as Activity
    var farmerName by remember { mutableStateOf(item.farmerName) }
    var memberId by remember { mutableStateOf(item.memberId) }
    var farmerPhoto by remember { mutableStateOf(item.farmerPhoto) }
    var village by remember { mutableStateOf(item.village) }
    var district by remember { mutableStateOf(item.district) }
    var size by remember { mutableStateOf(item.size.toString()) }
    var latitude by remember { mutableStateOf(item.latitude) }
    var longitude by remember { mutableStateOf(item.longitude) }
    var coordinates by remember { mutableStateOf(item.coordinates) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel =
        viewModel(
            factory = FarmViewModelFactory(context.applicationContext as Application),
        )
    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }
    val showLocationDialogNew = remember { mutableStateOf(false) }
    val showPermissionRequest = remember { mutableStateOf(false) }
    val file = context.createImageFile()
    val uri =
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            context.packageName + ".provider",
            file,
        )
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("Ha", "Acres", "Sqm", "Timad", "Fichesa", "Manzana", "Tarea")
    var selectedUnit by remember { mutableStateOf(items[0]) }

    val scientificNotationPattern = Pattern.compile("([+-]?\\d*\\.?\\d+)[eE][+-]?\\d+")

    LaunchedEffect(Unit) {
        if (!isLocationEnabled(context)) {
            showLocationDialog.value = true
        }
    }

    // Define string constants
    val titleText = stringResource(id = R.string.enable_location_services)
    val messageText = stringResource(id = R.string.location_services_required_message)
    val enableButtonText = stringResource(id = R.string.enable)

    // Dialog to prompt user to enable location services
    if (showLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { showLocationDialog.value = false },
            title = { Text(titleText) },
            text = { Text(messageText) },
            confirmButton = {
                Button(onClick = {
                    showLocationDialog.value = false
                    promptEnableLocation(context)
                }) {
                    Text(enableButtonText)
                }
            },
            dismissButton = {
                Button(onClick = {
                    showLocationDialog.value = false
                    Toast.makeText(context, R.string.location_permission_denied_message, Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
        )
    }
    if (navController.currentBackStackEntry!!.savedStateHandle.contains("coordinates")) {
        val parcelableCoordinates = navController.currentBackStackEntry!!
            .savedStateHandle
            .get<List<ParcelablePair>>("coordinates")

        coordinates = parcelableCoordinates?.map { Pair(it.first, it.second) }
    }


    val fillForm = stringResource(id = R.string.fill_form)

    fun validateForm(): Boolean {
        var isValid = true

        if (farmerName.isBlank()) {
            isValid = false
        }

        if (village.isBlank()) {
            isValid = false
        }

        if (district.isBlank()) {
            isValid = false
        }

        if (size.toFloatOrNull()?.let { it > 0 } != true) {
            isValid = false
        }

        if (latitude.isBlank() || longitude.isBlank()) {
            isValid = false
        }

        return isValid
    }

    /**
     * Updating Farm details
     * Before sending to the database
     */

    fun updateFarmInstance() {
        val isValid = validateForm()
        if (isValid) {
            item.farmerPhoto = ""
            item.farmerName = farmerName
            item.memberId = memberId
            item.latitude = latitude
            item.village = village
            item.district = district
            item.longitude = longitude
            if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) >= 4) {
                if ((coordinates?.size ?: 0) < 3) {
                    Toast
                        .makeText(
                            context,
                            R.string.error_polygon_points,
                            Toast.LENGTH_SHORT,
                        ).show()
                    return
                }
                item.coordinates = coordinates?.plus(coordinates?.first()) as List<Pair<Double, Double>>
            } else {
                item.coordinates = listOf(Pair(item.longitude.toDoubleOrNull() ?: 0.0, item.latitude.toDoubleOrNull() ?: 0.0)) // Example default value
            }
            item.size = convertSize(size.toDouble(), selectedUnit).toFloat()
            item.purchases = 0.toFloat()
            item.updatedAt = Instant.now().millis
            updateFarm(farmViewModel, item)
            item.needsUpdate = false
            val returnIntent = Intent()
            context.setResult(Activity.RESULT_OK, returnIntent)
            navController.navigate("farmList/$siteID")
        } else {
            Toast.makeText(context, fillForm, Toast.LENGTH_SHORT).show()
        }
    }
    // Confirm farm update and ask if they wish to capture new polygon
    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.update_farm)) },
            text = {
                Column {
                    Text(text = stringResource(id = R.string.confirm_update_farm))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    updateFarmInstance()
                }) {
                    Text(text = stringResource(id = R.string.update_farm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick =
                        {
                            showDialog.value = false
                            navController.navigate("setPolygon")
                        },
                ) {
                    Text(text = stringResource(id = R.string.set_polygon))
                }
            },
        )
    }

    val scrollState = rememberScrollState()
    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val buttonColor = if (isDarkTheme) Color.Black else Color.White
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    if (showPermissionRequest.value) {
        LocationPermissionRequest(
            onLocationEnabled = {
                showLocationDialog.value = true
            },
            onPermissionsGranted = {
                showPermissionRequest.value = false
            },
            onPermissionsDenied = {
                // Handle permissions denied
                // Show a message or take appropriate action
            },
            showLocationDialogNew = showLocationDialogNew,
            hasToShowDialog = showLocationDialogNew.value,
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
//                .padding(16.dp)
                .verticalScroll(state = scrollState),
    ) {
            FarmListHeader(
                title = stringResource(id = R.string.update_farm),
                onSearchQueryChanged = {},
                onAddFarmClicked = { /* Handle adding a farm here */ },
                onBackClicked = { navController.popBackStack() },
                onBackSearchClicked = {},
                showAdd = false,
                showSearch = false,
            )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = { focusRequester1.requestFocus() },
                ),
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text(stringResource(id = R.string.farm_name), color = inputLabelColor) },
            isError = farmerName.isBlank(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            focusRequester1.requestFocus()
                            true
                        }
                        false
                    },
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = { focusRequester1.requestFocus() },
                ),
            value = memberId,
            onValueChange = { memberId = it },
            label = { Text(stringResource(id = R.string.member_id), color = inputLabelColor) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            focusRequester1.requestFocus()
                        }
                        false
                    },
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = { focusRequester2.requestFocus() },
                ),
            value = village,
            onValueChange = { village = it },
            label = { Text(stringResource(id = R.string.village), color = inputLabelColor) },
            modifier =
                Modifier
                    .focusRequester(focusRequester1)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = { focusRequester3.requestFocus() },
                ),
            value = district,
            onValueChange = { district = it },
            label = { Text(stringResource(id = R.string.district), color = inputLabelColor) },
            modifier =
                Modifier
                    .focusRequester(focusRequester2)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                singleLine = true,
                value = truncateToDecimalPlaces(size,9),
                onValueChange = {
                    size = it
                },
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                    ),
                label = { Text(stringResource(id = R.string.size_in_hectares) + " (*)", color = inputLabelColor) },
                isError = size.toFloatOrNull() == null || size.toFloat() <= 0, // Validate size
                colors =
                    TextFieldDefaults.textFieldColors(
                        errorLeadingIconColor = Color.Red,
                        cursorColor = inputTextColor,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = inputBorder,
                        unfocusedIndicatorColor = inputBorder,
                        errorIndicatorColor = Color.Red,
                    ),
                modifier =
                    Modifier
                        .focusRequester(focusRequester3)
                        .weight(1f)
                        .padding(bottom = 16.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))
            // Size measure
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                },
                modifier = Modifier.weight(1f),
            ) {
                TextField(
                    readOnly = true,
                    value = selectedUnit,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.unit)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded,
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                ) {
                    items.forEach { selectionOption ->
                        DropdownMenuItem(
                            { Text(text = selectionOption) },
                            onClick = {
                                selectedUnit = selectionOption
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
        if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) < 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextField(
                    readOnly = true,
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(id = R.string.latitude), color = inputLabelColor) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp),
                )
                Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
                TextField(
                    readOnly = true,
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(id = R.string.longitude), color = inputLabelColor) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp),
                )
            }
        }
        Button(
            onClick = {
                showPermissionRequest.value = true
                if (!isLocationEnabled(context)) {
                    showLocationDialog.value = true
                } else {
                    if (isLocationEnabled(context) && context.hasLocationPermission()) {
                        if (size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }?.let { it < 4f } == true) {
                            // Simulate collecting latitude and longitude
                            if (context.hasLocationPermission()) {
                                val locationRequest =
                                    LocationRequest.create().apply {
                                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                        interval = 10000 // Update interval in milliseconds
                                        fastestInterval =
                                            5000 // Fastest update interval in milliseconds
                                    }

                                fusedLocationClient.requestLocationUpdates(
                                    locationRequest,
                                    object : LocationCallback() {
                                        override fun onLocationResult(locationResult: LocationResult) {
                                            locationResult.lastLocation?.let { lastLocation ->
                                                // Handle the new location
                                                latitude = "${lastLocation.latitude}"
                                                longitude = "${lastLocation.longitude}"
                                                // Log.d("FARM_LOCATION", "loaded success,,,,,,,")
                                            }
                                        }
                                    },
                                    Looper.getMainLooper(),
                                )
                            }
                        } else {
                            if (isLocationEnabled(context)) {
                                navController.navigate("setPolygon")
                            }
                        }
                    } else {
                        showPermissionRequest.value = true
                        showLocationDialog.value = true
                    }
                }
            },
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 5.dp)
                    .height(50.dp),
            enabled = size.toFloatOrNull() != null,
        ) {
            Text(
                // text = if (size.toFloatOrNull() != null && size.toFloat() < 4) stringResource(id = R.string.get_coordinates) else stringResource(
                text =
                    if (size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }?.let { it < 4f } ==
                        true
                    ) {
                        stringResource(id = R.string.get_coordinates)
                    } else {
                        stringResource(
                            id = R.string.set_new_polygon,
                        )
                    },
            )
        }
        Button(
            onClick = {
                if (validateForm()) {
                    showDialog.value = true
                } else {
                    Toast.makeText(context, fillForm, Toast.LENGTH_SHORT).show()
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
        ) {
            Text(text = stringResource(id = R.string.update_farm))
        }
    }
}

fun updateFarm(
    farmViewModel: FarmViewModel,
    item: Farm,
) {
    farmViewModel.updateFarm(item)
}
