package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import java.util.concurrent.TimeUnit


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
    val arguments = navController.currentBackStackEntry?.arguments
    //  Getting farm details such as polygon or single pair of lat and long if shared from farm list
    var farmCoordinate =
        navController.previousBackStackEntry?.arguments?.getSerializable("coordinates") as? ArrayList<Pair<Double, Double>>
    var latLong =
        navController.previousBackStackEntry?.arguments?.getSerializable("latLong") as? Pair<Double, Double>
    var accuracy by remember { mutableStateOf("") }
    var viewSelectFarm by remember { mutableStateOf(false) }
    val hasCoordinates = farmCoordinate != null && farmCoordinate.isNotEmpty() || latLong != null
    // Display coordinates of a farm on map
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
    // Confirm farm polygon setting
    if(showConfirmDialog.value){
        ConfirmDialog(stringResource(id = R.string.set_polygon),
            stringResource(id = R.string.confirm_set_polygon),showConfirmDialog, fun(){
            viewModel.clearCoordinates()
            viewModel.addCoordinates(coordinates)
            navController.previousBackStackEntry
                ?.savedStateHandle?.apply {
                    set( "coordinates", coordinates)
                }
            navController.navigateUp()
        })
    }
    // Getting GPS signal strength
    if (isCapturingCoordinates)
    {
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(5)  // Update location every 5 seconds
            fastestInterval = TimeUnit.SECONDS.toMillis(2)  // Allow faster updates if possible
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (viewSelectFarm) 0.8f else 0.6f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google map
            MapScreen(
                state = viewModel.state.value,
                setupClusterManager = viewModel::setupClusterManager,
                calculateZoneViewCenter = viewModel::calculateZoneLatLngBounds
            )
        }
        Column(
            modifier = Modifier
                .background(Color.DarkGray)
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
                modifier = Modifier.fillMaxWidth()
                    .align(alignment = Alignment.End),
            ) {
                // Hidding some buttons depending on page usage. Viewing verse setting farm polygon
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
                    ElevatedButton(
                        modifier = Modifier.fillMaxWidth(0.23f)
                            .padding(PaddingValues(1.dp,1.dp)),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
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
                        Text(
                            color = Color.Black ,
                            text = if (isCapturingCoordinates) "Close" else "Start")
                    }
                    ElevatedButton(
                        modifier = Modifier.fillMaxWidth(0.30f)
                            .padding(PaddingValues(1.dp,1.dp)),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF1C9C3C)),
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
                                            }
                                        }
                                    }
                            }
                        }
                    ) {
                        Text(text = "Add Point")
                    }
                    ElevatedButton(
                        modifier = Modifier.fillMaxWidth(0.22f)
                            .padding(PaddingValues(1.dp,1.dp)),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(Color.White),
                        onClick = {
                            coordinates = listOf()// Clear coordinates array when starting
                            accuracy = ""
                            viewModel.clearCoordinates() // Clear google map

                        }
                    ) {
                        Text(color = Color.Black ,
                            text = "Clear")
                    }
                    ElevatedButton(
                        modifier = Modifier.fillMaxWidth(0.25f)
                            .padding(PaddingValues(vertical = 1.dp, horizontal = 1.dp)),
                        colors = ButtonDefaults.buttonColors(Color(0xFFCA1212)),
                        shape = RoundedCornerShape(0.dp),
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


