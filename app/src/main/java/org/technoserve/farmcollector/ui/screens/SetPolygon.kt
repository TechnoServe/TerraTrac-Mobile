package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import org.technoserve.farmcollector.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.tns.lab.composegooglemaps.MapViewModel
import com.tns.lab.composegooglemaps.clusters.ZoneClusterManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.maps.android.compose.GoogleMap
import org.technoserve.farmcollector.map.MapScreen
import org.technoserve.farmcollector.hasLocationPermission
import org.technoserve.farmcollector.ui.composes.ConfirmDialog


@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SetPolygon(navController: NavController, viewModel: MapViewModel) {
    val context = LocalContext.current as Activity
    var coordinates by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }
    var isCapturingCoordinates by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    val arguments = navController.currentBackStackEntry?.arguments
    //  Getting farm details such as polygon or single pair of lat and long if shared
    var farmCoordinate =
        navController.previousBackStackEntry?.arguments?.getSerializable("coordinates") as? ArrayList<Pair<Double, Double>>
    var latLong =
        navController.previousBackStackEntry?.arguments?.getSerializable("latLong") as? Pair<Double, Double>
    var accuracy by remember { mutableStateOf("") }
    var viewSelectFarm by remember { mutableStateOf(false) }
    val hasCoordinates = farmCoordinate != null && farmCoordinate.isNotEmpty() || latLong != null
    // Display coordinates of a farm
    if (hasCoordinates) {
        viewModel.clearCoordinates()
        if (farmCoordinate != null) {
            viewModel.addCoordinates(farmCoordinate)
        }
        if (latLong != null) {
            viewModel.addMarker(latLong)
        }
        navController.previousBackStackEntry?.arguments?.remove("coordinates")
        navController.previousBackStackEntry?.arguments?.remove("latLong")
        viewSelectFarm = true
    }
    // Confirm setting farm polygon
    if(showConfirmDialog.value){
        // Confirm farm polygon setup
        ConfirmDialog(stringResource(id = R.string.set_polygon),
            stringResource(id = R.string.confirm_set_polygon),showConfirmDialog, fun(){
            viewModel.clearCoordinates()
            viewModel.addCoordinates(coordinates)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("coordinates", coordinates)
            navController.navigateUp()
        })
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight( if (viewSelectFarm) 0.8f else 0.6f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapScreen(
                state = viewModel.state.value,
                setupClusterManager = viewModel::setupClusterManager,
                calculateZoneViewCenter = viewModel::calculateZoneLatLngBounds
            )
        }
        Column(
            modifier = Modifier
                .background(Color.DarkGray)
                .padding(14.dp)
                .fillMaxWidth()
                .fillMaxHeight()


        ) {
            Text(
                text = "Coordinates of the farm polygon",
                fontSize = 20.sp
            )
            if (!viewSelectFarm) Text(stringResource(id = R.string.accuracy) + ": ${accuracy}")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = coordinates.joinToString(separator = ", "),
                modifier = Modifier
                    .fillMaxHeight(0.4f)
                    .verticalScroll(state = ScrollState(1)),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (viewSelectFarm) {
                    Button(
                        onClick = {
                            viewModel.clearCoordinates()
                            navController.navigateUp()
                        }
                    ) {
                        Text(text = "Close")
                    }
                } else {
                    Button(
                        onClick = {
                            if(!isCapturingCoordinates && !showConfirmDialog.value)
                            {
                                coordinates = listOf() // Clear coordinates array when starting
                                viewModel.clearCoordinates()
                                isCapturingCoordinates = true
                            }else if(isCapturingCoordinates && !showConfirmDialog.value)
                            {
                                showConfirmDialog.value = true
                            }

                        }
                    ) {
                        Text(text = if (isCapturingCoordinates) "Close" else "Start")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (context.hasLocationPermission() && isCapturingCoordinates) {
                                val locationRequest = LocationRequest.create().apply {
                                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                    interval = 10000 // Update interval in milliseconds
                                    fastestInterval =
                                        5000 // Fastest update interval in milliseconds

                                }

                                fusedLocationClient.getCurrentLocation(
                                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                                    object : CancellationToken() {
                                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                            CancellationTokenSource().token

                                        override fun isCancellationRequested() = false
                                    })
                                    .addOnSuccessListener { location: Location? ->
                                        if (location == null)
                                            Toast.makeText(
                                                context,
                                                "Can't get location, Try again",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        else {
                                            val coordinate = Pair(location.latitude, location.longitude)
                                            if (coordinates.isNotEmpty() && coordinates.get(coordinates.lastIndex) == coordinate
                                            ) {
                                                Toast.makeText(
                                                    context,
                                                    "Can't get location, Try again",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                accuracy = location.accuracy.toString()

                                                coordinates = coordinates + coordinate
                                                viewModel.addMarker(coordinate)
                                                System.out.println("Current Location------ coordinate: ${coordinates}-----LatLong-(-${location.latitude})--ac--${location.accuracy}")

                                            }
                                        }
                                    }
                            }
                        }
                    ) {
                        Text(text = "Add Point")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            coordinates = listOf()// Clear coordinates array when starting
                            accuracy = ""
                            viewModel.clearCoordinates()

                        }
                    ) {
                        Text(text = "Clear")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            coordinates = coordinates.dropLast(1)
                            viewModel.removeLastCoordinate();
                        }
                    ) {
                        Text(text = "Remove")
                    }
                }
            }
        }
    }
}


