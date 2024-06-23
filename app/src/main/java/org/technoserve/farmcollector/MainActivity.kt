package org.technoserve.farmcollector

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.database.sync.SyncService
import org.technoserve.farmcollector.database.sync.SyncWorker
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.ui.screens.AddFarm
import org.technoserve.farmcollector.ui.screens.AddSite
import org.technoserve.farmcollector.ui.screens.CollectionSiteList
import org.technoserve.farmcollector.ui.screens.FarmList
import org.technoserve.farmcollector.ui.screens.Home
import org.technoserve.farmcollector.ui.screens.ScreenWithSidebar
import org.technoserve.farmcollector.ui.screens.SetPolygon
import org.technoserve.farmcollector.ui.screens.SettingsScreen
import org.technoserve.farmcollector.ui.screens.UpdateFarmForm
import org.technoserve.farmcollector.ui.theme.FarmCollectorTheme
import org.technoserve.farmcollector.utils.LanguageViewModel
import org.technoserve.farmcollector.utils.LanguageViewModelFactory
import org.technoserve.farmcollector.utils.getLocalizedLanguages
import org.technoserve.farmcollector.utils.updateLocale
import java.util.Locale
import java.util.concurrent.TimeUnit

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with the action that requires permission.
            // showSyncNotification()

            // Start the service
            val serviceIntent = Intent(this, SyncService::class.java)
            startService(serviceIntent)

        } else {
            // Permission is denied. Handle the case where the user denies the permission.
        }
    }

    private val viewModel: MapViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels {
        LanguageViewModelFactory(application)
    }
    private val sharedPreferences by lazy {
        getSharedPreferences("theme_mode", MODE_PRIVATE)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val darkMode = mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))

        // Apply the selected theme
        if (darkMode.value) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContent {
            val navController = rememberNavController()
            val currentLanguage by languageViewModel.currentLanguage.collectAsState()

            LaunchedEffect(currentLanguage) {
                updateLocale(context = applicationContext, Locale(currentLanguage.code))
            }

            FarmCollectorTheme(darkTheme = darkMode.value) {
                val multiplePermissionsState = rememberMultiplePermissionsState(
                    listOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                LaunchedEffect(true) {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val languages = getLocalizedLanguages(applicationContext)
                    val farmViewModel: FarmViewModel = viewModel(
                        factory = FarmViewModelFactory(applicationContext as Application)
                    )
                    val listItems by farmViewModel.readData.observeAsState(listOf())
                    NavHost(
                        navController = navController, startDestination = "home"
                    ) {
                        composable("home") {
                            Home(navController, languageViewModel, languages)
                        }
                        composable("siteList") {
                            ScreenWithSidebar(navController) {
                                CollectionSiteList(navController)
                            }
                        }
                        composable("farmList/{siteId}") { backStackEntry ->
                            val siteId = backStackEntry.arguments?.getString("siteId")
                            if (siteId != null) {
                                ScreenWithSidebar(navController) {
                                    FarmList(
                                        navController = navController, siteId = siteId.toLong()
                                    )
                                }
                            }
                        }
                        composable("addFarm/{siteId}") { backStackEntry ->
                            val siteId = backStackEntry.arguments?.getString("siteId")
                            if (siteId != null) {
                                AddFarm(navController = navController, siteId = siteId.toLong())
                            }
                        }
                        composable("addSite") {
                            AddSite(navController)
                        }

                        composable("updateFarm/{farmId}") { backStackEntry ->
                            val farmId = backStackEntry.arguments?.getString("farmId")
                            if (farmId != null) {
                                UpdateFarmForm(
                                    navController = navController,
                                    farmId = farmId.toLong(),
                                    listItems = listItems
                                )
                            }
                        }
                        // Screen for displaying and setting farm polygon coordinates
                        composable(
                            "setPolygon", arguments = listOf(navArgument("coordinates") {
                                type = NavType.StringType
                            })
                        ) {
                            SetPolygon(navController, viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(
                                navController,
                                darkMode,
                                languageViewModel,
                                languages
                            )
                        }
                    }
                }
            }
        }

        // Request notification permission if needed and show notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
//                    showSyncNotification()
                    // Start the service
                    val serviceIntent = Intent(this, SyncService::class.java)
                    startService(serviceIntent)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show an educational UI to explain why the permission is needed
                    // Then request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, no need to request permission
//            showSyncNotification()
        }


        // Simulate sync completion after 10 seconds for demo purposes
//        handler.postDelayed({
//            createNotificationChannelAndShowCompleteNotification()
//        },1000)
    }



//    override fun onStart() {
//        super.onStart()
//        startSyncWork()
//        // Simulate sync completion after 10 seconds for demo purposes
//        handler.postDelayed({
//            createNotificationChannelAndShowCompleteNotification()
//        },1000)
//    }



//    private val syncWorkTag = "sync_work_tag"
//    private fun startSyncWork() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        // Create a periodic work request to sync data every 5 minutes
//        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
//            .setConstraints(constraints)
//            .addTag(syncWorkTag)
//            .build()
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            syncWorkTag,
//            ExistingPeriodicWorkPolicy.REPLACE,  // Keep existing work and do not replace
//            syncRequest
//        )
//    }

    // Adding Elapsed time in notification

    private lateinit var handler: Handler
    private lateinit var updateRunnable: Runnable
    private var startTime: Long = 0

    private fun showSyncNotification() {
        val builder = NotificationCompat.Builder(this, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync)
            .setContentTitle("Sync Data in Progress")
            .setContentText("Synchronizing Farms Data with the server.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }

        // Initialize the start time and handler
        startTime = System.currentTimeMillis()
        handler = Handler(mainLooper)

        // Define the updateRunnable
        updateRunnable = object : Runnable {
            override fun run() {
                // Calculate elapsed time
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                // Update the notification content
                val timeText = String.format("Elapsed time: %02d:%02d:%02d", hours, minutes, seconds)
                builder.setContentText(timeText)

                // Notify the updated notification
                with(NotificationManagerCompat.from(this@MainActivity)) {
                    notify(1, builder.build())
                }

                // Re-run the handler every second
                handler.postDelayed(this, 1000)
            }
        }

        // Start the handler to update the notification
        handler.post(updateRunnable)
    }

    private fun createNotificationChannelAndShowCompleteNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sync Channel"
            val descriptionText = "Channel for sync notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("SYNC_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, "SYNC_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_sync_complete)
            .setContentTitle("Sync Complete")
            .setContentText("Farms have been successfully synchronized with the server.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(2, builder.build())
        }

        // Stop the handler when sync is complete
        if (::handler.isInitialized && ::updateRunnable.isInitialized) {
            handler.removeCallbacks(updateRunnable)
        }
    }

}

