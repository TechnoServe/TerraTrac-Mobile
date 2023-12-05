package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.hasLocationPermission
import java.io.IOException
import java.io.InputStream
import java.util.Objects

@Composable
fun AddSite(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.add_site),
            onAddFarmClicked = { /* Handle adding a farm here */ },
            onBackClicked = { navController.popBackStack() },
            showAdd = false

        )
        Spacer(modifier = Modifier.height(16.dp))
        SiteForm(navController)
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SiteForm(navController: NavController) {
    val context = LocalContext.current as Activity
    var isImageUploaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

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

        if (name.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }




        return isValid
    }

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    val scrollState = rememberScrollState()
    val permission_granted = stringResource(id = R.string.permission_granted)
    val permission_denied = stringResource(id = R.string.permission_denied)
    val fill_form = stringResource(id = R.string.fill_form)


    var imageInputStream: InputStream? = null
    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val treeUri = result.data?.data

            if (treeUri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)

                // Now, you have permission to write to the selected directory
                val imageFileName = "image${Instant.now().millis}.jpg"

                val selectedDir = DocumentFile.fromTreeUri(context, treeUri)
                val imageFile = selectedDir?.createFile("image/jpeg", imageFileName)

                imageFile?.uri?.let { fileUri ->
                    try {
                        imageInputStream?.use { input ->
                            context.contentResolver.openOutputStream(fileUri)?.use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Update the database with the file path
//                        farmerPhoto = fileUri.toString()
//                        Log.d("farmerphoto", "$farmerPhoto")
                        // Update other fields in the Farm object
                        // Then, insert or update the farm object in your database
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { it ->
        uri?.let { it1 ->
            imageInputStream = context.contentResolver.openInputStream(it1)

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            resultLauncher.launch(intent)
        }
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it)
        {
            Toast.makeText(context, permission_granted, Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        }
        else
        {
            Toast.makeText(context, permission_denied, Toast.LENGTH_SHORT).show()
        }
    }










    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()
    Column(
        modifier = Modifier

            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(state = scrollState)
    ) {
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester1.requestFocus() }
            ),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.site_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp).onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
                        focusRequester1.requestFocus()
                        true
                    }
                    false
                }
        )
//        TextField(
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//            keyboardActions = KeyboardActions(
//                onDone = { focusRequester2.requestFocus() }
//            ),
//            value = village,
//            onValueChange = { village = it },
//            label = { Text(stringResource(id = R.string.village)) },
//            modifier = Modifier
//                .focusRequester(focusRequester1)
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
//        TextField(
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//            keyboardActions = KeyboardActions(
//                onDone = { focusRequester3.requestFocus() }
//            ),
//            value = district,
//            onValueChange = { district = it },
//            label = { Text(stringResource(id = R.string.district)) },
//            modifier = Modifier
//                .focusRequester(focusRequester2)
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
//
//        TextField(
//            singleLine = true,
//            value = size,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//            ),
//            onValueChange = { size = it },
//            label = { Text(stringResource(id = R.string.size_in_hectares)) },
//            modifier = Modifier
//                .focusRequester(focusRequester3)
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the latitude and longitude input fields
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            TextField(
//                readOnly = true,
//                value = latitude,
//                onValueChange = { latitude = it },
//                label = { Text(stringResource(id = R.string.latitude)) },
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(bottom = 16.dp)
//            )
//            Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
//            TextField(
//                readOnly = true,
//                value = longitude,
//                onValueChange = { longitude = it },
//                label = { Text(stringResource(id = R.string.longitude)) },
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(bottom = 16.dp)
//            )
//        }
//        Button(
//            onClick = {
//                // Simulate collecting latitude and longitude
//
//                if (context.hasLocationPermission()) {
//                    fusedLocationClient.lastLocation.addOnSuccessListener { locationResult ->
//                        locationResult?.let { lastLocation ->
//                            latitude = "${lastLocation.latitude}"
//                            longitude = "${lastLocation.longitude}"
//                            Log.d("FARM_LOCATION",mylocation.value)
//                        }
//                    }.addOnFailureListener { e ->
//                        Log.d("LOCATION_ERROR","${e.message}")
//                    }
//                }
//
//            },
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .fillMaxWidth(0.7f)
//                .padding(bottom = 5.dp)
//                .height(50.dp),
//        ) {
//            Text(text = stringResource(id = R.string.get_coordinates))
//        }

        Button(
            onClick = {
                val isValid = validateForm()
                if(isValid){
                    val item = addSite(
                        farmViewModel,
                        name
                    )
                    val returnIntent = Intent()
                    context.setResult(Activity.RESULT_OK, returnIntent)
//                    context.finish()
                    navController.navigate("siteList")
                }
                else {
                    Toast.makeText(context, fill_form, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.add_site))
        }
    }
}

fun addSite(
    farmViewModel: FarmViewModel,
    name: String,


): CollectionSite {
    val site = CollectionSite(
        name,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis
    )
    farmViewModel.addSite(site)
    return site
}