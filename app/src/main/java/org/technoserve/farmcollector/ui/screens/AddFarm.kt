package org.technoserve.farmcollector.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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
import org.technoserve.farmcollector.utils.convertSize
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject



@Composable
fun AddFarm(navController: NavController, siteId: Long) {
    var coordinatesData: List<Pair<Double, Double>>? = null
    if (navController.currentBackStackEntry!!.savedStateHandle.contains("coordinates")) {
        val parcelableCoordinates = navController.currentBackStackEntry!!
            .savedStateHandle
            .get<List<ParcelablePair>>("coordinates")

        coordinatesData = parcelableCoordinates?.map { Pair(it.first, it.second) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
//            .padding(16.dp)
    ) {
            FarmListHeader(
                title = stringResource(id = R.string.add_farm),
                onSearchQueryChanged = {},
                onAddFarmClicked = { /* Handle adding a farm here */ },
                onBackSearchClicked = {},
                onBackClicked = { navController.popBackStack() },
                showAdd = false,
                showSearch = false,
                showRestore = false,
                onRestoreClicked = {}
            )
        Spacer(modifier = Modifier.height(16.dp))
        FarmForm(navController, siteId, coordinatesData)
    }
}
// Helper function to truncate a string representation of a number to a specific number of decimal places
fun truncateToDecimalPlaces(value: String, decimalPlaces: Int): String {
    val dotIndex = value.indexOf('.')
    return if (dotIndex == -1 || dotIndex + decimalPlaces + 1 > value.length) {
        // If there's no decimal point or the length is already less than required, return the original value
        value
    } else {
        // Truncate the value after the specified number of decimal places
        value.substring(0, dotIndex + decimalPlaces + 1)
    }
}

// Function to read and format stored value
fun readStoredValue(sharedPref: SharedPreferences): String {
    val storedValue = sharedPref.getString("plot_size", "") ?: ""

    // Truncate the value to 4 decimal places without rounding
    val formattedValue = truncateToDecimalPlaces(storedValue, 9)

    return formattedValue
}

// Function to format input value to 6 decimal places without scientific notation
fun formatInput(input: String): String {
    return try {
        val number = BigDecimal(input)
        val scale = number.scale()
        val decimalPlaces = scale - number.precision()

        when {
           decimalPlaces > 3 -> {
                // Format to 6 decimal places without trailing zeros if more than 3 decimal places
               BigDecimal(input).setScale(9, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
            }
            decimalPlaces == 0 -> {
                // No decimal part, return the number as is
                input
            }
            else -> {
                // Set the precision to 6 decimal places without rounding
                val formattedNumber = number.setScale(9, RoundingMode.DOWN)
                // If 3 or fewer decimal places, return as is without trailing zeros
                formattedNumber.stripTrailingZeros().toPlainString()
            }
        }
    } catch (e: NumberFormatException) {
        input // Return an empty string if the input is invalid
    }
}
fun validateSize(size: String): Boolean {
    // Check if the input matches the allowed pattern: digits and at most one dot
    val regex = Regex("^[0-9]*\\.?[0-9]*$")
    return size.matches(regex) && size.toFloatOrNull() != null && size.toFloat() > 0 && size.isNotBlank()
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

    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    val items = listOf("Ha", "Acres", "Sqm", "Timad", "Fichesa", "Manzana", "Tarea")
    var expanded by remember { mutableStateOf(false) }
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val mapViewModel: MapViewModel = viewModel()
    // Read initial value from SharedPreferences
    var size by rememberSaveable { mutableStateOf(readStoredValue(sharedPref)) }
    var selectedUnit by rememberSaveable { mutableStateOf(sharedPref.getString("selectedUnit", items[0]) ?: items[0]) }
    var isValidSize by remember { mutableStateOf(true) }
    var isFormSubmitted by remember { mutableStateOf(false) }
    // Regex pattern to check for scientific notation
    val scientificNotationPattern = Pattern.compile("([+-]?\\d*\\.?\\d+)[eE][+-]?\\d+")
    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }
    val showLocationDialogNew = remember { mutableStateOf(false) }

    // Function to update the selected unit
    fun updateSelectedUnit(newUnit: String) {
        selectedUnit = newUnit
        sharedPref.edit().putString("selectedUnit", newUnit).apply()
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Access location services
            } else {
                // Handle the denied permission
                Toast.makeText(
                    context,
                    "Location permission is required to access this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun fetchLocationAndNavigate() {
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

                        // Navigate to 'setPolygon' if conditions are met
                        navController.currentBackStackEntry?.arguments?.putParcelable(
                            "farmData",
                            null
                        )
                        navController.navigate("setPolygon")
                        mapViewModel.clearCoordinates()
                    }
                }
            },
            Looper.getMainLooper()
        )
    }


    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Update the value from SharedPreferences when the screen is resumed
                size = sharedPref.getString("plot_size", "") ?: ""
                selectedUnit = sharedPref.getString("selectedUnit", "Ha") ?: "Ha"
