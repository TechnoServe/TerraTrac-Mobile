package org.technoserve.farmcollector.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import org.technoserve.farmcollector.database.Farm
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.util.Date
import java.util.Objects

//data class Farm(val farmerName: String, val village: String, val district: String)
var siteID =0L
@Composable
fun FarmList(navController: NavController,siteId:Long) {
    siteID=siteId
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val showDeleteDialog = remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var selectedFarm: Farm? by remember { mutableStateOf(null) }
//    val farmList = listOf(
//        Farm(farmerName = "Farm A", village = "Village 1", district = "District X"),
//        Farm(farmerName = "Farm B", village = "Village 2", district = "District Y"),
//        Farm(farmerName = "Farm C", village = "Village 3", district = "District Z"),
//        Farm(farmerName = "Farm D", village = "Village 4", district = "District X"),
//        Farm(farmerName = "Farm E", village = "Village 5", district = "District Y"),
//        Farm(farmerName = "Farm F", village = "Village 6", district = "District Z"),
//        Farm(farmerName = "Farm G", village = "Village 7", district = "District X"),
//        Farm(farmerName = "Farm H", village = "Village 8", district = "District Y"),
//        Farm(farmerName = "Farm I", village = "Village 9", district = "District Z"),
//    )
    val listItems by farmViewModel.readAllData(siteId).observeAsState(listOf())

    fun onDelete(){
        val toDelete = mutableListOf<Long>()
        toDelete.addAll(selectedIds)
        farmViewModel.deleteList(toDelete)
        selectedIds.removeAll(selectedIds)
        showDeleteDialog.value = false
    }


    fun refreshListItems() {
        // TODO: update saved predictions list when db gets updated
        //  currently using a terrible makeshift solution
        navController.navigate("home")
        navController.navigate("farmList") {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    if(listItems.size>0){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                FarmListHeader(
                    title = stringResource(id = R.string.farm_list),
                    onAddFarmClicked = { navController.navigate("addFarm/${siteId}") },
                   // onBackClicked = { navController.navigateUp() }, siteList
                    onBackClicked = { navController.navigate("siteList") },
                    showAdd = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DownloadCsvButton(listItems)
            }
            items(listItems) { farm ->
                FarmCard(farm = farm, onCardClick = {
                    // When a FarmCard is clicked, show the dialog
                    selectedFarm = farm
                    isDialogVisible = true
                }, onDeleteClick = {
                    // When the delete icon is clicked, invoke the onDelete function
                    selectedIds.add(farm.id)
                    showDeleteDialog.value = true
                })
                Spacer(modifier = Modifier.height(16.dp))
            }


        }
        if (showDeleteDialog.value) {
            DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
        }
        if (isDialogVisible) {
            FarmDialog(
                navController = navController,
                farm = selectedFarm,
                onDismiss = {
                    // Dismiss the dialog when needed
                    isDialogVisible = false
                    selectedFarm = null
                }
            )
        }
    }else{


        Column(modifier = Modifier
            .fillMaxSize() ){
            FarmListHeader(
                title = stringResource(id = R.string.farm_list),
                onAddFarmClicked = { navController.navigate("addFarm/${siteId}") },
                onBackClicked = { navController.navigateUp() },
                showAdd = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp, 8.dp)

                ,
                painter = painterResource(id = R.drawable.no_data2),
                contentDescription = null
            )
        }
    }

}

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
fun FarmListHeader(title: String, onAddFarmClicked: () -> Unit, onBackClicked: () -> Unit, showAdd: Boolean) {
    TopAppBar(
//        elevation = 4.dp,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(start = 16.dp, end = 16.dp)
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
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            if(showAdd){
                IconButton(onClick = onAddFarmClicked) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    )
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
                        text = "${stringResource(id = R.string.size)}: ${ farm.size } ${stringResource(
                            id = R.string.ha
                        )}",
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




