package org.technoserve.farmcollector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.technoserve.farmcollector.viewmodels.AppUpdateViewModel
//import org.technoserve.farmcollector.viewmodels.ExitConfirmationDialog
import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory
import org.technoserve.farmcollector.viewmodels.UpdateAlert
import org.technoserve.farmcollector.database.helpers.map.LocationHelper
import org.technoserve.farmcollector.viewmodels.MapViewModel
import org.technoserve.farmcollector.ui.screens.farms.AddFarm
import org.technoserve.farmcollector.ui.screens.collectionsites.AddSite
import org.technoserve.farmcollector.ui.screens.collectionsites.CollectionSiteList
import org.technoserve.farmcollector.ui.screens.farms.FarmList
import org.technoserve.farmcollector.ui.screens.home.Home
import org.technoserve.farmcollector.ui.screens.settings.ScreenWithSidebar
import org.technoserve.farmcollector.ui.screens.map.SetPolygon
import org.technoserve.farmcollector.ui.screens.settings.SettingsScreen
import org.technoserve.farmcollector.ui.screens.farms.UpdateFarmForm
import org.technoserve.farmcollector.ui.theme.FarmCollectorTheme
import org.technoserve.farmcollector.viewmodels.LanguageViewModel
import org.technoserve.farmcollector.viewmodels.LanguageViewModelFactory
import java.util.Locale


/**
 *
 * Constants for navigation routes. These are used to define the different screens in the app.
 *
 * For example, if you have a "Site" model with an "id" field, you could define routes like:
 *
 * const val SITE_LIST = "siteList"
 * const val FARM_LIST = "farmList/{siteId}"
 * const val ADD_FARM = "addFarm/{siteId}"
 * const val ADD_SITE = "addSite"
 * const val UPDATE_FARM = "updateFarm/{farmId}"
 *
 */
object Routes {
    const val HOME = "home"
    const val SITE_LIST = "siteList"
    const val FARM_LIST = "farmList/{siteId}"
    const val ADD_FARM = "addFarm/{siteId}"
    const val ADD_SITE = "addSite"
    const val UPDATE_FARM = "updateFarm/{farmId}"
    const val SET_POLYGON = "setPolygon"
    const val SETTINGS = "settings"
}

/**
 * MainActivity is the entry point for the Android app. It sets up the navigation graph,
 * manages permissions, and initializes the language and map view models.
 *
 */
class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper
    private val viewModel: MapViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels {
        LanguageViewModelFactory(application)
    }
    private val sharedPreferences by lazy {
        getSharedPreferences("theme_mode", MODE_PRIVATE)
    }
    private val sharedPref by lazy {
        getSharedPreferences("FarmCollector", MODE_PRIVATE)
    }


    @SuppressLint("InlinedApi")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this)

        val darkMode = mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))

        // Apply the selected theme
        if (darkMode.value) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // remove plot_size from shared preferences if it exists
        if (sharedPref.contains("plot_size")) {
            sharedPref.edit().remove("plot_size").apply()
        }
        // remove selected unit from shared preferences if it exists
        if (sharedPref.contains("selectedUnit")) {
            sharedPref.edit().remove("selectedUnit").apply()
        }

        // Apply language preference when the activity starts
        applyLanguagePreference()

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            var canExitApp by remember { mutableStateOf(false) }
//            var showExitToast by remember { mutableStateOf(false) }

            val currentLanguage by languageViewModel.currentLanguage.collectAsState()

            val appUpdateViewModel: AppUpdateViewModel = viewModel()
            val updateAvailable by appUpdateViewModel.updateAvailable.collectAsState()

           //  var showExitDialog by remember { mutableStateOf(false) }


            // Initialize update check
            LaunchedEffect(Unit) {
                appUpdateViewModel.initializeAppUpdateCheck(context as Activity)
            }


//            // Handle back press
//            BackHandler {
//                if (canExitApp) {
//                    // Exit the app
//                    (context as? Activity)?.finish()
//                } else {
//                    showExitToast = true
//                    canExitApp = true
//                }
//            }
//
//
//            // Show exit toast with delay for reset
//            if (showExitToast) {
//                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
//
//                // Delay resetting `showExitToast` and `canExitApp`
//                LaunchedEffect(showExitToast) {
//                    kotlinx.coroutines.delay(2000) // 2 seconds delay
//                    showExitToast = false
//                    canExitApp = false
//                }
//            }



            // Update Alert
            UpdateAlert(
                showDialog = updateAvailable,
                onDismiss = { /* Cannot dismiss forced update */ },
                onConfirm = {
                    // Open Play Store
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                    })
                }
            )

