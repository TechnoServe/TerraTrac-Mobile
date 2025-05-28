package org.technoserve.farmcollector.ui.components


import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.technoserve.farmcollector.BuildConfig
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.sync.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Retrofit API Setup
    val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/pdf")
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ApiService::class.java)

    // Document launcher for file selection
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { selectedUri ->
            if (selectedUri != null) {
                coroutineScope.launch {
                    try {
                        val fileUrl = BuildConfig.USER_GUIDE_URL
                            .replace("/edit?usp=sharing", "/export?format=pdf")

                        val response = api.downloadUserGuide(fileUrl)

                        if (response.isSuccessful) {
                            response.body()?.byteStream()?.use { inputStream ->
                                context.contentResolver.openOutputStream(selectedUri)?.use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                } ?: throw Exception("Unable to open output stream.")
                            } ?: throw Exception("Response body is null.")

                            Toast.makeText(
                                context,
                                context.getString(R.string.download_success),
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()
                        } else {
                            throw Exception("HTTP error: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.download_failed, e.localizedMessage),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.no_location_selected),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.user_guide_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },

            )
        }
    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = stringResource(R.string.welcome_message),
//                style = MaterialTheme.typography.bodyMedium,
//                textAlign = TextAlign.Center
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            Button(onClick = {
//                documentLauncher.launch("TerraTrac Mobile Application User Guide.pdf")
//            }) {
//                Icon(
//                    painter = painterResource(id = R.drawable.save),
//                    contentDescription = stringResource(id = R.string.download_user_guide),
//                    tint = MaterialTheme.colorScheme.onPrimary
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = stringResource(R.string.download_user_guide))
//            }
//        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp), // Added more vertical padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome message text with more emphasis
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.bodyLarge, // More prominent style
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground, // Ensures good contrast
                modifier = Modifier.padding(bottom = 32.dp) // Extra padding for spacing
            )

            // User Guide Button with more prominent design
            Button(
                onClick = {
                    documentLauncher.launch("TerraTrac Mobile Application User Guide.pdf")
                },
                modifier = Modifier
                    .fillMaxWidth() // Makes the button expand to full width
                    .height(56.dp), // Consistent height for the button
                shape = MaterialTheme.shapes.medium, // Rounded corners for the button
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Custom background color
                    contentColor = MaterialTheme.colorScheme.onPrimary // Custom text color
                ) // Custom background color
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = stringResource(id = R.string.download_user_guide),
                    tint = MaterialTheme.colorScheme.onPrimary // Makes the icon visible on the button background
                )
                Spacer(modifier = Modifier.width(12.dp)) // Increased spacing between the icon and text
                Text(
                    text = stringResource(R.string.download_user_guide),
                    style = MaterialTheme.typography.bodySmall, // A more appropriate text style for a button
                    color = MaterialTheme.colorScheme.onPrimary // Ensure the text contrasts well
                )
            }
        }


    }
}
