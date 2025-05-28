package org.technoserve.farmcollector.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import org.technoserve.farmcollector.R


@RequiresApi(Build.VERSION_CODES.M)
fun isConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
}

fun getUserLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return null // Permissions are not granted
    }

    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
}

//@RequiresApi(Build.VERSION_CODES.M)
//fun cacheMapInBackground(context: Context, mapUrl: String) {
//    val sharedPreferences: SharedPreferences =
//        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
//    val isMapCached = sharedPreferences.getBoolean("MapCached", false)
//
//    if (!isMapCached) {
//        if (isConnected(context)) {
//            // Create a WebView for caching
//            val webView = WebView(context).apply {
//                settings.javaScriptEnabled = true
//                settings.domStorageEnabled = true
//                settings.cacheMode = WebSettings.LOAD_DEFAULT
//                webViewClient = object : WebViewClient() {
//                    override fun onPageFinished(view: WebView?, url: String?) {
//                        super.onPageFinished(view, url)
//
//                        // Mark map caching as complete
//                        sharedPreferences.edit().putBoolean("MapCached", true).apply()
//                        Toast.makeText(context, "Map tiles cached successfully.", Toast.LENGTH_LONG).show()
//                        println("Map tiles cached successfully.")
//                        // Destroy the WebView to free resources
//                        destroy()
//                    }
//                }
//
//                // Load the map URL in the WebView
//                loadUrl(mapUrl)
//            }
//        } else {
//            Toast.makeText(context, "No internet connection. Map caching skipped.", Toast.LENGTH_LONG).show()
//        }
//    }
//}


@RequiresApi(Build.VERSION_CODES.M)
fun cacheMapInBackground(context: Context, mapUrl: String) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val isMapCached = sharedPreferences.getBoolean("MapCached", false)

    if (!isMapCached) {
        if (isConnected(context)) {
            val location = getUserLocation(context)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // Modify the map URL to center it on the user's location
                val centeredMapUrl = "$mapUrl?center=$latitude,$longitude&zoom=14"

                // Create a WebView for caching
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.allowFileAccess = true // Enable access to cached files

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // Mark map caching as complete
                            sharedPreferences.edit().putBoolean("MapCached", true).apply()
                            Toast.makeText(
                                context,
                                context.getString(R.string.map_tiles_cached),
                                Toast.LENGTH_LONG
                            ).show()
                            println("Map tiles cached successfully.")
                            // Destroy the WebView to free resources
                            destroy()
                        }
                    }

                    // Load the centered map URL in the WebView
                    loadUrl(centeredMapUrl)
                }
            } else {
                Toast.makeText(context, context.getString(R.string.unable_to_get_location), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_LONG).show()
        }
    }
}