@Composable
fun FarmDialog(navController:NavController, farm: Farm?, onDismiss: () -> Unit) {
    if (farm != null) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = farm.farmerName) },
            dismissButton = {
                Button(
                    onClick = { onDismiss() },
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = Color.Blue,
//                        contentColor = Color.White
//                    )
                ) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                              navController.navigate("updateFarm/${farm.id}")
                              },

                ) {
                    Text(text = stringResource(id = R.string.update))
                }
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp)
//                    ) {
//                        val imgFile = File(farm.farmerPhoto)
//
//                        // on below line we are checking if the image file exist or not.
//                        var imgBitmap: Bitmap? = null
//                        if (imgFile.exists()) {
//                            // on below line we are creating an image bitmap variable
//                            // and adding a bitmap to it from image file.
//                            imgBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
//                        }
//                        Image(
//                            painter = rememberAsyncImagePainter(farm.farmerPhoto),
//                            contentDescription = stringResource(id = R.string.farmer_photo),
//                            contentScale = ContentScale.Fit,
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(start = 10.dp, end = 10.dp)
//                        )
//                    }
                    Text(text = "${stringResource(id = R.string.village)}: ${farm.village}", modifier = Modifier.padding(top=10.dp))
                    Text(text = "${stringResource(id = R.string.district)}: ${farm.district}")
                    Text(text = "${stringResource(id = R.string.latitude)}: ${farm.latitude}")
                    Text(text = "${stringResource(id = R.string.longitude)}: ${farm.longitude}")
                    Text(text = "${stringResource(id = R.string.size)}: ${farm.size} ${stringResource(id = R.string.ha)}")
//                    Text(text = "${stringResource(id = R.string.harvested_this_year)}: ${farm.purchases} ${stringResource(id = R.string.kgs)}")
                    // Add more fields as needed
                }
            }
        )
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

@Composable
fun DownloadCsvButton(farms: List<Farm>) {
    val context = LocalContext.current
    val activity = context as Activity
     val createDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // Handle the result as needed
            if (data != null) {
                val uri = data.data
                uri?.let {
                    val contentResolver = context.contentResolver
                    val outputStream: OutputStream? = contentResolver.openOutputStream(uri)

                    if (outputStream != null) {
                        PrintWriter(outputStream.bufferedWriter()).use { writer ->
                            // Write the header row
                            writer.println("Farmer Name, Village, District, Size in Ha, Cherry harvested this year in Kgs, latitude, longitude , createdAt, updatedAt ")

                            // Write each farm's data
                            for (farm in farms) {
                                val line = "${farm.farmerName}, ${farm.village},${farm.district},${farm.size},${farm.purchases},${farm.latitude},${farm.longitude},${Date(farm.createdAt)}, ${Date(farm.updatedAt)}"
                                writer.println(line)
                            }
                        }
                    }
                }
            }
        }
    }

    Button(
//        onClick = {
//
//            // Requesting Permission to access External Storage
//            // Requesting Permission to access External Storage
//            ActivityCompat.requestPermissions(
//                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE), 23
//            )
//
//            // getExternalStoragePublicDirectory() represents root of external storage, we are using DOWNLOADS
//            // We can use following directories: MUSIC, PODCASTS, ALARMS, RINGTONES, NOTIFICATIONS, PICTURES, MOVIES
//            val folder: File =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//
//            // Storing the data in file with name as geeksData.txt
//            val file = File(folder, "farms${Instant.now().millis}.csv")
//            writeTextData(file, farms, context)
//            // displaying a toast message
//            Toast.makeText(context, "Data saved ...", Toast.LENGTH_SHORT).show()
//
//        },
        onClick = {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23
            )
            // Create a CSV file and write data to it
            val filename = "farms.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)

            try {
                writeTextData(file, farms, context)
                // File was successfully written
            } catch (e: IOException) {
                // Handle file writing errors here
                Log.d("saving",e.message.toString())
            }
            // Create an intent to send the file

            val csvURI: Uri = context.let {
                    FileProvider.getUriForFile(
                        it,
                        context.applicationContext.packageName.toString() + ".provider",
                        file
                    )
            }
//            val content = getContentFromUri(csvURI, context)
//            Log.d("FileContent", "Content of $csvURI:\n$content")
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
            intent.putExtra(Intent.EXTRA_STREAM, csvURI)
            intent.putExtra(Intent.EXTRA_TITLE, "farms${Instant.now().millis}")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            createDocumentLauncher.launch(intent)

        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.download_csv))
    }
}