//                delete plot_size from sharedPreference
                with(sharedPref.edit()) {
                    remove("plot_size")
                    remove("selectedUnit")
                    apply()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                Button(onClick = {
                    showLocationDialog.value = false
                    Toast.makeText(
                        context,
                        R.string.location_permission_denied_message,
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text(stringResource(id = R.string.no))
                }
            },
            containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
            tonalElevation = 6.dp // Adds a subtle shadow for better UX
        )
    }

    fun saveFarm() {
        // Validate size input if the size is empty we use the default size 0
        if (size.isEmpty()) {
            size = "0.0"
        }

        // convert selectedUnit to hectares
        val sizeInHa = convertSize(size.toDouble(), selectedUnit)
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
            },
            containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
            tonalElevation = 6.dp // Adds a subtle shadow for better UX
        )
    }

    fun validateForm(): Boolean {
        isValid = true
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

        if (size.isBlank() || size.toFloatOrNull() == null || size.toFloat() <= 0) {
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

    val showPermissionRequest = remember { mutableStateOf(false) }

    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val buttonColor = if (isDarkTheme) Color.Black else Color.White
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
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
            label = { Text(stringResource(id = R.string.farm_name) + " (*)",color = inputLabelColor)},
            supportingText = { if (!isValid && farmerName.isBlank()) Text(stringResource(R.string.error_farmer_name_empty) + " (*)") },
            isError = !isValid && farmerName.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
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
            label = { Text(stringResource(id = R.string.member_id),color = inputLabelColor) },
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
            label = { Text(stringResource(id = R.string.village) + " (*)",color = inputLabelColor) },
            supportingText = { if (!isValid && village.isBlank()) Text(stringResource(R.string.error_village_empty)) },
            isError = !isValid && village.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
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
            label = { Text(stringResource(id = R.string.district) + " (*)", color =inputLabelColor) },
            supportingText = { if (!isValid && district.isBlank()) Text(stringResource(R.string.error_district_empty)) },
            isError = !isValid && district.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
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
                value = truncateToDecimalPlaces(size,9),
                onValueChange = { inputValue ->
                    val formattedValue = when {
                        validateSize(inputValue) -> inputValue
                        // Check if the input is in scientific notation
                        scientificNotationPattern.matcher(inputValue).matches() -> {
                            truncateToDecimalPlaces(formatInput(inputValue),9)
                        }
                        else -> inputValue
                    }

                    // Update the size state with the formatted value
                    size = formattedValue
                    isValidSize = validateSize(formattedValue)

                    // Save to SharedPreferences or perform other actions
                    with(sharedPref.edit()) {
                        putString("plot_size", formattedValue)
                        apply()
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                label = {
                    Text(
                        text = stringResource(id = R.string.size_in_hectares) + " (*)",
                        color = inputLabelColor
                    )
                },
                supportingText = {
                    when {
                        isFormSubmitted && size.isBlank() -> {
                            Text(stringResource(R.string.error_farm_size_empty))
                        }
                        isFormSubmitted && !isValidSize -> {
                            Text(stringResource(R.string.error_farm_size_invalid))
                        }
                    }
                },
                isError = isFormSubmitted && (!isValidSize || size.isBlank()),
                colors = TextFieldDefaults.textFieldColors(
                    errorLeadingIconColor = Color.Red,
                    cursorColor = inputTextColor,
                    errorCursorColor = Color.Red,
                    focusedIndicatorColor = inputBorder,
                    unfocusedIndicatorColor = inputBorder,
                    errorIndicatorColor = Color.Red
                ),
                modifier = Modifier
                    .focusRequester(focusRequester3)
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            // Size measure
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
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
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(text = selectionOption) },
                            onClick = {
                                //selectedUnit = selectionOption
                                updateSelectedUnit(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
//        if ((size.toFloatOrNull() ?: 0f) < 4f) {
        if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) < 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextField(
                    readOnly = true,
                    value = latitude,
                    onValueChange = {
                        val parts = it.split(".")
                        if (parts.size == 2 && parts.last().length == 5 ) {
                            val decimalPlaces = parts.last().length
                            val requiredZeros = 6 - decimalPlaces
                            // Append the required number of zeros
                            val formattedLatitude = it.padEnd(it.length + requiredZeros, '0')
                            latitude = formattedLatitude
                        } else if (parts.size == 2 && parts.last().length >= 6) {
                            latitude = it
                        } else {
                            Toast.makeText(
                                context,
                                R.string.error_latitude_decimal_places,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    label = { Text(stringResource(id = R.string.latitude) + " (*)",color = inputLabelColor) },
                    supportingText = {
                        if (!isValid && latitude.split(".").last().length < 6) Text(
                            stringResource(R.string.error_latitude_decimal_places)
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
                        val parts = it.split(".")
                        if (parts.size == 2) {
                            val decimalPlaces = parts.last().length
                            val formattedLongitude = if (decimalPlaces == 5 ) {
                                // Append the required number of zeros to the decimal part
                                it.padEnd(it.length + (6 - decimalPlaces), '0')
                            } else {
                                it
                            }
                            longitude = formattedLongitude
                        } else {
                            Toast.makeText(
                                context,
                                R.string.error_longitude_decimal_places,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    label = { Text(stringResource(id = R.string.longitude) + " (*)",color = inputLabelColor) },
                    supportingText = {
                        if (!isValid && longitude.split(".").last().length < 6) Text(
                            stringResource(R.string.error_longitude_decimal_places) + ""
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

        // Button to trigger the location permission request
        Button(
            onClick = {
                val enteredSize = size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f
                if (isLocationEnabled(context) && context.hasLocationPermission()) {
                    if (enteredSize < 4f) {
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
                        navController.currentBackStackEntry?.arguments?.putParcelable(
                            "farmData",
                            null
                        )
                        navController.navigate("setPolygon")
                        mapViewModel.clearCoordinates()
                    }
                } else {
                    showPermissionRequest.value = true
                    showLocationDialog.value = true
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp)
                .height(50.dp),
            enabled = size.isNotBlank()
        ) {
            val enteredSize = size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f

            Text(
                text = if (enteredSize >= 4f) {
                    stringResource(id = R.string.set_polygon)
                } else {
                    stringResource(id = R.string.get_coordinates)
                }
            )
        }
        Button(
            onClick = {
                isFormSubmitted = true
                // Finding the center of the polygon captured
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
                .background(MaterialTheme.colorScheme.background)
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
    farmViewModel.addFarm(farm,siteId)
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest(
    onLocationEnabled: () -> Unit,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    showLocationDialogNew: MutableState<Boolean>,
    hasToShowDialog: Boolean
) {
    val context = LocalContext.current
    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (isLocationEnabled(context)) {
            if (multiplePermissionsState.allPermissionsGranted) {
                onPermissionsGranted()
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        } else {
            onLocationEnabled()
        }
    }

    // Optionally, show some text to inform the user about the importance of permissions
    if ((!multiplePermissionsState.allPermissionsGranted) && hasToShowDialog) {
        Column {
            AlertDialog(
                onDismissRequest = { showLocationDialogNew.value = false },
                title = { Text(stringResource(id = R.string.enable_location)) },
                text = { Text(stringResource(id = R.string.enable_location_msg)) },
                confirmButton = {
                    Button(onClick = {
                        // Perform action to enable location permissions
                        promptEnableLocation(context)
                        showLocationDialogNew.value = false  // Dismiss the dialog after action
                    }) {
                        Text(stringResource(id = R.string.yes))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        // Show a toast message indicating that the permission was denied
                        Toast.makeText(
                            context,
                            R.string.location_permission_denied_message,
                            Toast.LENGTH_SHORT
                        ).show()
                        showLocationDialogNew.value = false  // Dismiss the dialog after action
                    }) {
                        Text(stringResource(id = R.string.no))
                    }
                },
                containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
                tonalElevation = 6.dp // Adds a subtle shadow for better UX
            )
        }
    }
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


