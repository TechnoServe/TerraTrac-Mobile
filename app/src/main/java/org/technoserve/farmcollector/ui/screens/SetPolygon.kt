package org.technoserve.farmcollector.ui.screens

import android.app.Activity
import android.location.Location
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
import com.codingwithmitch.composegooglemaps.MapViewModel
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.technoserve.farmcollector.map.MapScreen
import org.technoserve.farmcollector.hasLocationPermission


@Composable
fun SetPolygon(navController: NavController, viewModel: MapViewModel) {
    val context = LocalContext.current as Activity
    var coordinates by remember { mutableStateOf(listOf<String>()) }
    var isCapturingCoordinates by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
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
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Text(
                text = "Coordinates of the farm polygon",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = coordinates.joinToString(separator = ", "),
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .verticalScroll(state = ScrollState(1)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isCapturingCoordinates = !isCapturingCoordinates
                        if (isCapturingCoordinates) {
                            coordinates = listOf()// Clear coordinates array when starting
                        }else
                        {
//                            navController.previousBackStackEntry
//                                ?.savedStateHandle
//                                ?.set("cordinates", coordinates)
//                            navController.popBackStack()
                            navController.navigateUp()
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
                                fastestInterval = 5000 // Fastest update interval in milliseconds

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
//                                        Toast.makeText(
//                                            this,
//                                            "Cannot get location.",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
                                    else {
                                        viewModel.addCoordinate(location.latitude, location.longitude)
                                        coordinates = coordinates + "[${location.latitude}, ${location.longitude}]"
                                    }
                                }

                        }
                    }
                ) {
                    Text(text = "Add Point")
                }
            }
        }
    }
}