//            // Exit Confirmation
//            ExitConfirmationDialog(
//                showDialog = showExitDialog,
//                onDismiss = { showExitDialog = false },
//                onConfirm = { (context as? Activity)?.finish() }
//            )



            LaunchedEffect(currentLanguage) {
                languageViewModel.updateLocale(context = applicationContext, Locale(currentLanguage.code))
            }

            FarmCollectorTheme(darkTheme = darkMode.value) {
                val multiplePermissionsState =
                    rememberMultiplePermissionsState(
                        listOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ),
                    )
                LaunchedEffect(true) {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
//                    val languages = getLocalizedLanguages(applicationContext)
                    val languages = languageViewModel.languages
                    val farmViewModel: FarmViewModel =
                        viewModel(
                            factory = FarmViewModelFactory(applicationContext as Application),
                        )
                    val listItems by farmViewModel.readData.observeAsState(listOf())
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                    ) {
//                        composable(Routes.HOME) {
//                            BackHandler(enabled = canExitApp) {
//                                (context as? Activity)?.finish()
//                            }
//                            LaunchedEffect(Unit) {
//                                canExitApp = true
//                            }
//                            Home(navController, languageViewModel, languages)
//                        }
                        composable(Routes.HOME) {
                            //val context = LocalContext.current
                            // var canExitApp by remember { mutableStateOf(false) }
                            var showExitToast by remember { mutableStateOf(false) }

                            // Handle back press with confirmation
                            BackHandler {
                                if (canExitApp) {
                                    // Exit the app
                                    (context as? Activity)?.finish()
                                } else {
                                    showExitToast = true
                                    canExitApp = true
                                }
                            }

                            // Show exit toast and reset the state after a delay
                            if (showExitToast) {
                                // Show the toast
                                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()

                                // Reset `showExitToast` and `canExitApp` after 2 seconds
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(2000) // 2 seconds delay
                                    showExitToast = false
                                    canExitApp = false
                                }
                            }

                            // Display Home Screen
                            Home(navController, languageViewModel, languages)
                        }

                        composable(Routes.SITE_LIST) {
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            ScreenWithSidebar(navController) {
                                CollectionSiteList(navController)
                            }
                        }
                        composable(Routes.FARM_LIST) { backStackEntry ->
                            val siteId = backStackEntry.arguments?.getString("siteId")
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            if (siteId != null) {
                                ScreenWithSidebar(navController) {
                                    FarmList(
                                        navController = navController,
                                        siteId = siteId.toLong(),
                                    )
                                }
                            }
                        }
                        composable(Routes.ADD_FARM) { backStackEntry ->
                            val siteId = backStackEntry.arguments?.getString("siteId")
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            if (siteId != null) {
                                AddFarm(navController = navController, siteId = siteId.toLong())
                            }
                        }
                        composable(Routes.ADD_SITE) {
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            AddSite(navController)
                        }
                        composable(Routes.UPDATE_FARM) { backStackEntry ->
                            val farmId = backStackEntry.arguments?.getString("farmId")
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            if (farmId != null) {
                                UpdateFarmForm(
                                    navController = navController,
                                    farmId = farmId.toLong(),
                                    listItems = listItems,
                                )
                            }
                        }

                        composable(Routes.SET_POLYGON,
                            arguments = listOf(
                                navArgument("coordinates") { type = NavType.StringType },
                                navArgument("accuracyArray") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            SetPolygon(navController, viewModel)
                        }
                        composable(Routes.SETTINGS) {
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            SettingsScreen(
                                navController,
                                darkMode,
                                languageViewModel,
                                languages,
                            )
                        }
                    }
                }
            }

        }
    }

    private fun applyLanguagePreference() {
        // Get the preferred language from shared preferences
        val savedLanguageCode = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("preferred_language", Locale.getDefault().language)

        val preferredLanguage = languageViewModel.languages .find { it.code == savedLanguageCode }
            ?: languageViewModel.languages.first()

        // Update the locale using the LanguageViewModel
        languageViewModel.selectLanguage(preferredLanguage, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.cleanup()
    }
}
