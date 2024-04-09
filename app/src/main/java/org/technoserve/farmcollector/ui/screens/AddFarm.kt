package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.tns.lab.composegooglemaps.clusters.getCenterOfPolygon
import dagger.hilt.android.lifecycle.HiltViewModel
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import javax.inject.Inject

@Composable
fun AddFarm(navController: NavController, siteId: Long) {
    var coordinatesData: List<Pair<Double, Double>>? = null
    if (navController.currentBackStackEntry!!.savedStateHandle.contains("coordinates")) {
        coordinatesData =
            navController.currentBackStackEntry!!.savedStateHandle.get<List<Pair<Double, Double>>>(
                "coordinates"
            )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.add_farm),
            onAddFarmClicked = { /* Handle adding a farm here */ },
            onBackClicked = { navController.popBackStack() },
            showAdd = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        FarmForm(navController, siteId, coordinatesData)
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FarmForm(
    navController: NavController,
    siteId: Long,
    coordinatesData: List<Pair<Double, Double>>?

) {
    val context = LocalContext.current as Activity
    var isImageUploaded by remember { mutableStateOf(false) }
    var farmerName by rememberSaveable { mutableStateOf("") }
    var farmerPhoto by rememberSaveable { mutableStateOf("") }
    var village by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableStateOf("") }
    var purchases by remember { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    val mylocation = remember { mutableStateOf("") }
    val currentPhotoPath = remember { mutableStateOf("") }
    val items = listOf("Ha", "Acres", "Sqm")
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(items[0]) }
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )
    val showDialog = remember { mutableStateOf(false) }

    fun convertSize(size: Double, selectedUnit: String): Double {
        return when (selectedUnit) {
            "ha" -> size // If already in hectares, return as is
            "Acres" -> size * 0.404686 // Convert Acres to hectares
            "sqm" -> size * 0.0001 // Convert square meters to hectares
            else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
        }
    }

    fun saveFarm()
    {
//        convert selectedUnit to hectares
        val sizeInHa = convertSize(size.toDouble(), selectedUnit)

//        save unit in sharedPreference
        with(sharedPref.edit()) {
            putString("unit", selectedUnit)
            apply()
        }

        // Add farm
        addFarm(
            farmViewModel,
            siteId,
            "",
            farmerName,
            village,
            district,
            0.toFloat(),
            sizeInHa.toFloat(),
            latitude,
            longitude,
            coordinatesData
        )

        val returnIntent = Intent()
        context.setResult(Activity.RESULT_OK, returnIntent)
//                    context.finish()
        navController.navigate("farmList/${siteId}")
    }


    if(showDialog.value)
    {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Add Farm") },
            text = {
                Column {
                    Text(text = stringResource(id = R.string.confirm_add_farm))
                }
            },

            confirmButton = {
                TextButton(onClick = {
                    saveFarm()
                }) {
                    Text(text = stringResource(id = R.string.add_farm))
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

        if (size.isBlank() || size.toFloatOrNull() == null) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if(selectedUnit.isBlank()) {
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
    val permissionGranted = stringResource(id = R.string.permission_granted)
    val permissionDenied = stringResource(id = R.string.permission_denied)
    val fillForm = stringResource(id = R.string.fill_form)
//    val cameraLauncher =
//        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){
//            capturedImageUri = uri
//            farmerPhoto.value = BitmapFactory.decodeStream(
//                uri?.let { it1 -> context.contentResolver?.openInputStream(it1) }
//            )
//            val stream = ByteArrayOutputStream()
//            farmerPhoto.value?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val imgAsByteArray: ByteArray = stream.toByteArray()
//            farmerPhoto.value = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.size)
//        }

    var imageInputStream: InputStream? = null
    val resultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val treeUri = result.data?.data

                if (treeUri != null) {
                    val takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
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

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { it ->
            uri?.let { it1 ->
                imageInputStream = context.contentResolver.openInputStream(it1)

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                resultLauncher.launch(intent)
            }
        }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, permissionGranted, Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, permissionDenied, Toast.LENGTH_SHORT).show()
        }
    }

    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()
    Column(
        modifier = Modifier

            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(state = scrollState)
    ) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                singleLine = true,
                value = size,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                ),
                onValueChange = {
                    size = it
                },
                label = { Text(stringResource(id = R.string.size_in_hectares)) },
                modifier = Modifier
                    .focusRequester(focusRequester3)
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )
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

//        TextField(
//            value = purchases,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//            ),
//            onValueChange = { purchases = it },
//            label = { Text(stringResource(id = R.string.harvested_this_year_in_kgs)) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
        if ((size.toFloatOrNull() ?: 0f) <= 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                // Simulate collecting latitude and longitude

                if (context.hasLocationPermission() && ((size.toFloatOrNull() ?: 0f) <= 4f)) {
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
                                    //Log.d("FARM_LOCATION", "loaded success,,,,,,,")
                                }
                            }
                        },
                        Looper.getMainLooper()
                    )
                } else {
                    navController.navigate("setPolygon")
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp)
                .height(50.dp),
        ) {
            Text(
                text = if ((size.toFloatOrNull() ?: 0f) > 4f) {
                    stringResource(id = R.string.set_polygon)
                } else {
                    stringResource(id = R.string.get_coordinates)
                }
            )
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
//                    ,
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
//
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
//                Finding the center of the polygon captured
                if (coordinatesData?.isNotEmpty() == true && latitude.isBlank() && longitude.isBlank()) {
                    var center = coordinatesData.toLatLngList().getCenterOfPolygon()
                    var bounds: LatLngBounds = center
                    longitude = bounds.northeast.longitude.toString()
                    latitude = bounds.southwest.latitude.toString()
                    //Show the overview of polygon taken

                }
                val isValid = validateForm()
                if (isValid) {
                    // Ask user to confirm before addding farm
                    if(coordinatesData?.isNotEmpty() == true) saveFarm()
                    else showDialog.value = true
                } else {
                    Toast.makeText(context, fillForm, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.add_farm))
        }
    }
}

fun addFarm(
    farmViewModel: FarmViewModel,
    siteId: Long,
    farmerPhoto: String,
    farmerName: String,
    village: String,
    district: String,
    purchases: Float,
    size: Float,
    latitude: String,
    longitude: String,
    coordinates: List<Pair<Double, Double>>?
): Farm {
    val farm = Farm(
        siteId,
        farmerPhoto,
        farmerName,
        village,
        district,
        purchases,
        size,
        latitude,
        longitude,
        coordinates,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis
    )
    farmViewModel.addFarm(farm)
    return farm
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}

fun createDefaultBitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}

@HiltViewModel
class FarmFormViewModel @Inject constructor() : ViewModel() {
    val farmerName = mutableStateOf("")
    val village = mutableStateOf("")
    val district = mutableStateOf("")
    val size = mutableStateOf("")
    val latitude = mutableStateOf("")
    val longitude = mutableStateOf("")

    fun setFarmerName(name: String) {
        farmerName.value = name
    }

    fun setVillage(villageName: String) {
        village.value = villageName
    }

    fun setDistrict(districtName: String) {
        district.value = districtName
    }

    fun setSize(sizeValue: String) {
        size.value = sizeValue
    }

    fun setLatitude(latitudeValue: String) {
        latitude.value = latitudeValue
    }

    fun setLongitude(longitudeValue: String) {
        longitude.value = longitudeValue
    }
}

fun List<Pair<Double, Double>>.toLatLngList(): List<LatLng> {
    return map { LatLng(it.first, it.second) }
}


