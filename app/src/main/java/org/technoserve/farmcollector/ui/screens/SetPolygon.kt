package org.technoserve.farmcollector.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.technoserve.farmcollector.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.core.app.ActivityCompat
import org.technoserve.farmcollector.hasLocationPermission

@Composable
fun SetPolygon(navController: NavController) {
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
            Text(text = "Oops! You are Offline!!")
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
//                                interval = 10000 // Update interval in milliseconds
//                                fastestInterval = 5000 // Fastest update interval in milliseconds
                            }
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                object : LocationCallback() {
                                    @SuppressLint("MissingPermission")
                                    override fun onLocationResult(locationResult: LocationResult) {
                                        locationResult.lastLocation?.let { lastLocation ->
                                            coordinates =
                                                coordinates + "[${lastLocation.latitude}, ${lastLocation.longitude}]"
                                        }
                                    }
                                },
                                Looper.getMainLooper()
                            )
                        }
                    }
                ) {
                    Text(text = "Add Point")
                }
            }
        }
    }
}