//fun getContentFromUri(uri: Uri, context: Context): String {
//    val inputStream = context.contentResolver.openInputStream(uri)
//    val reader = BufferedReader(InputStreamReader(inputStream))
//    val stringBuilder = StringBuilder()
//    var line: String?
//
//    try {
//        while (reader.readLine().also { line = it } != null) {
//            stringBuilder.append(line).append("\n")
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } finally {
//        inputStream?.close()
//    }
//
//    return stringBuilder.toString()
//}





// on below line creating a method to write data to txt file.
private fun writeTextData(file: File, farms: List<Farm>, context: Context) {
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(file)

        fileOutputStream
            .write(""""Farmer Name", "Village", "District", "Size in Ha", "Cherry harvested this year in Kgs", "latitude", "longitude" , "createdAt", "updatedAt" """.toByteArray())
        fileOutputStream.write(10);
        farms.forEach {
            fileOutputStream.write("${it.farmerName}, ${it.village},${it.district},${it.size},${it.purchases},${it.latitude},${it.longitude},${Date(it.createdAt)}, \"${Date(it.updatedAt)}\"".toByteArray())
            fileOutputStream.write(10);
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
fun UpdateFarmForm(navController:NavController,farmId:Long?,listItems:List<Farm>) {
    val floatValue: Float = 123.45f
    val item = listItems.find { it.id == farmId }?: Farm(
//        id = 0,
        siteId = 0L,
        farmerName = "Default Farmer",
        farmerPhoto = "Default photo",
        village = "Default Village",
        district = "Default District",
        latitude = "Default Village",
        longitude = "Default Village",
        size = floatValue,
        purchases = floatValue,
        createdAt = 1L,
        updatedAt = 1L

    )
    val context = LocalContext.current as Activity
    var isImageUploaded by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf(item.farmerName) }
    var farmerPhoto by remember { mutableStateOf(item.farmerPhoto) }
    var village by remember { mutableStateOf(item.village) }
    var district by remember { mutableStateOf(item.district) }
    var size by remember { mutableStateOf(item.size.toString()) }
    var purchases by remember { mutableStateOf(item.purchases.toString()) }
    var latitude by remember { mutableStateOf(item.latitude) }
    var longitude by remember { mutableStateOf(item.longitude) }
    val mylocation = remember { mutableStateOf("") }
    val currentPhotoPath = remember { mutableStateOf("") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )

    val permission_granted = stringResource(id = R.string.permission_granted)
    val permission_denied = stringResource(id = R.string.permission_denied)
    val fill_form = stringResource(id = R.string.fill_form)

    fun validateForm(): Boolean {
        var isValid = true

        if (farmerName.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (village.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (district.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (size.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

//        if (purchases.isBlank()) {
//            isValid = false
//            // You can display an error message for this field if needed
//        }

//        if (!isImageUploaded) {
//            isValid = false
//            // You can display an error message for this field if needed
//        }

        if (latitude.isBlank() || longitude.isBlank()) {
            isValid = false
            // You can display an error message for these fields if needed
        }

        return isValid
    }

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    val scrollState = rememberScrollState()
    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()
    var imageInputStream: InputStream? = null
    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val treeUri = result.data?.data

            if (treeUri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)

                // Now, you have permission to write to the selected directory
                val imageFileName = "image${Instant.now().millis}.jpg"

                val selectedDir = DocumentFile.fromTreeUri(context, treeUri)
                val imageFile = selectedDir?.createFile("image/jpeg", imageFileName)

                imageFile?.uri?.let { fileUri ->
                    try {
                        imageInputStream?.use { input ->
                            context.contentResolver.openOutputStream(fileUri)?.use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Update the database with the file path
                        farmerPhoto = fileUri.toString()
                        Log.d("farmerphoto", "$farmerPhoto")
                        // Update other fields in the Farm object
                        // Then, insert or update the farm object in your database
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { it ->
        uri?.let { it1 ->
            imageInputStream = context.contentResolver.openInputStream(it1)

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
        }
    }





    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it)
        {
            Toast.makeText(context, permission_granted, Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        }
        else
        {
            Toast.makeText(context, permission_denied, Toast.LENGTH_SHORT).show()
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp).onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
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

        TextField(
            singleLine = true,
            value = size,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
            ),
            onValueChange = { size = it },
            label = { Text(stringResource(id = R.string.size_in_hectares)) },
            modifier = Modifier
                .focusRequester(focusRequester3)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
//        TextField(
//            value = purchases,
//            onValueChange = { purchases = it },
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//            ),
//            label = { Text(stringResource(id = R.string.harvested_this_year_in_kgs)) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
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
        Button(
            onClick = {
                // Simulate collecting latitude and longitude

                if (context.hasLocationPermission()) {
                    val locationRequest = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 10000 // Update interval in milliseconds
                        fastestInterval = 5000 // Fastest update interval in milliseconds
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

            },

            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp)
                .height(50.dp),
        ) {
            Text(text = stringResource(id = R.string.get_coordinates))
        }

//        if (!farmerPhoto.isBlank())
//        {
//            val imgFile = File(farmerPhoto)
//
//            // on below line we are checking if the image file exist or not.
//            var imgBitmap: Bitmap? = null
//            if (imgFile.exists()) {
//                // on below line we are creating an image bitmap variable
//                // and adding a bitmap to it from image file.
//                imgBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
//            }
//            Image(
//                modifier = Modifier
//                    .size(width = 200.dp, height = 150.dp)
//                    .padding(16.dp, 8.dp)
//                    .align(Alignment.CenterHorizontally)
//                ,
//                painter = rememberAsyncImagePainter(farmerPhoto),
//                contentDescription = null
//            )
//        }
//        else
//        {
//            Image(
//                modifier = Modifier
//                    .size(width = 200.dp, height = 150.dp)
//                    .padding(16.dp, 8.dp)
//                    .align(Alignment.CenterHorizontally)
//                ,
//                painter = painterResource(id = R.drawable.image_placeholder),
//                contentDescription = null
//            )
//        }

//        Button(
//            onClick = {
//                val permissionCheckResult =
//                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
//
//                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
//                {
//                    cameraLauncher.launch(uri)
//                    isImageUploaded = true
//                }
//                else
//                {
//                    permissionLauncher.launch(Manifest.permission.CAMERA)
//                }
//            }
//        ){
//            Text(text = stringResource(id = R.string.take_picture))
//        }
        Button(
            onClick = {
                val isValid = validateForm()
                if(isValid){
                    item.farmerPhoto = ""
                    item.farmerName = farmerName
                    item.latitude = latitude
                    item.village = village
                    item.district = district
                    item.longitude = longitude
                    item.size = size.toFloat()
                    item.purchases = 0.toFloat()
                    item.updatedAt = Instant.now().millis
                    updateFarm(farmViewModel,item)
                    val returnIntent = Intent()
                    context.setResult(Activity.RESULT_OK, returnIntent)
//                    context.finish()
                    navController.navigate("farmList/${siteID}")

                }
                else {
                    Toast.makeText(context, fill_form, Toast.LENGTH_SHORT).show()
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