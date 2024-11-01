package org.technoserve.farmcollector.database

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.ui.screens.isLocationEnabled
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


data class LocationState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUsingGPS: Boolean = true,
    val accuracy: Float = 0f
)

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}


class LocationHelper(private val context: Context) : SensorEventListener {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val _locationState = MutableStateFlow(LocationState(isLoading = true))
    val locationState: StateFlow<LocationState> = _locationState

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var stepCount: Int = 0
    private var previousAzimuth: Float = 0.0f
    private var strideLength: Double = 0.7
    private var accelerometerValues: FloatArray = FloatArray(3)
    private val accuracyArray = mutableListOf<Float>()

    var showPermissionRequest = mutableStateOf(false)
    var showLocationDialog = mutableStateOf(false)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L  // Request updates every second
    ).setMinUpdateIntervalMillis(500L).build()

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Primary location update function
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            showPermissionRequest.value = true
            throw LocationException("Missing location permission")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationDialog.value = true
            startDeadReckoning()
            throw LocationException("GPS and Network are disabled")
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                    updateLocationState(
                        location.latitude,
                        location.longitude,
                        location.accuracy,
                        isUsingGPS = true
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener {
            _locationState.value = LocationState(
                error = it.message,
                isLoading = false,
                isUsingGPS = false
            )
            startDeadReckoning()
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            stopSensorUpdates()
        }
    }

    // Fallback method using dead reckoning with sensors
    private fun startDeadReckoning() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values.clone()
                detectStep(event.values)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> calculateDirection(event.values)
        }
    }

    private fun detectStep(values: FloatArray) {
        val magnitude = Math.sqrt((values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble())
        if (magnitude > 12) {
            stepCount++
            updateLocationWithDeadReckoning()
        }
    }

    private fun calculateDirection(magneticField: FloatArray) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticField)) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            previousAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        }
    }

    private fun updateLocationWithDeadReckoning() {
        val currentState = _locationState.value
        if (!currentState.isUsingGPS) {
            val distanceTraveled = stepCount * strideLength
            val azimuthRadians = Math.toRadians(previousAzimuth.toDouble())
            val latChange = (distanceTraveled * Math.cos(azimuthRadians)) / 111320
            val lonChange = (distanceTraveled * Math.sin(azimuthRadians)) /
                    (111320 * Math.cos(Math.toRadians(currentState.latitude)))

            updateLocationState(
                currentState.latitude + latChange,
                currentState.longitude + lonChange,
                accuracy = -1f,
                isUsingGPS = false
            )
        }
    }

    private fun updateLocationState(latitude: Double, longitude: Double, accuracy: Float, isUsingGPS: Boolean) {
        if (accuracy > 0) accuracyArray.add(accuracy)
        val meanAccuracy = accuracyArray.average().toFloat()
        _locationState.value = LocationState(latitude, longitude, isLoading = false, error = null, isUsingGPS, meanAccuracy)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun stopSensorUpdates() {
        sensorManager.unregisterListener(this)
    }

    // Function to request location updates with additional navigation handling
    fun requestLocationPermissionAndUpdateCoordinates(
        enteredSize: Float,
        navController: NavController,
        mapViewModel: MapViewModel,
        onLocationResult: (String, String, String) -> Unit
    ) {
        try {
            if (isLocationEnabled() && hasLocationPermission()) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.lastLocation?.let { lastLocation ->
                                // Get latitude, longitude, and accuracy as strings
                                val latitude = lastLocation.latitude.toString()
                                val longitude = lastLocation.longitude.toString()
                                val accuracy = lastLocation.accuracy.toString()

                                // Log the coordinates and accuracy
                                Log.d(
                                    "Coordinates",
                                    "Coordinates: Lat = $latitude, Long = $longitude"
                                )
                                Log.d("Accuracy", "Accuracy: $accuracy")

                                // Call the callback with the obtained values
                                onLocationResult(latitude, longitude, accuracy)
                            }
                        }
                    },
                    Looper.getMainLooper()
                )
            } else {
                startDeadReckoning()
                Log.d("LocationHelper", "Permission not granted or location disabled")
                // Call with default values or handle as needed
                onLocationResult("0.0", "0.0", "0.0")
            }
        } catch (e: SecurityException) {
            // Handle the case where location permission was denied at runtime
            Log.e("LocationHelper", "Location permission denied: ${e.message}")
            // Call with default values or handle as needed
            onLocationResult("0.0", "0.0", "0.0")
        }

        if (enteredSize >= 4f) {
            navController.currentBackStackEntry?.arguments?.putParcelable("farmData", null)
            navController.navigate("setPolygon")
            mapViewModel.clearCoordinates()
        }
    }

    fun requestLocationPermissionAndUpdatePolygon(
        onLocationResult: (latitude: String, longitude: String, accuracy: String) -> Unit
    ) {
        val errorResponse = Triple("0.0", "0.0", "0.0")

        try {
            if (isLocationEnabled() && hasLocationPermission()) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    }
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationResult(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            location.accuracy.toString()
                        )
                    } else {
                        onLocationResult(errorResponse.first, errorResponse.second, errorResponse.third)
                    }
                }.addOnFailureListener {
                    onLocationResult(errorResponse.first, errorResponse.second, errorResponse.third)
                }
            } else {
                startDeadReckoning()
                onLocationResult(errorResponse.first, errorResponse.second, errorResponse.third)
            }
        } catch (e: SecurityException) {
            onLocationResult(errorResponse.first, errorResponse.second, errorResponse.third)
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationResult: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            showPermissionRequest.value = true
            onLocationResult(null)
            return
        }
        fusedLocationClient.getCurrentLocation(
            locationRequest.priority,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(location)
                updateLocationState(location.latitude, location.longitude, location.accuracy, true)
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_LONG).show()
                onLocationResult(null)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get location", Toast.LENGTH_LONG).show()
            onLocationResult(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(onLocationUpdate: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            showPermissionRequest.value = true
            return
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                onLocationUpdate(location)
                location?.let {
                    updateLocationState(it.latitude, it.longitude, it.accuracy, true)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun cleanup() {
        stopSensorUpdates()
    }
}

class LocationException(message: String) : Exception(message)



