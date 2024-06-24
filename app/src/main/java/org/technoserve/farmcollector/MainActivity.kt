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
    }
}

