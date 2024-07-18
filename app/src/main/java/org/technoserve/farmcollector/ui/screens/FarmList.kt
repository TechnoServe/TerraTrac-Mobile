package org.technoserve.farmcollector.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity.MODE_PRIVATE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
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
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

//data class Farm(val farmerName: String, val village: String, val district: String)
var siteID = 0L


enum class Action {
    Export,
    Share
}

@Composable
fun FormatSelectionDialog(
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
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
                        onClick = { selectedFormat = "CSV" }
                    )
                    Text(stringResource(R.string.csv))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFormat == "GeoJSON",
                        onClick = { selectedFormat = "GeoJSON" }
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
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun FarmList(navController: NavController, siteId: Long) {
    siteID = siteId
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val listItems by farmViewModel.readAllData(siteId).observeAsState(listOf())
    val cwsListItems by farmViewModel.readAllSites.observeAsState(listOf())
    // var showExportDialog by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<Action?>(null) }
    val activity = context as Activity
    var exportFormat by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }

    // Inside your composable function
    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }


    fun createFileforsharing(): File? {
        // Get the current date and time
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val getSiteById = cwsListItems.find { it.siteId == siteID }
        val siteName = getSiteById?.name ?: "SiteName"
        val filename = if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.json"
        val mimeType = if (exportFormat == "CSV") "text/csv" else "application/json"
        // Get the Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)


        try {
            file.bufferedWriter().use { writer ->
                if (exportFormat == "CSV") {
                    writer.write("remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n")
                    listItems.forEach { farm ->
                        val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                        val matches = regex.findAll(farm.coordinates.toString())
                        // Reverse the coordinates and format with brackets
                        val reversedCoordinates = matches.map { match ->
                            val (lat, lon) = match.destructured
                            "[$lon, $lat]"
                        }.joinToString(", ", prefix = "[", postfix = "]")
                        val line = "${farm.remoteId},${farm.farmerName},${farm.memberId},${getSiteById?.name},${getSiteById?.agentName},${farm.village},${farm.district},${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",${Date(farm.createdAt)},${Date(farm.updatedAt)}\n"
                        writer.write(line)
                    }
                } else {
                    val geoJson = buildString {
                        append("{\"type\": \"FeatureCollection\", \"features\": [")
                        listItems.forEachIndexed { index, farm ->
                            val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                            val matches = regex.findAll(farm.coordinates.toString())
                            val geoJsonCoordinates = matches.map { match ->
                                val (lat, lon) = match.destructured
                                "[$lon, $lat]"
                            }.joinToString(", ", prefix = "[", postfix = "]")
                            append("""
                            {
                                "type": "Feature",
                                "properties": {
                                    "remote_id": "${farm.remoteId}",
                                    "farmer_name": "${farm.farmerName}",
                                    "member_id": "${farm.memberId}",
                                    "collection_site": "${getSiteById?.name}",
                                    "agent_name": "${getSiteById?.agentName}",
                                    "farm_village": "${farm.village}",
                                    "farm_district": "${farm.district}",
                                    "farm_size": ${farm.size},
                                    "latitude": ${farm.latitude},
                                    "longitude": ${farm.longitude},
                                    "created_at": "${Date(farm.createdAt)}",
                                    "updated_at": "${Date(farm.updatedAt)}"
                                },
                                "geometry": {
                                    "type": "${if (farm.coordinates!!.size > 1) "Polygon" else "Point"}",
                                    "coordinates": [${if (farm.coordinates?.isEmpty() == true) "[${farm.longitude}, ${farm.latitude}]" else geoJsonCoordinates}]
                                }
                            }${if (index == listItems.size - 1) "" else ","}
                        """.trimIndent())
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
fun createFile(context: Context, uri: Uri): Boolean {
    // Get the current date and time
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val getSiteById = cwsListItems.find { it.siteId == siteID }
    val siteName = getSiteById?.name ?: "SiteName"
    val filename = if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.json"

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                if (exportFormat == "CSV") {
                    writer.write("remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n")
                    listItems.forEach { farm ->
                        val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                        val matches = regex.findAll(farm.coordinates.toString())
                        val reversedCoordinates = matches.map { match ->
                            val (lat, lon) = match.destructured
                            "[$lon, $lat]"
                        }.joinToString(", ", prefix = "[", postfix = "]")
                        val line = "${farm.remoteId},${farm.farmerName},${farm.memberId},${getSiteById?.name},${getSiteById?.agentName},${farm.village},${farm.district},${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",${Date(farm.createdAt)},${Date(farm.updatedAt)}\n"
                        writer.write(line)
                    }
                } else {
                    val geoJson = buildString {
                        append("{\"type\": \"FeatureCollection\", \"features\": [")
                        listItems.forEachIndexed { index, farm ->
                            val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                            val matches = regex.findAll(farm.coordinates.toString())
                            val geoJsonCoordinates = matches.map { match ->
                                val (lat, lon) = match.destructured
                                "[$lon, $lat]"
                            }.joinToString(", ", prefix = "[", postfix = "]")
                            append("""
                            {
                                "type": "Feature",
                                "properties": {
                                    "remote_id": "${farm.remoteId}",
                                    "farmer_name": "${farm.farmerName}",
                                    "member_id": "${farm.memberId}",
                                    "collection_site": "${getSiteById?.name}",
                                    "agent_name": "${getSiteById?.agentName}",
                                    "farm_village": "${farm.village}",
                                    "farm_district": "${farm.district}",
                                    "farm_size": ${farm.size},
                                    "latitude": ${farm.latitude},
                                    "longitude": ${farm.longitude},
                                    "created_at": "${Date(farm.createdAt)}",
                                    "updated_at": "${Date(farm.updatedAt)}"
                                },
                                "geometry": {
                                    "type": "${if (farm.coordinates!!.size > 1) "Polygon" else "Point"}",
                                    "coordinates": [${if (farm.coordinates?.isEmpty() == true) "[${farm.longitude}, ${farm.latitude}]" else geoJsonCoordinates}]
                                }
                            }${if (index == listItems.size - 1) "" else ","}
                        """.trimIndent())
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
                    Toast.makeText(context, R.string.success_export_msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun initiateFileCreation(activity: Activity) {
        val mimeType = if (exportFormat == "CSV") "text/csv" else "application/json"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            val getSiteById = cwsListItems.find { it.siteId == siteID }
            val siteName = getSiteById?.name ?: "SiteName"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.json"
            putExtra(Intent.EXTRA_TITLE, filename)
        }
        createDocumentLauncher.launch(intent)
    }





    // Function to share the file
    fun shareFile(file: File) {
        val fileURI: Uri = context.let {
            FileProvider.getUriForFile(
                it,
                context.applicationContext.packageName.toString() + ".provider",
                file
            )
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (exportFormat == "CSV") "text/csv" else "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
            putExtra(Intent.EXTRA_TEXT, "Sharing the farm data file.")
            putExtra(Intent.EXTRA_STREAM, fileURI)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share file")
        activity.startActivity(chooserIntent)
    }

    // Function to handle the export (save) action
//    fun exportFile() {
//        val file = createFile()
//        if (file != null) {
//            // Directly save the file to the device
//            Toast.makeText(
//                context,
//                R.string.success_export_msg,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
    fun exportFile(activity: Activity) {
        initiateFileCreation(activity)
    }


    // Function to handle the share action
    fun shareFileAction() {
        val file = createFileforsharing()
        if (file != null) {
            shareFile(file)
        }
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
            }
        )
    }

/*
    // For Android 10 (API 29) and above
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createFileInDownloadsAndroidQAndAbove(context: Context, filename: String, format: String): File? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, if (format == "CSV") "text/csv" else "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let {
            val outputStream = resolver.openOutputStream(it)
            val file = File(context.cacheDir, filename) // Temporary file
            try {
                // Write data to file
                writeTextData(file, listItems, {}, format)
                // Copy file content to the output stream
                outputStream?.use { output -> file.inputStream().copyTo(output) }
                file
            } catch (e: IOException) {
                Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
                null
            }
        }
    }


    // For Android 9 (API 28) and below
    fun createFileInDownloadsPreAndroidQ(filename: String, format: String): File? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)
        try {
            // Write data to file
            writeTextData(file, listItems, {}, format)
            return file
        } catch (e: IOException) {
            Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
            return null
        }
    }


    fun createFile(context: Context, format: String): File? {
        val filename = if (format == "CSV") "farms.csv" else "farms.json"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createFileInDownloadsAndroidQAndAbove(context, filename, format)
        } else {
            createFileInDownloadsPreAndroidQ(filename, format)
        }
    }

    // Function to handle the file creation
    fun createFile(): File? {
        return createFile(context, exportFormat)
    }

    // Function to share the file
    fun shareFile(file: File) {
        val fileURI: Uri = context.let {
            FileProvider.getUriForFile(
                it,
                context.applicationContext.packageName.toString() + ".provider",
                file
            )
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (exportFormat == "CSV") "text/csv" else "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
            putExtra(Intent.EXTRA_TEXT, "Sharing the farm data file.")
            putExtra(Intent.EXTRA_STREAM, fileURI)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share file")
        activity.startActivity(chooserIntent)
    }

    // Function to handle the export (save) action
    fun exportFile() {
        val file = createFile()
        if (file != null) {
            // Notify user of successful export
            Toast.makeText(
                context,
                R.string.success_export_msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Function to handle the share action
    fun shareFileAction() {
        val file = createFile()
        if (file != null) {
            shareFile(file)
        }
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
            }
        )
    }
    */




    if (showImportDialog) {
        println("site ID am Using: $siteId")
        ImportFileDialog( siteId,onDismiss = { showImportDialog = false })
    }


    fun onDelete() {
        val toDelete = mutableListOf<Long>()
        toDelete.addAll(selectedIds)
        farmViewModel.deleteList(toDelete)
        selectedIds.removeAll(selectedIds)
        showDeleteDialog.value = false
    }

    if (listItems.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                FarmListHeaderPlots(
                    title = stringResource(id = R.string.farm_list),
                    onAddFarmClicked = { navController.navigate("addFarm/${siteId}") },
                    // onBackClicked = { navController.navigateUp() }, siteList
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
                    showAdd = true,
                    showExport = listItems.isNotEmpty(),
                    showShare = listItems.isNotEmpty(),
                    showSearch= listItems.isNotEmpty()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            /*
            item {

                Button(onClick = { showExportDialog = true }) {
                    Text(stringResource(id = R.string.export_data))
                }

                if (showExportDialog) {
                    ExportDataDialog(
                        onDismiss = { showExportDialog = false },
                        listItems,
                        cwsListItems
                    )
                }
            }
            */

//            items(listItems) { farm ->
//                FarmCard(farm = farm, onCardClick = {
//                    Bundle().apply {
//                        putSerializable("coordinates",
//                            farm.coordinates?.let { ArrayList(it) })
//                    }
//
//                    navController.currentBackStackEntry?.arguments?.apply {
//                        putSerializable("farmData", Pair(farm, "view"))
//                    }
//                    navController.navigate(route = "setPolygon")
//                }, onDeleteClick = {
//                    // When the delete icon is clicked, invoke the onDelete function
//                    selectedIds.add(farm.id)
//                    showDeleteDialog.value = true
//                })
//                Spacer(modifier = Modifier.height(16.dp))
//            }
            items(listItems.filter {
                it.farmerName.contains(searchQuery, ignoreCase = true)
                // Adjust filtering based on your farm data structure
            }) { farm ->
                FarmCard(
                    farm = farm,
                    onCardClick = {
                        Bundle().apply {
                            putSerializable("coordinates",
                                farm.coordinates?.let { ArrayList(it) })
                        }

                        navController.currentBackStackEntry?.arguments?.apply {
                            putSerializable("farmData", Pair(farm, "view"))
                        }
                        navController.navigate(route = "setPolygon")
                    },
                    onDeleteClick = {
                        selectedIds.add(farm.id)
                        showDeleteDialog.value = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (showDeleteDialog.value) {
            DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FarmListHeaderPlots(
                title = stringResource(id = R.string.farm_list),
                onAddFarmClicked = { navController.navigate("addFarm/${siteId}") },
                onBackClicked = { navController.navigateUp() },
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
                showSearch = true,
            )
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


@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ImportFileDialog(siteId: Long,onDismiss: () -> Unit) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val farmViewModel: FarmViewModel = viewModel()
    var selectedFileType by remember { mutableStateOf("") }
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }

    // Create a launcher to handle the file picker result
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val result = farmViewModel.importFile(context, it, siteId)
                    println("site ID am Using in import dialog: $siteId")
                    println("Import result: ${result.success}")
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    if (result.duplicateFarms.isNotEmpty()) {
                        Toast.makeText(context, "Duplicate farms found", Toast.LENGTH_SHORT).show()
                        println("Duplicate farms found:")
                        result.duplicateFarms.forEach { println(it) }
                    }
                    // Handle farms needing updates
                    if (result.farmsNeedingUpdate.isNotEmpty()) {
                        println("Farms that needs to be updated found:")
                        // Update the UI to mark farms that need updates
                       // markFarmsNeedingUpdate(result.farmsNeedingUpdate)
                    }
                    // Retrieve imported farms and flag those without plot info
                    val importedFarms = result.importedFarms // Adjust to your actual data
                    println("Imported farms now: $importedFarms")
                    farmViewModel.flagFarmersWithNewPlotInfo(siteId, importedFarms)
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.import_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create a launcher to handle the file creation result
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
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
                        "json" -> "farm_template.json"
                        else -> throw IllegalArgumentException("Unsupported file type: $selectedFileType")
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, R.string.template_download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.import_file)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.select_file_type),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { isDropdownMenuExpanded = true }
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (selectedFileType.isNotEmpty()) selectedFileType else stringResource(R.string.select_file_type),
                        color = if (selectedFileType.isNotEmpty()) Color.Black else Color.Gray
                    )
                    DropdownMenu(
                        expanded = isDropdownMenuExpanded,
                        onDismissRequest = { isDropdownMenuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = { selectedFileType = "csv";isDropdownMenuExpanded = false }, text = { Text("CSV") })
                        DropdownMenuItem(onClick = { selectedFileType = "json"; isDropdownMenuExpanded = false}, text = { Text("GeoJSON") })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { downloadTemplate()},
                    enabled = selectedFileType.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(stringResource(R.string.download_template))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_file_to_import),
                    modifier = Modifier.padding(bottom = 8.dp)
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
        }
    )


}

/*
@Composable
fun ExportDataDialog(
    onDismiss: () -> Unit,
    farms: List<Farm>,
    cwsListItems: List<CollectionSite>
) {
    var exportFormat by remember { mutableStateOf("CSV") }
    // Handle export logic here
    val context = LocalContext.current
    val activity = context as Activity
    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // Handle the result as needed
                if (data != null) {
                    val uri = data.data
                    uri?.let {
                        val contentResolver = context.contentResolver
                        val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                        val getSiteById = cwsListItems.find { it.siteId == siteID }

                        if (outputStream != null) {
                            PrintWriter(outputStream.bufferedWriter()).use { writer ->
                                if (exportFormat == "CSV") {
                                    // Write the header row
                                    writer.println("farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at")

                                    // Write each farm's data
                                    for (farm in farms) {
                                        val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                                        val matches = regex.findAll(farm.coordinates.toString())

                                        // Reverse the coordinates and format with brackets
                                        val reversedCoordinates = matches.map { match ->
                                            val (lat, lon) = match.destructured
                                            "[$lon, $lat]"
                                        }.joinToString(", ", prefix = "[", postfix = "]")
                                        val line =
                                            "${farm.farmerName},${farm.memberId},${getSiteById?.name},${getSiteById?.agentName},${farm.village},${farm.district},${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",${
                                                Date(farm.createdAt)
                                            },${Date(farm.updatedAt)}"
                                        writer.println(line)
                                    }
                                } else {
                                    // GeoJSON format
                                    val geoJson = buildString {
                                        append("{\"type\": \"FeatureCollection\", \"features\": [")
                                        for (farm in farms) {
                                            val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                                            val matches = regex.findAll(farm.coordinates.toString())

                                            // Reverse the coordinates and format as GeoJSON coordinates
                                            val geoJsonCoordinates = matches.map { match ->
                                                val (lat, lon) = match.destructured
                                                "[$lon, $lat]"
                                            }.joinToString(", ", prefix = "[", postfix = "]")

                                            append(
                                                """
                                                {
                                                    "type": "Feature",
                                                    "properties": {
                                                        "remote_id": "${farm.remoteId}",
                                                        "farmer_name": "${farm.farmerName}",
                                                        "member_id": "${farm.memberId}",
                                                        "collection_site": "${getSiteById?.name}",
                                                        "agent_name": "${getSiteById?.agentName}",
                                                        "farm_village": "${farm.village}",
                                                        "farm_district": "${farm.district}",
                                                        "farm_size": ${farm.size},
                                                        "latitude": ${farm.latitude},
                                                        "longitude": ${farm.longitude},
                                                        "created_at": "${Date(farm.createdAt)}",
                                                        "updated_at": "${Date(farm.updatedAt)}"
                                                    },
                                                    "geometry": {
                                                        "type": "${
                                                    if (farm.coordinates!!.size > 1) "Polygon" else "Point"
                                                }",
                                                        "coordinates": [
                                                            ${
                                                    if (farm.coordinates?.isEmpty() == true) {
                                                        "[${farm.longitude}, ${farm.latitude}]"
                                                    } else {
                                                        geoJsonCoordinates
                                                    }
                                                }
                                                        ]
                                                    }
                                                }${
                                                    if (farms.indexOf(farm) == farms.size - 1) "" else ","
                                                }
                                                """.trimIndent()
                                            )
                                        }
                                        append("]}")
                                    }
                                    writer.println(geoJson)
                                }

                                onDismiss()

                                Toast.makeText(
                                    context,
                                    R.string.success_export_msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.export_data)) },
        text = {
            Column {
                Text(stringResource(id = R.string.export_data_desc))
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = exportFormat == "CSV",
                        onClick = { exportFormat = "CSV" }
                    )
                    Text("CSV")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = exportFormat == "GeoJSON",
                        onClick = { exportFormat = "GeoJSON" }
                    )
                    Text("GeoJSON")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23
                    )
                    val filename = if (exportFormat == "CSV") "farms.csv" else "farms.json"
                    val mimeType = if (exportFormat == "CSV") "text/csv" else "application/json"
                    val file =
                        File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)

                    try {
                        writeTextData(file, farms, onDismiss, exportFormat)
                    } catch (e: IOException) {
                        // Handle file writing errors here
                        Toast.makeText(
                            context,
                            R.string.error_export_msg,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                    val fileURI: Uri = context.let {
                        FileProvider.getUriForFile(
                            it,
                            context.applicationContext.packageName.toString() + ".provider",
                            file
                        )
                    }
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = mimeType
                        putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
                        putExtra(Intent.EXTRA_STREAM, fileURI)
                        putExtra(Intent.EXTRA_TITLE, "farms${Instant.now().millis}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    createDocumentLauncher.launch(intent)
                }
            ) {
                Text(stringResource(id = R.string.export))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
*/


@Composable
fun DeleteAllDialogPresenter(
    showDeleteDialog: MutableState<Boolean>,
    onProceedFn: () -> Unit
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
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListHeader(
    title: String,
    onAddFarmClicked: () -> Unit,
    onBackClicked: () -> Unit,
    showAdd: Boolean
) {
    TopAppBar(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            if (showAdd) {
                IconButton(onClick = onAddFarmClicked) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListHeaderPlots(
    title: String,
    onAddFarmClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onExportClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onImportClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    showAdd: Boolean,
    showExport: Boolean,
    showShare: Boolean,
    showSearch: Boolean,
) {
    val context = LocalContext.current as Activity

    // State for holding the search query
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text=title,fontSize=18.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (showExport) {
                IconButton(onClick = onExportClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.save),
                        contentDescription = "Export"
                    )
                }
            }
            if (showShare) {
                IconButton(onClick = onShareClicked) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                }
            }
            IconButton(onClick = onImportClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.import_icon),
                    contentDescription = "Import"
                )
            }
            IconButton(onClick = {
                isSearchVisible = !isSearchVisible
            }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            if (showAdd) {
                IconButton(onClick = {
                    // Remove plot_size from shared preferences
                    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
                    if (sharedPref.contains("plot_size")) {
                        sharedPref.edit().remove("plot_size").apply()
                    }
                    // Call the onAddFarmClicked lambda
                    onAddFarmClicked()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    )

    // Conditional rendering of the search field
    if (isSearchVisible && showSearch) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChanged(it)
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmCard(farm: Farm, onCardClick: () -> Unit, onDeleteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth() // 90% of the screen width
                .padding(8.dp),
            onClick = {
                onCardClick()
            }
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = farm.farmerName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${stringResource(id = R.string.size)}: ${farm.size} ${
                            stringResource(
                                id = R.string.ha
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(0.9f)
                            .padding(bottom = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${stringResource(id = R.string.village)}: ${farm.village}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${stringResource(id = R.string.district)}: ${farm.district}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
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
private fun writeTextData(file: File, farms: List<Farm>, onDismiss: () -> Unit, format: String) {
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(file)

        fileOutputStream
            .write(""""Farmer Name", "Village", "District", "Size in Ha", "Cherry harvested this year in Kgs", "latitude", "longitude" , "createdAt", "updatedAt" """.toByteArray())
        fileOutputStream.write(10)
        farms.forEach {
            fileOutputStream.write(
                "${it.farmerName}, ${it.village},${it.district},${it.size},${it.purchases},${it.latitude},${it.longitude},${
                    Date(
                        it.createdAt
                    )
                }, \"${Date(it.updatedAt)}\"".toByteArray()
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
fun UpdateFarmForm(navController: NavController, farmId: Long?, listItems: List<Farm>) {
    val floatValue = 123.45f
    val item = listItems.find { it.id == farmId } ?: Farm(
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
        updatedAt = 1L
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
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }
    val showLocationDialogNew = remember { mutableStateOf(false) }
    val showPermissionRequest = remember { mutableStateOf(false) }
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("Ha", "Acres", "Sqm", "Timad", "Fichesa", "Manzana", "Tarea")
    var selectedUnit by remember { mutableStateOf(items[0]) }


    LaunchedEffect(Unit) {
        if (!isLocationEnabled(context)) {
            showLocationDialog.value = true
        }
    }

    // Define string constants
    val titleText = stringResource(id = R.string.enable_location_services)
    val messageText = stringResource(id = R.string.location_services_required_message)
    val enableButtonText = stringResource(id = R.string.enable)
    val cancelButtonText = stringResource(id = R.string.cancel)

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
            }

        )
    }




    if (navController.currentBackStackEntry!!.savedStateHandle.contains("coordinates")) {
        coordinates =
            navController.currentBackStackEntry!!.savedStateHandle.get<List<Pair<Double, Double>>>(
                "coordinates"
            )
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
//            item.coordinates =
//                (coordinates?.plus(coordinates?.first()) as List<Pair<Double, Double>>?)!!

            // fixing updating farms with size less than 4 ha

//            item.coordinates = if (!coordinates.isNullOrEmpty()) {
//                coordinates!!.plus(coordinates!!.first()) as List<Pair<Double, Double>>
//            } else {
//                // Default value or an appropriate handling mechanism
//                listOf(Pair(0.0, 0.0)) // Example default value
//            }

            // Ensure the coordinates have at least 4 points for size greater than 4 hectares
//            if ((size.toFloatOrNull() ?: 0f) >= 4) {
            if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) >= 4) {
                if ((coordinates?.size ?: 0) < 3) {
                    Toast.makeText(context, "Please capture at least 4 points for the polygon when the size is greater than 4 hectares.", Toast.LENGTH_SHORT).show()
                    return
                }
                item.coordinates = coordinates?.plus(coordinates?.first()) as List<Pair<Double, Double>>
            } else {
                item.coordinates = listOf(Pair( item.longitude.toDoubleOrNull()?:0.0,item.latitude.toDoubleOrNull()?:0.0)) // Example default value
            }

//            item.size = size.toFloat()
            item.size= convertSize(size.toDouble(), selectedUnit).toFloat()
            item.purchases = 0.toFloat()
            item.updatedAt = Instant.now().millis
            updateFarm(farmViewModel, item)
            val returnIntent = Intent()
            context.setResult(Activity.RESULT_OK, returnIntent)
            navController.navigate("farmList/${siteID}")
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
                TextButton(onClick =
                {
                    showDialog.value = false
                    navController.navigate("setPolygon")
                }) {
                    Text(text = stringResource(id = R.string.set_polygon))
                }
            }
        )
    }

    val scrollState = rememberScrollState()
    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()

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
            hasToShowDialog = showLocationDialogNew.value
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(state = scrollState)
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.update_farm),
            onAddFarmClicked = { /* Handle adding a farm here */ },
            onBackClicked = { navController.popBackStack() },
            showAdd = false
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester1.requestFocus() }
            ),
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text(stringResource(id = R.string.farm_name)) },
            isError = farmerName.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusRequester1.requestFocus()
                        true
                    }
                    false
                }
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester1.requestFocus() }
            ),
            value = memberId,
            onValueChange = { memberId = it },
            label = { Text(stringResource(id = R.string.member_id)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusRequester1.requestFocus()
                    }
                    false
                }
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester2.requestFocus() }
            ),
            value = village,
            onValueChange = { village = it },
            label = { Text(stringResource(id = R.string.village)) },
            modifier = Modifier
                .focusRequester(focusRequester1)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester3.requestFocus() }
            ),
            value = district,
            onValueChange = { district = it },
            label = { Text(stringResource(id = R.string.district)) },
            modifier = Modifier
                .focusRequester(focusRequester2)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

