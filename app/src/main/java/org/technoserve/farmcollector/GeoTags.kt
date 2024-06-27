package org.technoserve.farmcollector

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class GeoTags : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MainContent()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainContent() {
    var permissionsGranted by remember { mutableStateOf(false) }

    RequestPermissions { granted ->
        permissionsGranted = granted
    }

    if (permissionsGranted) {
        CaptureImageAndGeotag()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please grant camera and location permissions to continue.")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(onPermissionsResult: (Boolean) -> Unit) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(cameraPermissionState.status, locationPermissionState.status) {
        if (cameraPermissionState.status.isGranted && locationPermissionState.status.isGranted) {
            onPermissionsResult(true)
        } else {
            cameraPermissionState.launchPermissionRequest()
            locationPermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(cameraPermissionState.status, locationPermissionState.status) {
        onDispose {
            if (cameraPermissionState.status.isGranted && locationPermissionState.status.isGranted) {
                onPermissionsResult(true)
            } else {
                onPermissionsResult(false)
            }
        }
    }
}

@Composable
fun CaptureImageAndGeotag() {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf<Location?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(true) {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location = it
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CameraPreview(cameraProviderFuture, imageCapture)

        Button(onClick = {
            val photoFile = File(context.cacheDir, "temp_image.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = photoFile.toUri()
                    location?.let { loc ->
                        overlayGeotagAndSave(photoFile, loc, context)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle error
                }
            })
        }) {
            Text("Capture Image")
        }

        imageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Captured Image",
                modifier = Modifier.size(200.dp)
            )
            location?.let {
                Text("Latitude: ${it.latitude}, Longitude: ${it.longitude}")
            }
        }
    }
}

@Composable
fun CameraPreview(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>, imageCapture: ImageCapture) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.bindToLifecycle(ctx as LifecycleOwner, cameraSelector, preview, imageCapture)
            previewView
        }
    )
}

fun overlayGeotagAndSave(photoFile: File, location: Location, context: Context) {
    val bitmap = BitmapFactory.decodeFile(photoFile.path)
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        setShadowLayer(1.5f, -1f, 1f, Color.BLACK)
    }

    // Date and Time
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // Location Name
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    val locationName = if (addresses?.isNotEmpty() == true) {
        addresses[0]?.getAddressLine(0)
    } else {
        "Unknown Location"
    }
    // Overlay text
    val text = """
        Lat: ${location.latitude}, Lon: ${location.longitude}
        Date: $date
        Location: $locationName
    """.trimIndent()

    val x = 20f
    val y = mutableBitmap.height - 40f

    text.split("\n").forEachIndexed { index, line ->
        canvas.drawText(line, x, y + index * 45f, paint) // Line height adjustment
    }

    // Save the new bitmap with the text overlay
    try {
        val fos = FileOutputStream(photoFile)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        // Now save the file to the gallery
        saveImageToGallery(context, photoFile)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun saveImageToGallery(context: Context, photoFile: File) {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
        val bitmap = BitmapFactory.decodeFile(photoFile.path)
        outputStream.use { out ->
            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        }
        Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
    }
}
