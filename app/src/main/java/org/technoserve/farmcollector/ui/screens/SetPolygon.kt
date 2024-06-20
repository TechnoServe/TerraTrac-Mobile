package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.hasLocationPermission
import org.technoserve.farmcollector.map.MapScreen
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.ui.composes.AreaDialog
import org.technoserve.farmcollector.ui.composes.ConfirmDialog
import org.technoserve.farmcollector.utils.GeoCalculator


/**
 * This screen helps you to capture and visualize farm polygon.
 * When capturing, You are able to start, add point, clear map or remove a point on the map
 */
@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SetPolygon(navController: NavController, viewModel: MapViewModel) {
    val context = LocalContext.current as Activity
    var coordinates by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }
    var isCapturingCoordinates by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    val showClearMapDialog = remember { mutableStateOf(false) }
    //  Getting farm details such as polygon or single pair of lat and long if shared from farm list
    val farmData =
        navController.previousBackStackEntry?.arguments?.getSerializable("farmData") as? Pair<Farm, String>
//    cast farmData string to Farm object
    val farmInfo = farmData?.first
    var accuracy by remember { mutableStateOf("") }
    var viewSelectFarm by remember { mutableStateOf(false) }

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 1000 // Update interval in milliseconds
        fastestInterval = 500 // Fastest update interval in milliseconds
    }

    val mapViewModel: MapViewModel = viewModel()
    val size by mapViewModel.size.collectAsState()

    // State to handle TextField value
    var textFieldValue by remember { mutableStateOf(TextFieldValue(size.toString())) }

    if (!isCapturingCoordinates && farmInfo == null) {
        fusedLocationClient.getCurrentLocation(locationRequest.priority,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
            // update map camera position
            if (location != null) {
                accuracy = location.accuracy.toString()
                if (viewModel.state.value.clusterItems.isEmpty()) {
                    viewModel.addCoordinate(location.latitude, location.longitude)
                }
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                accuracy = location.accuracy.toString()
            }
        },
        Looper.getMainLooper()
    )

    // Display coordinates of a farm on map
    if (farmInfo != null && !isCapturingCoordinates && !viewSelectFarm) {
        viewModel.clearCoordinates()
        if (farmInfo.coordinates?.isNotEmpty() == true) {
            viewModel.addCoordinates(farmInfo.coordinates!!)
        } else if (farmInfo.latitude.isNotEmpty() && farmInfo.longitude.isNotEmpty()) {
            viewModel.addMarker(Pair(farmInfo.latitude.toDouble(), farmInfo.longitude.toDouble()))
        }

        viewSelectFarm = true
    }