//        TextField(
//            singleLine = true,
//            value = size,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//            ),
//            onValueChange = { size = it },
//            label = { Text(stringResource(id = R.string.size_in_hectares)) },
//            isError = size.toFloatOrNull() == null || size.toFloat() <= 0, // Validate size
//            modifier = Modifier
//                .focusRequester(focusRequester3)
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                singleLine = true,
                value = size,
                onValueChange = {
                    size = it
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                ),

                label = { Text(stringResource(id = R.string.size_in_hectares) + " (*)") },
                isError = size.toFloatOrNull() == null || size.toFloat() <= 0, // Validate size
                colors = TextFieldDefaults.textFieldColors(
                    errorLeadingIconColor = Color.Red,
                ),
                modifier = Modifier
                    .focusRequester(focusRequester3)
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )

            //AreaInputField( farmViewModel = farmViewModel)
            Spacer(modifier = Modifier.width(16.dp))
            // Size measure
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    readOnly = true,
                    value = selectedUnit,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.unit)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    items.forEach { selectionOption ->
                        DropdownMenuItem(
                            { Text(text = selectionOption) },
                            onClick = {
                                selectedUnit = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
        if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) < 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    readOnly = true,
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(id = R.string.latitude)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
                TextField(
                    readOnly = true,
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(id = R.string.longitude)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
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
//                        if (size.toFloatOrNull() != null && size.toFloat() < 4) {
                        if (size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }?.let { it < 4f } == true) {
                        // Simulate collecting latitude and longitude
                            if (context.hasLocationPermission()) {
                                val locationRequest = LocationRequest.create().apply {
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
                                    Looper.getMainLooper()
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

            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp)
                .height(50.dp),
            enabled = size.toFloatOrNull() != null
        ) {
            Text(
                //text = if (size.toFloatOrNull() != null && size.toFloat() < 4) stringResource(id = R.string.get_coordinates) else stringResource(
                text = if (size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }?.let { it < 4f } == true) stringResource(id = R.string.get_coordinates) else stringResource(
                    id = R.string.set_new_polygon
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.update_farm))
        }
    }
}

fun updateFarm(
    farmViewModel: FarmViewModel,
    item: Farm
) {
    farmViewModel.updateFarm(item)
}
