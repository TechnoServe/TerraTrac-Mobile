package org.technoserve.farmcollector.ui.screens.farms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.database.models.ParcelablePair
import org.technoserve.farmcollector.utils.hasLocationPermission
import org.technoserve.farmcollector.database.helpers.map.LocationHelper
import org.technoserve.farmcollector.viewmodels.MapViewModel
import org.technoserve.farmcollector.ui.components.FarmListHeader
import org.technoserve.farmcollector.ui.components.KeepPolygonDialog
import org.technoserve.farmcollector.utils.convertSize
import org.technoserve.farmcollector.utils.isSystemInDarkTheme
import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory
import java.util.regex.Pattern


private const val KEY_HAS_NEW_POLYGON = "has_new_polygon"
/**
 * This is the update farm form screen.
 * It displays a form with fields for editing farm details.
 * It also includes a button to save the updated farm details.
 * If the user clicks the save button, it validates the input and saves the updated farm details to the database.
 * It also includes a button to delete the farm from the database.
 * If the user clicks the delete button, it displays a confirmation dialog to confirm the deletion.
 */
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
            siteId = 0L,
            farmerName = "Default Farmer",
            memberId = "",
            farmerPhoto = "Default photo",
            village = "Default Village",
            district = "Default District",
            latitude = "Default Village",
            longitude = "Default Village",
            coordinates = null,
            accuracyArray = null,
            size = floatValue,
            purchases = floatValue,
            createdAt = 1L,
            updatedAt = 1L,
        )
    val context = LocalContext.current as Activity
    var farmerName by remember { mutableStateOf(item.farmerName) }
    var memberId by remember { mutableStateOf(item.memberId) }
    var village by remember { mutableStateOf(item.village) }
    var district by remember { mutableStateOf(item.district) }
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)
    var isValidSize by remember { mutableStateOf(true) }
    var size by remember {
        mutableStateOf(
            sharedPref.getString("plot_size", item.size.toString()) ?: item.size.toString()
        )
    }
    var latitude by remember { mutableStateOf(item.latitude) }
    var longitude by remember { mutableStateOf(item.longitude) }
    var coordinates by remember { mutableStateOf(item.coordinates) }
    var showKeepPolygonDialog by remember { mutableStateOf(false) }
    val farmViewModel: FarmViewModel =
        viewModel(
            factory = FarmViewModelFactory(context.applicationContext as Application),
        )

    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }
    val showLocationDialogNew = remember { mutableStateOf(false) }
    val showPermissionRequest = remember { mutableStateOf(false) }
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
                    Toast.makeText(
                        context,
                        R.string.location_permission_denied_message,
                        Toast.LENGTH_SHORT
                    ).show()
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
        val textWithNumbersRegex = Regex(".*[a-zA-Z]+.*") // Ensures there is at least one letter
        if (farmerName.isBlank() || !farmerName.matches(textWithNumbersRegex)) {
            isValid = false
        }

        if (village.isBlank() || !village.matches(textWithNumbersRegex)) {
            isValid = false
        }

        if (district.isBlank() || !district.matches(textWithNumbersRegex)) {
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

            // Updated condition handling
            if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }
                    ?: 0f) >= 4) {
                // Check if coordinates are valid for a polygon
                if ((coordinates?.size ?: 0) < 3) {
                    Toast.makeText(
                        context,
                        R.string.error_polygon_points,
                        Toast.LENGTH_SHORT,
                    ).show()
                    return
                }
                showKeepPolygonDialog = true

            } else {
                if ((coordinates?.size ?: 0) >= 3) {
                    // Size is less than 4 but valid polygon coordinates are present
                    // Show the dialog to ask whether to keep or capture new coordinates
                    showKeepPolygonDialog = true
                } else {
                    // Handle the case where size is less than the threshold and only one coordinate is present
                    item.coordinates = listOf(
                        Pair(
                            item.longitude.toDoubleOrNull() ?: 0.0,
                            item.latitude.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
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

    // If changes are detected, show dialog to confirm
    if (showKeepPolygonDialog) {
        KeepPolygonDialog(
            onDismiss = { showKeepPolygonDialog = false },
            onKeepExisting = {
                item.coordinates =
                    coordinates?.plus(coordinates?.first()) as List<Pair<Double, Double>>
                updateFarmInstance()
                showKeepPolygonDialog = false
            },
            onCaptureNew = {
                coordinates =
                    listOf()
                navController.navigate("SetPolygon")

                with(sharedPref.edit()) {
                    putBoolean(KEY_HAS_NEW_POLYGON, true)
                    apply()
                }
                showKeepPolygonDialog = false
            }
        )
    }

    /**
     * Confirm farm update and ask if they wish to capture new polygon
     */
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
                    if ((coordinates?.size ?: 0) >= 3) {
                        showKeepPolygonDialog = true
                    } else {
                        updateFarmInstance()
                    }
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
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
    val scrollState = rememberScrollState()
    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()
    val isDarkTheme = isSystemInDarkTheme()
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    if (showPermissionRequest.value) {
        LocationPermissionRequest(
            onLocationEnabled = {
                showLocationDialog.value = true
            },
            onPermissionsGranted = {
                showPermissionRequest.value = false
            },
            showLocationDialogNew = showLocationDialogNew,
            hasToShowDialog = showLocationDialogNew.value,
        )
    }

    val locationHelper = LocationHelper(context)
    val mapViewModel: MapViewModel = viewModel()

    var accuracyArray by rememberSaveable { mutableStateOf(listOf<Float>()) }

    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(state = scrollState),
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.update_farm),
            onSearchQueryChanged = {},
            onBackClicked = { navController.popBackStack() },
            showSearch = false,
            showRestore = false,
            onRestoreClicked = {}
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
            colors = TextFieldDefaults.colors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            )
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
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            )
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
            colors = TextFieldDefaults.colors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            )
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
            colors = TextFieldDefaults.colors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                singleLine = true,
                value = truncateToDecimalPlaces(size, 9),
                onValueChange = { it ->
                    val formattedValue = when {
                        validateSize(it) -> it
                        scientificNotationPattern.matcher(it).matches() -> {
                            truncateToDecimalPlaces(formatInput(it), 9)
                        }

                        else -> it
                    }
                    size = formattedValue
                    isValidSize = validateSize(formattedValue)
                    with(sharedPref.edit()) {
                        putString("plot_size", formattedValue)
                        apply()
                    }
                },
                keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                ),
                label = {
                    Text(
                        stringResource(id = R.string.size_in_hectares) + " (*)",
                        color = inputLabelColor
                    )
                },
                isError = size.toFloatOrNull() == null || size.toFloat() <= 0, // Validate size
                modifier =
                Modifier
                    .focusRequester(focusRequester3)
                    .weight(1f)
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    errorLeadingIconColor = Color.Red,
                    cursorColor = inputTextColor,
                    errorCursorColor = Color.Red,
                    focusedIndicatorColor = inputBorder,
                    unfocusedIndicatorColor = inputBorder,
                    errorIndicatorColor = Color.Red,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.background,
                )
            )

            Spacer(modifier = Modifier.width(16.dp))
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
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                    ),
                    modifier = Modifier.menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
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

        Spacer(modifier = Modifier.height(16.dp))
        if ((size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f) < 4f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextField(
                    readOnly = true,
                    value = latitude,
                    onValueChange = { it ->
                        val formattedValue = when {
                            validateNumber(it) -> {
                                truncateToDecimalPlaces(
                                    it,
                                    9
                                )
                            }
                            scientificNotationPattern.matcher(it).matches() -> {
                                truncateToDecimalPlaces(
                                    formatInput(it),
                                    9
                                )
                            }

                            else -> {
                                // Show a Toast message if the input does not meet the requirements
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_latitude_decimal_places),
                                    Toast.LENGTH_SHORT
                                ).show()
                                null
                            }
                        }
                        formattedValue?.let {
                            latitude = it
                        }
                    },
                    label = {
                        Text(
                            stringResource(id = R.string.latitude),
                            color = inputLabelColor
                        )
                    },
                    modifier =
                    Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        errorLeadingIconColor = Color.Red,
                        cursorColor = inputTextColor,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = inputBorder,
                        unfocusedIndicatorColor = inputBorder,
                        errorIndicatorColor = Color.Red,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    readOnly = true,
                    value = longitude,
                    onValueChange = { it ->
                        val formattedValue = when {
                            validateNumber(it) -> {
                                truncateToDecimalPlaces(
                                    it,
                                    9
                                )
                            }
                            scientificNotationPattern.matcher(it).matches() -> {
                                truncateToDecimalPlaces(
                                    formatInput(it),
                                    9
                                )
                            }
                            else -> {
                                // Show a Toast message if the input does not meet the requirements
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_longitude_decimal_places),
                                    Toast.LENGTH_SHORT
                                ).show()
                                null
                            }
                        }
                        formattedValue?.let {
                            longitude = it
                        }
                    },
                    label = {
                        Text(
                            stringResource(id = R.string.longitude),
                            color = inputLabelColor
                        )
                    },
                    modifier =
                    Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        errorLeadingIconColor = Color.Red,
                        cursorColor = inputTextColor,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = inputBorder,
                        unfocusedIndicatorColor = inputBorder,
                        errorIndicatorColor = Color.Red,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                    )
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
                        val enteredSize =
                            size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() } ?: 0f
                        locationHelper.requestLocationPermissionAndUpdateCoordinates(
                            enteredSize = enteredSize,
                            navController = navController,
                            mapViewModel = mapViewModel,
                            onLocationResult = { newLatitude, newLongitude, accuracy ->
                                latitude = newLatitude
                                longitude = newLongitude
                                accuracyArray = accuracyArray + accuracy.toFloat()
                            }
                        )
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
                text =
                if (size.toDoubleOrNull()?.let { convertSize(it, selectedUnit).toFloat() }
                        ?.let { it < 4f } ==
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