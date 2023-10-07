package org.technoserve.farmcollector.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import org.technoserve.farmcollector.database.Farm
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream

//data class Farm(val farmerName: String, val village: String, val district: String)

@Composable
fun FarmList(navController: NavController) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    var isDialogVisible by remember { mutableStateOf(false) }
    var selectedFarm: Farm? by remember { mutableStateOf(null) }
//    val farmList = listOf(
//        Farm(farmerName = "Farm A", village = "Village 1", district = "District X"),
//        Farm(farmerName = "Farm B", village = "Village 2", district = "District Y"),
//        Farm(farmerName = "Farm C", village = "Village 3", district = "District Z"),
//        Farm(farmerName = "Farm D", village = "Village 4", district = "District X"),
//        Farm(farmerName = "Farm E", village = "Village 5", district = "District Y"),
//        Farm(farmerName = "Farm F", village = "Village 6", district = "District Z"),
//        Farm(farmerName = "Farm G", village = "Village 7", district = "District X"),
//        Farm(farmerName = "Farm H", village = "Village 8", district = "District Y"),
//        Farm(farmerName = "Farm I", village = "Village 9", district = "District Z"),
//    )
    val listItems by farmViewModel.readAllData.observeAsState(listOf())

    fun refreshListItems() {
        // TODO: update saved predictions list when db gets updated
        //  currently using a terrible makeshift solution
        navController.navigate("home")
        navController.navigate("farmList") {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    if(listItems.size>0){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                FarmListHeader(
                    title = "Farm List",
                    onAddFarmClicked = { navController.navigate("addFarm") },
                    onBackClicked = { navController.navigateUp() },
                    showAdd = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DownloadCsvButton(listItems)
            }
            items(listItems) { farm ->
                FarmCard(farm = farm) {
                    // When a FarmCard is clicked, show the dialog
                    selectedFarm = farm
                    isDialogVisible = true
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


        }
        if (isDialogVisible) {
            FarmDialog(
                farm = selectedFarm,
                onDismiss = {
                    // Dismiss the dialog when needed
                    isDialogVisible = false
                    selectedFarm = null
                }
            )
        }
    }else{


        Column(modifier = Modifier
            .fillMaxSize() ){
            FarmListHeader(
                title = "Farm List",
                onAddFarmClicked = { navController.navigate("addFarm") },
                onBackClicked = { navController.navigateUp() },
                showAdd = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp, 8.dp)

                ,
                painter = painterResource(id = R.drawable.no_data2),
                contentDescription = null
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListHeader(title: String, onAddFarmClicked: () -> Unit, onBackClicked: () -> Unit, showAdd: Boolean) {
    TopAppBar(
//        elevation = 4.dp,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            if(showAdd){
                IconButton(onClick = onAddFarmClicked) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmCard(farm: Farm, onCardClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth() // 90% of the screen width
                .padding(8.dp),
            onClick = {
                onCardClick()
            }
            ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)

            ) {
                Text(
                    text = farm.farmerName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Village: ${farm.village}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "District: ${farm.district}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}



@Composable
fun FarmDialog(farm: Farm?, onDismiss: () -> Unit) {
    if (farm != null) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = farm.farmerName) },
            confirmButton = {
                Button(
                    onClick = { onDismiss() },
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = Color.Blue,
//                        contentColor = Color.White
//                    )
                ) {
                    Text(text = "Close")
                }
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        Image(
                            bitmap = farm.farmerPhoto.asImageBitmap(),
                            contentDescription = "Farmer Photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                        )
                    }
                    Text(text = "Village: ${farm.village}", modifier = Modifier.padding(top=10.dp))
                    Text(text = "District: ${farm.district}")
                    Text(text = "Latitude: ${farm.latitude}")
                    Text(text = "Longitude: ${farm.longitude}")
                    Text(text = "Size: ${farm.size} hectares")
                    Text(text = "Purchases: ${farm.purchases}")
                    // Add more fields as needed
                }
            }
        )
    }
}
fun OutputStream.writeCsv(farms: List<Farm>) {
    val writer = bufferedWriter()
    writer.write(""""Farmer Name", "Village", "District"""")
    writer.newLine()
    farms.forEach {
        writer.write("${it.farmerName}, ${it.village}, \"${it.district}\"")
        writer.newLine()
    }
    writer.flush()
}

@Composable
fun DownloadCsvButton(farms: List<Farm>) {
    val context = LocalContext.current
    val activity = context as Activity


    Button(
        onClick = {

            // Requesting Permission to access External Storage
            // Requesting Permission to access External Storage
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23
            )

            // getExternalStoragePublicDirectory() represents root of external storage, we are using DOWNLOADS
            // We can use following directories: MUSIC, PODCASTS, ALARMS, RINGTONES, NOTIFICATIONS, PICTURES, MOVIES
            val folder: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Storing the data in file with name as geeksData.txt
            val file = File(folder, "farms.csv")
            writeTextData(file, farms, context)
            // displaying a toast message
            Toast.makeText(context, "Data saved publicly..", Toast.LENGTH_SHORT).show()

        },
//        onClick = {
//            ActivityCompat.requestPermissions(
//                activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23
//            )
//            // Create a CSV file and write data to it
//            val filename = "farms.csv"
//            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)
//
//            try {
//                file.outputStream().use {
////                    it.writeCsv(farms) // Ensure writeCsv function is implemented correctly
//                }
//                // File was successfully written
//            } catch (e: IOException) {
//                // Handle file writing errors here
//                Log.d("saving",e.message.toString())
//            }
//
//            // Create an intent to send the file
//
//            val csvURI: Uri = context.let {
//                    FileProvider.getUriForFile(
//                        it,
//                        context.applicationContext.packageName.toString() + ".provider",
//                        file
//                    )
//            }
//            val content = getContentFromUri(csvURI, context)
//            Log.d("FileContent", "Content of $csvURI:\n$content")
//            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.type = "text/csv"
//            intent.putExtra(Intent.EXTRA_SUBJECT, "Farm Data")
//            intent.putExtra(Intent.EXTRA_STREAM, csvURI)
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            context.startActivity(Intent.createChooser(intent, "Send CSV"))
//        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Download CSV")
    }
}

//fun getContentFromUri(uri: Uri, context: Context): String {
//    val inputStream = context.contentResolver.openInputStream(uri)
//    val reader = BufferedReader(InputStreamReader(inputStream))
//    val stringBuilder = StringBuilder()
//    var line: String?
//
//    try {
//        while (reader.readLine().also { line = it } != null) {
//            stringBuilder.append(line).append("\n")
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } finally {
//        inputStream?.close()
//    }
//
//    return stringBuilder.toString()
//}





// on below line creating a method to write data to txt file.
private fun writeTextData(file: File, farms: List<Farm>, context: Context) {
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(file)

        fileOutputStream
            .write(""""Farmer Name", "Village", "District"""".toByteArray())
        fileOutputStream.write(10);
        farms.forEach {
            fileOutputStream.write("${it.farmerName}, ${it.village}, \"${it.district}\"".toByteArray())
            fileOutputStream.write(10);
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}