package org.technoserve.farmcollector.ui.screens.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.helpers.map.JavaScriptInterface
import org.technoserve.farmcollector.database.helpers.map.LocationHelper
import org.technoserve.farmcollector.database.models.ParcelableFarmData
import org.technoserve.farmcollector.ui.components.InvalidPolygonDialog
import org.technoserve.farmcollector.ui.composes.AreaDialog
import org.technoserve.farmcollector.ui.composes.ConfirmDialog
import org.technoserve.farmcollector.ui.screens.farms.formatInput
import org.technoserve.farmcollector.ui.screens.farms.truncateToDecimalPlaces
import org.technoserve.farmcollector.utils.convertSize
import org.technoserve.farmcollector.utils.isSystemInDarkTheme
import org.technoserve.farmcollector.viewmodels.LanguageViewModel
import org.technoserve.farmcollector.viewmodels.MapViewModel
import java.io.File
import java.net.URLConnection
import java.net.URLEncoder

/**
 * Implementation of caching mechanism for performance
 *
 */




@RequiresApi(Build.VERSION_CODES.M)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPage(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    var backEnabled by remember { mutableStateOf(false) }
    val webViewState = remember { mutableStateOf<WebView?>(null) }  // Store WebView instanc
    // var webView: WebView? = null
    val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)

    // Observe dialog state
    val showDialog by viewModel.showDialog.collectAsState()



//    val showClearMapDialog by viewModel.showClearMapDialog.collectAsState()

    val plotData by viewModel.plotData.collectAsState()
    Log.d("Data in web view page", "Data in web view page: $plotData")

    // Define cache directory
    val cachePath = File(context.cacheDir, "webview-cache")
    if (!cachePath.exists()) {
        cachePath.mkdirs()
    }

    val gson = Gson()
    val farmDataJson = gson.toJson(plotData)
    if (farmDataJson == null) {
        Log.d("JavaScriptInterface", "Farm Data Json is null")
        navController.navigate("addFarm/${plotData?.siteId}")
    }
    val encodedFarmDataJson =
        URLEncoder.encode(farmDataJson, "UTF-8") // Encode J SON to avoid special character issues'

    val calculatedArea: Double = plotData?.size?.toDouble() ?: 0.0

    val enteredArea = sharedPref.getString("plot_size", "0.0")?.toDoubleOrNull() ?: 0.0
    val selectedUnit = sharedPref.getString("selectedUnit", "Ha") ?: "Ha"
    val enteredAreaConverted = convertSize(enteredArea, selectedUnit)


    // Display AreaDialog when triggered
    if (showDialog) {
        AreaDialog(
            showDialog = true,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { chosenArea ->
                val chosenSize =
                    when (chosenArea) {
                        CALCULATED_AREA_OPTION -> calculatedArea.toString()
                        ENTERED_AREA_OPTION -> enteredAreaConverted.toString()
                        else -> throw IllegalArgumentException("Unknown area option: $chosenArea")
                    }
                val truncatedSize = truncateToDecimalPlaces(formatInput(chosenSize), 9)
                sharedPref.edit().putString("plot_size", truncatedSize).apply()
                if (sharedPref.contains("selectedUnit")) {
                    sharedPref.edit().remove("selectedUnit").apply()
                }
                navController.navigate("addFarm/${plotData?.siteId}?plotDataJson=$encodedFarmDataJson")
                viewModel.dismissDialog() // Close the dialog
                //navController.navigateUp()
            },
            calculatedArea = calculatedArea,
            enteredArea = enteredAreaConverted
        )
    }

    println("showclearmap: ${viewModel.showClearMapDialog.value}")


    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Enable JavaScript and required settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setGeolocationEnabled(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    allowFileAccessFromFileURLs = true

                    // Cache settings
                    cacheMode =
                        WebSettings.LOAD_CACHE_ELSE_NETWORK // Use cached resources when available
                    databaseEnabled = true
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String,
                        callback: GeolocationPermissions.Callback
                    ) {
                        callback.invoke(origin, true, false)
                    }
                }

                // Attach JavaScript interface with form data lambdas
                addJavascriptInterface(
                    JavaScriptInterface(
                        context = context,
                        navController = navController,
                        viewModel
                    ),
                    "Android"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url.toString()
                        val cachedResponse = getCachedResponse(context, url)
                        if (cachedResponse != null) {
                            return cachedResponse
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript("if(map){map.invalidateSize(true);}", null)
                    }
                }

                // Load URL with fallback to cached content
                if (isNetworkAvailable(context)) {
                    loadUrl(url)
                } else {
                    val cachedFile = File(cachePath, "${Uri.parse(url).host}.mht")
                    if (cachedFile.exists()) {
                        loadUrl("file://${cachedFile.absolutePath}")
                    } else {
                        loadUrl(url)
                    }
                }

                // Save WebView instance in the remembered state
                webViewState.value = this
                //webView = this
                onWebViewCreated(this) // Pass the WebView instance
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Web View" },
        update = { webView ->
            webView.evaluateJavascript("if(map){map.invalidateSize(true);}", null)
        }
    )

    // Confirm Clear Map Dialog
    if (viewModel.showClearMapDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.set_polygon),
            message = stringResource(id = R.string.clear_map),
            showDialog = viewModel.showClearMapDialog,
            onProceedFn = {
               // viewModel.clearMap(webView)
                viewModel.clearMap(webViewState.value) // Use stored WebView instance
            },
            onCancelFn = {
                viewModel.dismissClearDialog()
            }
        )
    }

    // Confirm dialog for setting the polygon
    if (viewModel.showConfirmDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.set_polygon),
            message = stringResource(id = R.string.confirm_set_polygon),
            showDialog = viewModel.showConfirmDialog,
            onProceedFn = {
                //viewModel.finalizePolygon(mapViewModel)
                // viewModel.confirmFinishPolygon(webView)
                viewModel.confirmFinishPolygon(webViewState.value) // Use stored WebView instance
            },
            onCancelFn = {
                viewModel.dismissConfirmPolygonDialog()
            }
        )
    }


    // Alert dialog for insufficient coordinates
    if (viewModel.showAlertDialog.value) {
        AlertDialog(
            onDismissRequest = {
                viewModel.showAlertDialog.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.insufficient_coordinates_title))
            },
            text = {
                Text(text = stringResource(id = R.string.insufficient_coordinates_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showAlertDialog.value = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.ok))
                    viewModel.showConfirmDialog.value = false
                    viewModel.clearMap(webViewState.value)
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }

   if(viewModel.showInvalidPolygonDialog.value) {
       // Invalid Polygon Dialog
       InvalidPolygonDialog(
           showDialog = viewModel.showInvalidPolygonDialog,
           onDismiss = { viewModel.showInvalidPolygonDialog.value = false }
       )
   }


