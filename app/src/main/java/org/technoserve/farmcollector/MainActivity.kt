package org.technoserve.farmcollector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.map.MapViewModel
import org.technoserve.farmcollector.ui.screens.AddFarm
import org.technoserve.farmcollector.ui.screens.AddSite
import org.technoserve.farmcollector.ui.screens.FarmList
import org.technoserve.farmcollector.ui.screens.Home
import org.technoserve.farmcollector.ui.screens.ScreenWithSidebar
import org.technoserve.farmcollector.ui.screens.SetPolygon
import org.technoserve.farmcollector.ui.screens.SettingsScreen
import org.technoserve.farmcollector.ui.screens.UpdateFarmForm
import org.technoserve.farmcollector.ui.screens.CollectionSiteList
import org.technoserve.farmcollector.ui.theme.FarmCollectorTheme
import org.technoserve.farmcollector.utils.LanguageViewModel
import org.technoserve.farmcollector.utils.LanguageViewModelFactory
import org.technoserve.farmcollector.utils.getLocalizedLanguages
import org.technoserve.farmcollector.utils.updateLocale
import java.util.Locale


// Constants for navigation routes
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

// @AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

        setContent {
            val navController = rememberNavController()
            var context = LocalContext.current
            var canExitApp by remember { mutableStateOf(false) }
            val currentLanguage by languageViewModel.currentLanguage.collectAsState()

            LaunchedEffect(currentLanguage) {
                updateLocale(context = applicationContext, Locale(currentLanguage.code))
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
                    val languages = getLocalizedLanguages(applicationContext)
                    val farmViewModel: FarmViewModel =
                        viewModel(
                            factory = FarmViewModelFactory(applicationContext as Application),
                        )
                    val listItems by farmViewModel.readData.observeAsState(listOf())
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                    ) {
                        composable(Routes.HOME) {
                            BackHandler(enabled = canExitApp) {
                                (context as? Activity)?.finish()
                            }
                            LaunchedEffect(Unit) {
                                canExitApp = true
                            }
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
                                navArgument("coordinates") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            LaunchedEffect(Unit) {
                                canExitApp = false
                            }
                            val coordinates = backStackEntry.arguments?.getString("coordinates")
                            SetPolygon(navController, viewModel,)
                        }
                        composable(Routes.SETTINGS,) {
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
}
