package org.technoserve.farmcollector.database.helpers.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.map.LocationState
import org.technoserve.farmcollector.viewmodels.MapViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * This File contains LocationHelper class that is responsible for capturing the farm plot's coordinates and polygon using GPS and sensors as an alternative
 *
 */

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
        val magnitude =
            Math.sqrt((values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble())
        if (magnitude > 12) {
            stepCount++
            updateLocationWithDeadReckoning()
        }
    }

    private fun calculateDirection(magneticField: FloatArray) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        if (SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerValues,
                magneticField
            )
        ) {
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

    private fun updateLocationState(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        isUsingGPS: Boolean
    ) {
        if (accuracy > 0) accuracyArray.add(accuracy)
        val meanAccuracy = accuracyArray.average().toFloat()
        _locationState.value = LocationState(
            latitude,
            longitude,
            isLoading = false,
            error = null,
            isUsingGPS,
            meanAccuracy
        )
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
                        onLocationResult(
                            errorResponse.first,
                            errorResponse.second,
                            errorResponse.third
                        )
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
                Toast.makeText(context, context.getString(R.string.can_not_get_location), Toast.LENGTH_LONG).show()
                onLocationResult(null)
            }
        }.addOnFailureListener {
            Toast.makeText(context, context.getString(R.string.location_update_failed), Toast.LENGTH_LONG).show()
            onLocationResult(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(onLocationUpdate: (Location?) -> Unit) {
        // Check location permission
        if (!hasLocationPermission()) {
            showPermissionRequest.value = true
            return
        }

        // Check if GPS or network is enabled
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            showLocationDialog.value = true
            startDeadReckoning()
            return
        }

        // Create location callback
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Update the location state
                    updateLocationState(
                        location.latitude,
                        location.longitude,
                        location.accuracy,
                        isUsingGPS = true
                    )

                    // Notify the callback
                    onLocationUpdate(location)
                }
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            _locationState.value = _locationState.value.copy(
                isLoading = false,
                isUsingGPS = true,
                error = null
            )
        }.addOnFailureListener { exception ->
            _locationState.value = LocationState(
                error = exception.message,
                isLoading = false,
                isUsingGPS = false
            )
            startDeadReckoning()
        }

        // Register a cleanup function with the CoroutineScope
        (context as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                fusedLocationClient.removeLocationUpdates(locationCallback)
                stopSensorUpdates()
            }
        })
    }


    fun cleanup() {
        stopSensorUpdates()
    }
}