//    BackHandler(enabled = backEnabled) {
//        webView?.goBack()
//    }

    BackHandler(enabled = backEnabled) {
        webViewState.value?.goBack()
    }
}

// Utility function to check network availability
@RequiresApi(Build.VERSION_CODES.M)
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
}

// Function to get cached response
private fun getCachedResponse(context: Context, url: String): WebResourceResponse? {
    val cache = context.cacheDir
    val cachedFile = File(cache, Uri.parse(url).lastPathSegment ?: return null)

    if (cachedFile.exists()) {
        try {
            val mimeType = URLConnection.guessContentTypeFromName(url)
            return WebResourceResponse(
                mimeType ?: "text/plain",
                "UTF-8",
                cachedFile.inputStream()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewWithVisualization(
    dataJson: String,
    farmId: Long,
    navController: NavController,
    mapViewModel: MapViewModel,
    currentLanguage: String
) {
    val context = LocalContext.current
    var backEnabled by remember { mutableStateOf(false) }
    var webView: WebView? = null

    println("Data JSON: $dataJson")
    println("Farm ID: $farmId")
    println("current Language: $currentLanguage")


    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Enable JavaScript and other settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setGeolocationEnabled(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    databaseEnabled = true
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String,
                        callback: GeolocationPermissions.Callback
                    ) {
                        callback.invoke(origin, true, false)
                    }
                }

                addJavascriptInterface(
                    JavaScriptInterface(
                        context,
                        navController = navController,
                        mapViewModel
                    ), "Android"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        backEnabled = view.canGoBack()
                    }

                }

                // Load the URL
                loadUrl("file:///android_asset/index.html?plotId=${farmId}&lang=${currentLanguage}")
                webView = this
            }
        },
        modifier = Modifier
            .fillMaxSize(),
//            .padding(WindowInsets.systemBars.asPaddingValues()), // Respect safe areas,,
        update = { webView ->
            webView.evaluateJavascript(
                """
                (function() {
                    if (typeof Android !== 'undefined') {
                        const plotJson = Android.getSelectedPlot(${farmId});
                        if (typeof visualizeData === 'function') {
                            visualizeData(plotJson);
                        } else {
                            console.error('visualizeData is not defined');
                        }
                    } else {
                        console.error('Android interface is not available');
                    }
                })();
                """.trimIndent(),
                null
            )
        }
    )

    // Handle back navigation
    BackHandler(enabled = backEnabled) {
        webView?.goBack()
    }
}


