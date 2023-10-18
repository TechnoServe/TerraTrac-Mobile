package org.technoserve.farmcollector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import org.technoserve.farmcollector.database.Farm
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import androidx.compose.foundation.Image
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects


@Composable
fun AddFarm(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FarmListHeader(
            title = "Add Farm",
            onAddFarmClicked = { /* Handle adding a farm here */ },
            onBackClicked = { navController.popBackStack() },
            showAdd = false

        )
        Spacer(modifier = Modifier.height(16.dp))
        FarmForm(navController)
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmForm(navController: NavController) {
    val context = LocalContext.current as Activity
    var isImageUploaded by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("") }
    var farmerPhoto by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var purchases by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    val mylocation = remember { mutableStateOf("") }
    val currentPhotoPath = remember { mutableStateOf("") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )

    fun validateForm(): Boolean {
        var isValid = true

        if (farmerName.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (village.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (district.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (size.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (purchases.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

//        if (!isImageUploaded) {
//            isValid = false
//            // You can display an error message for this field if needed
//        }

        if (latitude.isBlank() || longitude.isBlank()) {
            isValid = false
            // You can display an error message for these fields if needed
        }

        return isValid
    }

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    val scrollState = rememberScrollState()

//    val cameraLauncher =
//        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){
//            capturedImageUri = uri
//            farmerPhoto.value = BitmapFactory.decodeStream(
//                uri?.let { it1 -> context.contentResolver?.openInputStream(it1) }
//            )
//            val stream = ByteArrayOutputStream()
//            farmerPhoto.value?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            val imgAsByteArray: ByteArray = stream.toByteArray()
//            farmerPhoto.value = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.size)
//        }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { it ->
        uri?.let { it1 ->
            val imageInputStream = context.contentResolver.openInputStream(it1)
            val imageFileName = "image${Instant.now().millis}.jpg"
            val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EGNSS_IMAGES")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val imageFile = File(storageDir, imageFileName)

            try {
                imageInputStream?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Update the database with the file path
                farmerPhoto = imageFile.absolutePath
                Log.d("farmerphoto","${farmerPhoto}")
                // Update other fields in the Farm object
                // Then, insert or update the farm object in your database
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it)
        {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        }
        else
        {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }











    Column(
        modifier = Modifier

            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(state = scrollState)
    ) {
        TextField(
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text("Farm Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        TextField(
            value = village,
            onValueChange = { village = it },
            label = { Text("Village") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        TextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("District") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        TextField(
            value = size,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
            ),
            onValueChange = { size = it },
            label = { Text("Size in hectares") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        TextField(
            value = purchases,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
            ),
            onValueChange = { purchases = it },
            label = { Text("Purchases in current year in Kgs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                readOnly = true,
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
            TextField(
                readOnly = true,
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )
        }
        Button(
            onClick = {
                // Simulate collecting latitude and longitude

                if (context.hasLocationPermission()) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { locationResult ->
                        locationResult?.let { lastLocation ->
                            latitude = "${lastLocation.latitude}"
                            longitude = "${lastLocation.longitude}"
                            Log.d("FARM_LOCATION",mylocation.value)
                        }
                    }.addOnFailureListener { e ->
                        Log.d("LOCATION_ERROR","${e.message}")
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 5.dp)
                .height(50.dp),
        ) {
            Text(text = "Get Coordinates")
        }

        if (!farmerPhoto.isBlank())
        {
            val imgFile = File(farmerPhoto)

            // on below line we are checking if the image file exist or not.
            var imgBitmap: Bitmap? = null
            if (imgFile.exists()) {
                // on below line we are creating an image bitmap variable
                // and adding a bitmap to it from image file.
                imgBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            }
            Image(
                modifier = Modifier
                    .size(width = 200.dp, height = 150.dp)
                    .padding(16.dp, 8.dp)
                    .align(Alignment.CenterHorizontally)
                    ,
                painter = rememberAsyncImagePainter(imgBitmap),
                contentDescription = null
            )
        }
        else
        {
            Image(
                modifier = Modifier
                    .size(width = 200.dp, height = 150.dp)
                    .padding(16.dp, 8.dp)
                    .align(Alignment.CenterHorizontally)
                ,
                painter = painterResource(id = R.drawable.image_placeholder),
                contentDescription = null
            )
        }

        Button(
            onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
                {
                    cameraLauncher.launch(uri)
                    isImageUploaded = true
                }
                else
                {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        ){
            Text(text = "take picture")
        }
        Button(
            onClick = {
                val isValid = validateForm()
                if(isValid){
                    val item = addFarm(
                        farmViewModel,
                        farmerPhoto,
                        farmerName,
                        village,
                        district,
                        purchases.toFloat(),
                        size.toFloat(),
                        latitude,
                        longitude
                    )
                    val returnIntent = Intent()
                    context.setResult(Activity.RESULT_OK, returnIntent)
//                    context.finish()
                    navController.navigate("farmList")
                }
                else {
                    Toast.makeText(context, "Please fill in all fields and take a picture", Toast.LENGTH_SHORT).show()
                }
                      },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Add Farm")
        }
    }
}

fun addFarm(
    farmViewModel: FarmViewModel,
    farmerPhoto: String,
    farmerName: String,
    village:String,
    district:String,
    purchases:Float,
    size:Float,
    latitude:String,
    longitude:String

): Farm {
    val farm = Farm(
        farmerPhoto,
        farmerName,
        village,
        district,
        purchases,
        size,
        latitude,
        longitude,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis
    )
    farmViewModel.addFarm(farm)
    return farm
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}

fun createDefaultBitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}


