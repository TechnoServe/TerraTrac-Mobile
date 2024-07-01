package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import dagger.hilt.android.lifecycle.HiltViewModel
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.map.getCenterOfPolygon
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import java.util.UUID
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
    var isValid by remember { mutableStateOf(true) }
    var farmerName by rememberSaveable { mutableStateOf("") }
    var memberId by rememberSaveable { mutableStateOf("") }
    var farmerPhoto by rememberSaveable { mutableStateOf("") }
    var village by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }

    // var size by rememberSaveable { mutableStateOf("") }
    //var size by remember { mutableStateOf("") }
    // var area by remember { mutableStateOf(0.0) }

    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    val items = listOf("Ha", "Acres", "Sqm", "Timad", "Fichesa", "Manzana", "Tarea")
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(items[0]) }
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )

    val mapViewModel: MapViewModel = viewModel()
    var size by rememberSaveable { mutableStateOf("") }
//    var size by mapViewModel.size.collectAsState()
    //var textFieldValue by remember { mutableStateOf(TextFieldValue(size.toString())) }

    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )
    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }

    if (showLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { showLocationDialog.value = false },
            title = { Text(stringResource(id = R.string.enable_location)) },
            text = { Text(stringResource(id = R.string.enable_location_msg)) },
            confirmButton = {
                Button(onClick = {
                    showLocationDialog.value = false
                    promptEnableLocation(context)
                }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showLocationDialog.value = false }) {
                    Text(stringResource(id = R.string.no))
                }
            }
        )
    }

    fun saveFarm() {
        // convert selectedUnit to hectares
        //val sizeInHa = convertSize(size.toDouble(), selectedUnit)

//        // Save the Calculate Area if the entered Size is greater than 4 otherwise keep the entered size Value
//        val sizeInHa = if ((size.toFloatOrNull() ?: 0f) < 4f) {
//            convertSize(size.toDouble(), selectedUnit)
//        } else {
//            mapViewModel.calculateArea(coordinatesData)?:0.0f
//        }
        val sizeInHa= mapViewModel.saveSize(selectedUnit, coordinatesData)

        //save unit in sharedPreference
        with(sharedPref.edit()) {
            putString("unit", selectedUnit)
            apply()
        }

        // Add farm
        // Generating a UUID for a new farm before saving it
        val newUUID = UUID.randomUUID()

        addFarm(
            farmViewModel,
            siteId,
            remote_id = newUUID,
            farmerPhoto,
            farmerName,
            memberId,
            village,
            district,
            0.toFloat(),
            sizeInHa.toFloat(),
            latitude,
            longitude,
            coordinates = coordinatesData?.plus(coordinatesData.first())
        )

        val returnIntent = Intent()
        context.setResult(Activity.RESULT_OK, returnIntent)
//                    context.finish()
        navController.navigate("farmList/${siteId}")
    }


    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 32.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.add_farm)) },
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

        if (selectedUnit.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (latitude.isBlank() || longitude.isBlank()) {
            isValid = false
            // You can display an error message for these fields if needed
        }

        return isValid
    }

    val scrollState = rememberScrollState()
    val permissionGranted = stringResource(id = R.string.permission_granted)
    val permissionDenied = stringResource(id = R.string.permission_denied)
    val fillForm = stringResource(id = R.string.fill_form)

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
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
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
            label = { Text(stringResource(id = R.string.farm_name) + " (*)") },
            supportingText = { if (!isValid && farmerName.isBlank()) Text("Farmer Name should not be empty") },
            isError = !isValid && farmerName.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
            ),
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
            label = { Text(stringResource(id = R.string.village) + " (*)") },
            supportingText = { if (!isValid && village.isBlank()) Text("Village should not be empty") },
            isError = !isValid && village.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
            ),
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
            label = { Text(stringResource(id = R.string.district) + " (*)") },
            supportingText = { if (!isValid && district.isBlank()) Text("District should not be empty") },
            isError = !isValid && district.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
            ),
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
                onValueChange = {
                    size = it
                    val newSize = it.toDoubleOrNull() ?: 0.0
                    mapViewModel.updateSize(newSize.toString())
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                ),

                label = { Text(stringResource(id = R.string.size_in_hectares) + " (*)") },
                supportingText = { if (!isValid && size.isBlank()) Text("Farm Size should not be empty") },
                isError = !isValid && size.isBlank(),
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
        if ((size.toFloatOrNull() ?: 0f) < 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextField(
                    readOnly = true,
                    value = latitude,
                    onValueChange = {
                        if (it.split(".").last().length >= 6) latitude = it
                        else Toast.makeText(
                            context,
                            "Latitude must have at least 6 decimal places",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    label = { Text(stringResource(id = R.string.latitude) + " (*)") },
                    supportingText = {
                        if (!isValid && latitude.split(".").last().length < 6) Text(
                            "Latitude must have at least 6 decimal places"
                        )
                    },
                    isError = !isValid && latitude.split(".").last().length < 6,
                    colors = TextFieldDefaults.textFieldColors(
                        errorLeadingIconColor = Color.Red,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
                TextField(
                    readOnly = true,
                    value = longitude,
                    onValueChange = {
                        if (it.split(".").last().length >= 6) longitude = it
                        else Toast.makeText(
                            context,
                            "Longitude must have at least 6 decimal places",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    label = { Text(stringResource(id = R.string.longitude) + " (*)") },
                    supportingText = {
                        if (!isValid && longitude.split(".").last().length < 6) Text(
                            "Longitude must have at least 6 decimal places"
                        )
                    },
                    isError = !isValid && longitude.split(".").last().length < 6,
                    colors = TextFieldDefaults.textFieldColors(
                        errorLeadingIconColor = Color.Red,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp)
                )
            }
        }
        Button(
            onClick = {
                // Simulate collecting latitude and longitude
                if (context.hasLocationPermission() && ((size.toFloatOrNull() ?: 0f) < 4f)) {
                    if (isLocationEnabled(context)) {
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
                                    }
                                }
                            },
                            Looper.getMainLooper()
                        )
                    } else {
                        showLocationDialog.value = true
                    }
                } else {
                    navController.currentBackStackEntry?.arguments?.putParcelable("farmData", null)
                    navController.navigate("setPolygon")
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp)
                .height(50.dp),
        ) {
            val enteredSize = size.toFloatOrNull() ?: 0f
            Text(
                text = if (enteredSize >= 4f) {
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
                    val center = coordinatesData.toLatLngList().getCenterOfPolygon()
                    val bounds: LatLngBounds = center
                    longitude = bounds.northeast.longitude.toString()
                    latitude = bounds.southwest.latitude.toString()
                    //Show the overview of polygon taken
                }
                if (validateForm()) {
                    // Ask user to confirm before adding farm
                    if (coordinatesData?.isNotEmpty() == true) saveFarm()
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
    remote_id: UUID,
    farmerPhoto: String,
    farmerName: String,
    memberId: String,
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
        remote_id,
        farmerPhoto,
        farmerName,
        memberId,
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

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun promptEnableLocation(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}

@SuppressLint("SimpleDateFormat")
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
    private val farmerName = mutableStateOf("")
    val memberId = mutableStateOf("")
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