@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.M)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlotVisualizationApp(
    navController: NavController,
    viewModel: MapViewModel, siteId: Long, languageViewModel: LanguageViewModel
) {
    val farmData =
        navController.previousBackStackEntry?.arguments?.getParcelable<ParcelableFarmData>("farmData")

    val plotData by viewModel.plotData.collectAsState()

    val farmInfo = farmData?.farm ?: plotData

    var viewSelectFarm by remember { mutableStateOf(false) }
    val mapViewModel: MapViewModel = viewModel()
    var accuracy by remember { mutableStateOf("") }
    val context = LocalContext.current as Activity
    val locationHelper = LocationHelper(context)


    // Observe language from ViewModel (Assuming it's an object)
    val currentLanguageObject by languageViewModel.currentLanguage.collectAsState()

    // Extract only the `code` field (e.g., "es")
    val languageCode = currentLanguageObject.code // Extract only the code part

    // Append language as a query parameter
    val url = "file:///android_asset/leaflet_map.html?siteId=$siteId&lang=$languageCode"

    println("URL Language Code: $languageCode") // This should print "es"



    LaunchedEffect(Unit) {
        mapViewModel.clearCoordinates()

        //  Get the accuracyArrayData from savedStateHandle
        val accuracyArrayData =
            navController.currentBackStackEntry?.savedStateHandle?.get<List<Float?>>("accuracyArray")

        // If the accuracyArrayData exists, clear it
        accuracyArrayData?.let {
            navController.currentBackStackEntry?.savedStateHandle?.set(
                "accuracyArray",
                emptyList<Float?>()
            )
        }
    }

    // First, get initial location if needed
    if (farmInfo == null) {
        locationHelper.getCurrentLocation { location ->
            location?.let {
                accuracy = it.accuracy.toString()
                if (viewModel.state.value.clusterItems.isEmpty()) {
                    viewModel.addCoordinate(it.latitude, it.longitude)
                }
            } ?: run {
                Toast.makeText(
                    context,
                    context.getString(R.string.can_not_get_location),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

// Then start continuous location updates
    locationHelper.requestLocationUpdates(onLocationUpdate = { location ->
        location?.let {
            accuracy = it.accuracy.toString()
        } ?: run {
            Toast.makeText(
                context,
                context.getString(R.string.location_update_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    )

    // Display coordinates of a farm on map
    if ( farmInfo.id != 0L && !viewSelectFarm) {
        viewModel.clearCoordinates()
        if (farmInfo.coordinates?.isNotEmpty() == true) {
            viewModel.addCoordinates(farmInfo.coordinates!!)
        } else if (farmInfo.latitude.isNotEmpty() && farmInfo.longitude.isNotEmpty()) {
            viewModel.addMarker(Pair(farmInfo.latitude.toDouble(), farmInfo.longitude.toDouble()))
        }
        viewSelectFarm = true
    }

    // This will make the status bar visible with a light theme
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons,
            isNavigationBarContrastEnforced = false
        )
        systemUiController.isSystemBarsVisible = true
        onDispose {}
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        // Top Section: Map Visualization
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(
                    when {
                        viewSelectFarm -> 0.65f
                        accuracy.isNotEmpty() -> 0.99f
                        else -> 0.93f
                    }
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Log.d("INFO Visualization", "${farmInfo.coordinates}")
            // Leaflet map
            val farmId = farmInfo.id
            val farmJson = Gson().toJson(farmInfo)
            if (farmId != 0L) {
                WebViewWithVisualization(
                    dataJson = farmJson,
                    farmId = farmId,
                    navController = navController,
                    mapViewModel = viewModel,
                    currentLanguage = languageCode
                )
            } else {
                if(siteId != 0L) {
                    WebViewPage(
//                        url = "file:///android_asset/leaflet_map.html?siteId=${siteId}",
                        url = "file:///android_asset/leaflet_map.html?siteId=$siteId&lang=$languageCode",
                        onWebViewCreated = { webView ->
                            webView.evaluateJavascript(
                                "if (typeof visualizeData === 'function') { visualizeData([]); } else { console.error('visualizeData is not defined'); }",
                                null
                            )
                        },
                        navController = navController,
                        viewModel
                    )
                }
            }

        }


        // Bottom Section: Farm Details
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = if (viewSelectFarm) Arrangement.Center else Arrangement.Start
            ) {
                if (viewSelectFarm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 4.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        farmInfo?.let {
                            // Farm Information Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Farm Info Title
                                    Text(
                                        text = stringResource(id = R.string.farm_info),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    // Divider
                                    Divider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                            )
                                    )

                                    // Farm Details Grid
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp)
                                    ) {
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.farm_name),
                                            value = farmInfo.farmerName
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.member_id),
                                            value = farmInfo.memberId.ifEmpty { "N/A" }
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.district),
                                            value = farmInfo.district
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.village),
                                            value = farmInfo.village
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.longitude),
                                            value = farmInfo.longitude.toDoubleOrNull()
                                                ?.let { String.format("%.6f", it) } ?: "N/A"
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.latitude),
                                            value = farmInfo.latitude.toDoubleOrNull()
                                                ?.let { String.format("%.6f", it) } ?: "N/A"
                                        )
                                        ResponsiveFarmDetailRow(
                                            label = stringResource(id = R.string.size),
                                            value = "${
                                                truncateToDecimalPlaces(
                                                    formatInput(farmInfo.size.toString()),
                                                    4
                                                )
                                            } ${stringResource(id = R.string.ha)}"
                                        )

                                    }
                                }
                            }


                            // Action Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Close Button
                                Button(
                                    onClick = {
                                        viewModel.clearCoordinates()
                                        navController.navigateUp()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 24.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.close),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Update Button
                                Button(
                                    onClick = {
                                        navController.navigate("updateFarm/${farmInfo.id}")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 24.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.update),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}