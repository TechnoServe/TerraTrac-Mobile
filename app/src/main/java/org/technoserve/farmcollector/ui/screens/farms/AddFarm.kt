package org.technoserve.farmcollector.ui.screens.farms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.database.models.ParcelablePair

import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.ui.components.FarmForm
import org.technoserve.farmcollector.ui.components.FarmListHeader
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/**
 * This function is used to add a capture farm details and location
 * It also allows the user to add a polygon representing the farm's area
 *
 * @param navController the navigation controller to navigate between screens
 * @param siteId the id of the collection site to which the farm belongs
 * @param coordinatesData the initial coordinates of the farm's location
 */
@Composable
fun AddFarm(navController: NavController, siteId: Long) {
    var coordinatesData: List<Pair<Double, Double>>? = null
    var accuracyArrayData: List<Float?>? = null
    if (navController.currentBackStackEntry!!.savedStateHandle.contains("coordinates")) {
        val parcelableCoordinates = navController.currentBackStackEntry!!
            .savedStateHandle
            .get<List<ParcelablePair>>("coordinates")
        coordinatesData = parcelableCoordinates?.map { Pair(it.first, it.second) }
        accuracyArrayData =
            navController.currentBackStackEntry!!.savedStateHandle.get<List<Float?>>("accuracyArray")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.add_farm),
            onSearchQueryChanged = {},
            onBackClicked = { navController.popBackStack() },
            showSearch = false,
            showRestore = false,
            onRestoreClicked = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        FarmForm(navController, siteId, coordinatesData, accuracyArrayData)
    }
}

// Helper function to truncate a string representation of a number to a specific number of decimal places
fun truncateToDecimalPlaces(value: String, decimalPlaces: Int): String {
    val dotIndex = value.indexOf('.')
    return if (dotIndex == -1 || dotIndex + decimalPlaces + 1 > value.length) {
        value
    } else {
        value.substring(0, dotIndex + decimalPlaces + 1)
    }
}

// Function to read and format stored value
fun readStoredValue(sharedPref: SharedPreferences): String {
    val storedValue = sharedPref.getString("plot_size", "") ?: ""
    val formattedValue = truncateToDecimalPlaces(storedValue, 9)
    return formattedValue
}

fun formatInput(input: String): String {
    return try {
        val number = BigDecimal(input)
        val scale = number.scale()
        val decimalPlaces = scale - number.precision()
        when {
            decimalPlaces > 3 -> {
                BigDecimal(input).setScale(9, RoundingMode.DOWN).stripTrailingZeros()
                    .toPlainString()
            }
            decimalPlaces == 0 -> {
                input
            }

            else -> {
                val formattedNumber = number.setScale(9, RoundingMode.DOWN)
                formattedNumber.stripTrailingZeros().toPlainString()
            }
        }
    } catch (e: NumberFormatException) {
        input
    }
}

fun validateSize(size: String): Boolean {
    val regex = Regex("^[0-9]*\\.?[0-9]*$")
    return size.matches(regex) && size.toFloatOrNull() != null && size.toFloat() > 0 && size.isNotBlank()
}

fun validateNumber(number: String): Boolean {
    val regex = Regex("^[0-9]*\\.?[0-9]*$")
    return number.matches(regex) && number.toFloatOrNull() != null && number.toFloat() > 0 && number.isNotBlank()
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
    coordinates: List<Pair<Double, Double>>?,
    accuracyArray: List<Float?>?
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
        accuracyArray,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis
    )
    farmViewModel.addFarm(farm, siteId)
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
                        showLocationDialogNew.value = false
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
                        showLocationDialogNew.value = false
                    }) {
                        Text(stringResource(id = R.string.no))
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 6.dp
            )
        }
    }
}

fun List<Pair<Double, Double>>.toLatLngList(): List<LatLng> {
    return map { LatLng(it.first, it.second) }
}


