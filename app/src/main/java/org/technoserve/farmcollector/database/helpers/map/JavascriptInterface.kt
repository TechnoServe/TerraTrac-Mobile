package org.technoserve.farmcollector.database.helpers.map

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.gson.Gson
import org.json.JSONObject
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.composes.ConfirmDialog
import org.technoserve.farmcollector.utils.convertSize
import org.technoserve.farmcollector.viewmodels.MapViewModel
import java.net.URLEncoder
import java.util.UUID

/**
 * This class provides a JavaScript interface for the WebView
 */
class JavaScriptInterface(
    private val context: Context,
    private val navController: NavController,
    private val mapViewModel: MapViewModel
) {
    fun parseCoordinates(coordinatesString: String): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        val cleanedString = coordinatesString.trim().removeSurrounding("\"", "").replace(" ", "")

        if (cleanedString.isNotEmpty()) {
            // Check if the coordinates are in polygon or point format
            val isPolygon = cleanedString.startsWith("[[") && cleanedString.endsWith("]]")
            val isPoint = cleanedString.startsWith("[") && cleanedString.endsWith("]") && !isPolygon

            if (isPolygon) {
                // Handle Polygon Format
                val pairs =
                    cleanedString
                        .removePrefix("[[")
                        .removeSuffix("]]")
                        .split("],[")
                        .map { it.split(",") }
                for (pair in pairs) {
                    if (pair.size == 2) {
                        try {
                            val lat = pair[0].toDouble()
                            val lon = pair[1].toDouble()
                            result.add(Pair(lat, lon))
                        } catch (e: NumberFormatException) {
                            println("Error parsing polygon coordinate pair: ${pair.joinToString(",")}")
                        }
                    }
                }
            } else if (isPoint) {
                // Handle Point Format
                val coords = cleanedString.removePrefix("[").removeSuffix("]").split(", ")
                if (coords.size == 2) {
                    try {
                        val lat = coords[1].toDouble()
                        val lon = coords[0].toDouble()
                        result.add(Pair(lat, lon))
                    } catch (e: NumberFormatException) {
                        println("Error parsing point coordinate pair: ${coords.joinToString(",")}")
                    }
                }
            } else {
                println("Unrecognized coordinates format: $coordinatesString")
            }
        }
        return result
    }

    @JavascriptInterface
    fun receivePlotData(plotDataJson: String) {
        Log.d("JavaScriptInterface", "Received Plot Data: $plotDataJson")
        val sharedPref = context.getSharedPreferences("FarmCollector", Context.MODE_PRIVATE)

        try {
            // Parse the JSON string into a JSONObject
            val jsonObject = JSONObject(plotDataJson)

            // Extract the coordinates as a string
            val coordinatesArray = jsonObject.getJSONArray("coordinates")
            val coordinatesString = coordinatesArray.toString()

            // Use the parseCoordinates function to parse the coordinates
            val parsedCoordinates = parseCoordinates(coordinatesString)

            // Map the parsed data to the Farm object
            val farmData = Farm(
                siteId = jsonObject.getLong("siteId"),
                remoteId = jsonObject.optString("remoteId").takeIf { it.isNotBlank() }?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
                farmerPhoto = jsonObject.getString("farmerPhoto"),
                farmerName = jsonObject.getString("farmerName")?: "Farmer",
                memberId = jsonObject.getString("memberId"),
                village = jsonObject.getString("village"),
                district = jsonObject.getString("district"),
                purchases = jsonObject.optString("purchases")?.toFloatOrNull(),
                size = jsonObject.getDouble("size").toFloat(),
                latitude = jsonObject.getString("latitude"),
                longitude = jsonObject.getString("longitude"),
                coordinates = parsedCoordinates,
                accuracyArray = jsonObject.optJSONArray("accuracyArray")?.let { array ->
                    List(array.length()) { i -> array.getDouble(i).toFloat() }
                },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis() ,
                synced = false,
                scheduledForSync = false,
                needsUpdate = false
            )

            Log.d("JavaScriptInterface", "Parsed Plot Data: $farmData")

//            mapViewModel.updatePlotData(farmData)
            mapViewModel.updatePlotData(
                siteId = farmData.siteId,
                coordinates = parsedCoordinates,
                latitude = farmData.latitude,
                longitude = farmData.longitude,
                size = farmData.size,
                accuracyArray = farmData.accuracyArray as List<Float>?
            )


            val gson = Gson()
            val farmDataJson = gson.toJson(farmData)
            if(farmDataJson == null){
                Log.d("JavaScriptInterface", "Farm Data Json is null")
                navController.navigate("addFarm/${farmData.siteId}")
            }
            val encodedFarmDataJson = URLEncoder.encode(farmDataJson, "UTF-8") // Encode J SON to avoid special character issues'

            val calculatedArea: Double = farmData.size?.toDouble() ?: 0.0
            val enteredArea = sharedPref.getString("plot_size", "0.0")?.toDoubleOrNull() ?: 0.0
            val selectedUnit = sharedPref.getString("selectedUnit", "Ha") ?: "Ha"
            val enteredAreaConverted = convertSize(enteredArea, selectedUnit)

            // Navigate to the AddFarm composable on the main thread
            Handler(Looper.getMainLooper()).post {
                mapViewModel.showAreaDialog(calculatedArea = calculatedArea.toString(), enteredArea = enteredAreaConverted.toString())// Update the dialog state in the ViewModel
            }

        } catch (e: Exception) {
            Log.e("JavaScriptInterface", "Error parsing plot data: ${e.message}", e)
        }
    }





    @JavascriptInterface
    fun getPlots(): String {
        return Gson().toJson(AppDatabase.getInstance(context).farmsDAO().getAllFarms())
    }

    fun parseCoordinatesVisualize(coordinatesString: String): List<Pair<Double, Double>> {
        if (coordinatesString.isEmpty()) return emptyList()

        // Regex to extract (lat, lng) pairs
        val regex = "\\(([^,]+), ([^\\)]+)\\)".toRegex()
        val matches = regex.findAll(coordinatesString)

        // Convert matches to List<Pair<Double, Double>>
        return matches.map { match ->
            val lat = match.groupValues[1].toDouble()
            val lng = match.groupValues[2].toDouble()
            Pair(lat, lng)
        }.toList()
    }

    @JavascriptInterface
    fun getSelectedPlot(id: Long): String {
        val farm = AppDatabase.getInstance(context).farmsDAO().getFarm(id)
        Log.d("Selected farm", "getSelectedPlot: $farm")

        if (farm != null) {

            // Determine coordinates
            val coordinates = farm.coordinates?.toString()?.takeIf { it.isNotEmpty() }
                ?: "[${farm.latitude}, ${farm.longitude}]" // Use latitude and longitude if coordinates are null

            // Parse coordinates
            val parsedCoordinates = parseCoordinatesVisualize(coordinates)

            Log.d("Selected Coordinates", "getSelectedPlot: $parsedCoordinates")

            // Create a new farm object with parsed coordinates
            val updatedFarm = farm.copy(coordinates = parsedCoordinates)

            // Return JSON representation
            return Gson().toJson(updatedFarm)
        } else {
            return "{}" // Return an empty JSON object if no farm is found
        }
    }


    @JavascriptInterface
    fun showClearMapDialog() {
        Handler(Looper.getMainLooper()).post {
            mapViewModel.showClearDialog()
        }
    }

    @JavascriptInterface
    fun showConfirmPolygonDialog() {
        Log.d("JavaScriptInterface", "showConfirmPolygonDialog triggered")
        Handler(Looper.getMainLooper()).post {
            mapViewModel.showConfirmPolygonDialog()
            Log.d("MapViewModel", "showConfirmPolygonDialog() executed in ViewModel")
        }
    }

    @JavascriptInterface
    fun showInsufficientPointsDialog() {
        Handler(Looper.getMainLooper()).post {
            mapViewModel.showAlertDialog()
        }
    }

    @JavascriptInterface
    fun showPolygonTooSmallDialog() {
        Handler(Looper.getMainLooper()).post {
            mapViewModel.showInvalidPolygonDialog()
        }
    }


}