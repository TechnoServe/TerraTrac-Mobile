package org.technoserve.farmcollector

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codingwithmitch.composegooglemaps.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.ui.screens.AddFarm
import org.technoserve.farmcollector.ui.screens.AddSite
import org.technoserve.farmcollector.ui.screens.CollectionSiteList
import org.technoserve.farmcollector.ui.screens.FarmList
import org.technoserve.farmcollector.ui.screens.Home
import org.technoserve.farmcollector.ui.screens.SetPolygon
import org.technoserve.farmcollector.ui.screens.UpdateFarmForm
import org.technoserve.farmcollector.ui.theme.FarmCollectorTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            FarmCollectorTheme {
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
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val farmViewModel: FarmViewModel = viewModel(
                        factory = FarmViewModelFactory(context.applicationContext as Application)
                    )
                    val listItems by farmViewModel.readData.observeAsState(listOf())
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            Home(navController)
                        }
                        composable("siteList") {
                            CollectionSiteList(navController)
                        }
                        composable("farmList/{siteId}") { backStackEntry ->
                            val siteId = backStackEntry.arguments?.getString("siteId")
                            if (siteId != null) {
                                FarmList(navController = navController, siteId = siteId.toLong())
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
                        composable("setPolygon")
                        {
                            SetPolygon(navController, viewModel)
                        }
                    }
                }
            }
        }
    }
}