//    // Confirm farm polygon setting
//    if (showConfirmDialog.value) {
//        ConfirmDialog(stringResource(id = R.string.set_polygon),
//            stringResource(id = R.string.confirm_set_polygon),
//            showConfirmDialog,
//            fun() {
//                viewModel.clearCoordinates()
//                viewModel.addCoordinates(coordinates)
//                navController.previousBackStackEntry?.savedStateHandle?.apply {
//                    set("coordinates", coordinates)
//                }
//                navController.navigateUp()
//            })
//    }



    val enteredArea = size.toDoubleOrNull() ?: 0.0
    val calculatedArea = GeoCalculator.calculateArea(coordinates) ?: 0.0f

    // Confirm farm polygon setting
    if (showConfirmDialog.value) {


        ConfirmDialog(
            title = stringResource(id = R.string.set_polygon),
            message = stringResource(id = R.string.confirm_set_polygon),
            showConfirmDialog,
            fun(){
                mapViewModel.clearCoordinates()
                mapViewModel.addCoordinates(coordinates)
                navController.previousBackStackEntry?.savedStateHandle?.apply {
                    set("coordinates", coordinates)
                }

                mapViewModel.showAreaDialog(calculatedArea.toString(), enteredArea.toString())
            }
        )
    }
    // Display AreaDialog if needed
    AreaDialog(
        showDialog = mapViewModel.showDialog.collectAsState().value,
        onDismiss = { mapViewModel.dismissDialog() },
        onConfirm = { chosenArea ->
            val chosenSize = if (chosenArea.contains("Calculated")) mapViewModel.calculatedArea.toString() else mapViewModel.size.toString()
            mapViewModel.updateSize(chosenSize.toString())
            textFieldValue = TextFieldValue(chosenSize.toString()) // Update TextFieldValue
            navController.navigateUp()
        },
        calculatedArea = calculatedArea.toDouble(),
        enteredArea = enteredArea


    )

    // Confirm clear map
    if (showClearMapDialog.value) {
        ConfirmDialog(stringResource(id = R.string.set_polygon),
            stringResource(id = R.string.clear_map),
            showClearMapDialog,
            fun() {
                coordinates = listOf()// Clear coordinates array when starting
                accuracy = ""
                viewModel.clearCoordinates() // Clear google map
                showClearMapDialog.value = false
            })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (viewSelectFarm) 0.65f else if (accuracy.isNotEmpty()) .87f else .93f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google map
            MapScreen(
                state = viewModel.state.value,
                setupClusterManager = viewModel::setupClusterManager,
                calculateZoneViewCenter = viewModel::calculateZoneLatLngBounds,
                onMapTypeChange = viewModel::onMapTypeChange,
            )
        }
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (!viewSelectFarm && accuracy.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 14.dp)
                ) {

                    Text(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = Color.Black,
                        text = stringResource(id = R.string.accuracy) + ": $accuracy m"
                    )
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 10.dp),
                horizontalArrangement = if (viewSelectFarm) Arrangement.Center else Arrangement.Start
            ) {
                // Hiding some buttons depending on page usage. Viewing verse setting farm polygon
                if (viewSelectFarm) {
                    Row {
                        if (farmInfo != null) {
                            Column(
                                modifier = Modifier.padding(5.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.farm_info),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(5.dp)
                                )
                                Column(
                                    content = { },
                                    modifier = Modifier
                                        .width(200.dp)
                                        .background(Color.Black)
                                        .height(2.dp)
                                )
                                Text(
                                    text = "${stringResource(id = R.string.farm_name)}: ${farmInfo.farmerName}",
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                                Text(text = "${stringResource(id = R.string.member_id)}: ${farmInfo.memberId.ifEmpty { "N/A" }}")
                                Text(
                                    text = "${stringResource(id = R.string.village)}: ${farmInfo.village}",
                                )
                                Text(text = "${stringResource(id = R.string.district)}: ${farmInfo.district}")
                                if (farmInfo.coordinates?.isEmpty() == true) {
                                    Text(text = "${stringResource(id = R.string.latitude)}: ${farmInfo.latitude}")
                                    Text(text = "${stringResource(id = R.string.longitude)}: ${farmInfo.longitude}")
                                }
                                Text(
                                    text = "${stringResource(id = R.string.size)}: ${farmInfo.size} ${
                                        stringResource(
                                            id = R.string.ha
                                        )
                                    }"
                                )
                            }
                        }
                    }
                    Row {
                        Button(shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxWidth(0.23f),
                            onClick = {
                                viewModel.clearCoordinates()
                                navController.navigateUp()
                            }) {
                            Text(text = stringResource(id = R.string.close))
                        }
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxWidth(0.23f)
                                .padding(start = 10.dp),
                            onClick = {
                                navController.navigate("updateFarm/${farmInfo?.id}")
                            },
                        ) {
                            Text(text = stringResource(id = R.string.update))
                        }
                    }
                } else {
                    ElevatedButton(modifier = Modifier
                        .fillMaxWidth(0.22f),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = {
                            if (!isCapturingCoordinates && !showConfirmDialog.value) {
                                coordinates = listOf() // Clear coordinates array when starting
                                viewModel.clearCoordinates()
                                isCapturingCoordinates = true
                            } else if (isCapturingCoordinates && !showConfirmDialog.value) {
                                showConfirmDialog.value = true
                            }
                        }) {
                        Text(
                            fontSize = 12.sp,
                            color = Color.Black,
                            text = if (isCapturingCoordinates) stringResource(id = R.string.finish) else stringResource(
                                id = R.string.start
                            )
                        )
                    }
                    ElevatedButton(modifier = Modifier
                        .fillMaxWidth(0.28f),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF1C9C3C)),
                        onClick = {
                            if (context.hasLocationPermission() && isCapturingCoordinates) {
                                fusedLocationClient.getCurrentLocation(locationRequest.priority,
                                    object : CancellationToken() {
                                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                            CancellationTokenSource().token

                                        override fun isCancellationRequested() = false
                                    }).addOnSuccessListener { location: Location? ->
                                    if (location == null) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.can_not_get_location),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        if (location.latitude.toString()
                                                .split(".")[1].length < 6 || location.longitude.toString()
                                                .split(".")[1].length < 6
                                        ) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.can_not_get_location),
                                                Toast.LENGTH_LONG
                                            ).show()

                                            return@addOnSuccessListener
                                        }

//                                            update map camera position
                                        val coordinate =
                                            Pair(location.latitude, location.longitude)
                                        accuracy = location.accuracy.toString()

                                        coordinates = coordinates + coordinate
                                        viewModel.addMarker(coordinate)

//                                                add camera position
                                        viewModel.addCoordinate(
                                            location.latitude, location.longitude
                                        )
                                    }
                                }
                            }
                        }) {
                        Text(
                            fontSize = 12.sp,
                            color = Color.White,
                            text = stringResource(id = R.string.add_point)
                        )
                    }
                    ElevatedButton(modifier = Modifier
                        .fillMaxWidth(0.22f),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = {
                            showClearMapDialog.value = true
                        }) {
                        Text(
                            fontSize = 12.sp,
                            color = Color.Black,
                            text = stringResource(id = R.string.reset)
                        )
                    }
                    ElevatedButton(modifier = Modifier.fillMaxWidth(0.28f),
                        colors = ButtonDefaults.buttonColors(Color(0xFFCA1212)),
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            coordinates = coordinates.dropLast(1)
                            viewModel.removeLastCoordinate();
                        }) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 12.sp,
                            color = Color.White,
                            text = stringResource(id = R.string.drop_point)
                        )
                    }
                }
            }
        }
    }
}


