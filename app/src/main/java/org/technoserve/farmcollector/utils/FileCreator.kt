package org.technoserve.farmcollector.utils

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.screens.farms.siteID

import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 *
 *  This function is used to create a CSV/GeoJson file with the given data
 *  and share it with the user
 *
 *  @param context The application context
 *  @param uri The Uri to save the CSV file to
 */

fun createFile(
    context: Context,
    uri: Uri,
    listItems: List<Farm>,
    exportFormat: String,
    siteID : Long,
    cwsListItems: List<CollectionSite>

): Boolean {

    val getSiteById = cwsListItems.find { it.siteId == siteID }
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                if (exportFormat == "CSV") {
                    writer.write(
                        "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,accuracyArray,created_at,updated_at\n",
                    )
                    listItems.forEach { farm ->
                        val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                        val matches = regex.findAll(farm.coordinates.toString())
                        val reversedCoordinates =
                            matches
                                .map { match ->
                                    val (lat, lon) = match.destructured
                                    "[$lon, $lat]"
                                }.toList()
                                .let { coordinates ->
                                    if (coordinates.isNotEmpty()) {
                                        // Always include brackets, even for a single point
                                        coordinates.joinToString(
                                            ", ",
                                            prefix = "[",
                                            postfix = "]"
                                        )
                                    } else {
                                        ""
                                    }
                                }

                        val line =
                            "${farm.remoteId},\"${
                                farm.farmerName.split(" ").joinToString(" ")
                            }\",${farm.memberId},${getSiteById?.name},\"${getSiteById?.agentName}\",\"${farm.village}\",\"${farm.district}\",${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",\"${farm.accuracyArray}\",${
                                Date(farm.createdAt)
                            },${Date(farm.updatedAt)}\n"
                        writer.write(line)
                    }
                } else {
                    val geoJson =
                        buildString {
                            append("{\"type\": \"FeatureCollection\", \"features\": [")
                            listItems.forEachIndexed { index, farm ->
                                val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                                val matches = regex.findAll(farm.coordinates.toString())
                                val geoJsonCoordinates =
                                    matches
                                        .map { match ->
                                            val (lat, lon) = match.destructured
                                            "[$lon, $lat]"
                                        }.joinToString(", ", prefix = "[", postfix = "]")
                                // Ensure latitude and longitude are not null
                                val latitude =
                                    farm.latitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0
                                val longitude =
                                    farm.longitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0

                                val feature =
                                    """
                                        {
                                            "type": "Feature",
                                            "properties": {
                                                "remote_id": "${farm.remoteId}",
                                                "farmer_name": "${
                                        farm.farmerName.split(" ").joinToString(" ")
                                    }",
                                                "member_id": "${farm.memberId}",
                                                "collection_site": "${getSiteById?.name ?: ""}",
                                                "agent_name": "${getSiteById?.agentName ?: ""}",
                                                "farm_village": "${farm.village}",
                                                "farm_district": "${farm.district}",
                                                 "farm_size": ${farm.size},
                                                "latitude": $latitude,
                                                "longitude": $longitude,
                                                "accuracyArray": "${farm.accuracyArray ?: ""}",
                                                "created_at": "${farm.createdAt.let { Date(it) }}",
                                                "updated_at": "${farm.updatedAt.let { Date(it) }}"
                                                
                                            },
                                            "geometry": {
                                                "type": "${if ((farm.coordinates?.size ?: 0) > 1) "Polygon" else "Point"}",
                                                 "coordinates": ${if ((farm.coordinates?.size ?: 0) > 1) "[$geoJsonCoordinates]" else "[$latitude, $longitude]"}
                                            }
                                        }
                                        """.trimIndent()
                                append(feature)
                                if (index < listItems.size - 1) append(",")
                            }
                            append("]}")
                        }
                    writer.write(geoJson)
                }
            }
        }
        return true
    } catch (e: IOException) {
        Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
        return false
    }
}

/**
 *
 *  This function is used to create a CSV/GeoJson file with the given data
 *  and share it with the user
 *
 *  @param context The application context
 *  @param uri The Uri to save the CSV file to
 */
fun createFileForSharing(
    context: Context,
    listItems: List<Farm>,
    exportFormat: String,
    siteID : Long,
    cwsListItems: List<CollectionSite>
): File? {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val getSiteById = cwsListItems.find { it.siteId == siteID }
    val siteName = getSiteById?.name ?: "SiteName"
    val filename =
        if (exportFormat == "CSV") "farms_${siteName}_$timestamp.csv" else "farms_${siteName}_$timestamp.geojson"
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, filename)

    try {
        file.bufferedWriter().use { writer ->
            if (exportFormat == "CSV") {
                writer.write(
                    "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,accuracyArray,created_at,updated_at\n",
                )
                listItems.forEach { farm ->
                    val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                    val matches = regex.findAll(farm.coordinates.toString())
                    val reversedCoordinates =
                        matches
                            .map { match ->
                                val (lat, lon) = match.destructured
                                "[$lon, $lat]"
                            }.toList()
                            .let { coordinates ->
                                if (coordinates.isNotEmpty()) {
                                    // Always include brackets, even for a single point
                                    coordinates.joinToString(", ", prefix = "[", postfix = "]")
                                } else {
                                    ""
                                }
                            }

                    val line =
                        "${farm.remoteId},\"${
                            farm.farmerName.split(" ").joinToString(" ")
                        }\",${farm.memberId},\"${getSiteById?.name}\",\"${getSiteById?.agentName}\",\"${farm.village}\",\"${farm.district}\",${farm.size},${farm.latitude},${farm.longitude},\"${reversedCoordinates}\",\"${farm.accuracyArray}\",${
                            Date(
                                farm.createdAt,
                            )
                        },${Date(farm.updatedAt)}\n"
                    writer.write(line)
                }
            } else {
                val geoJson =
                    buildString {
                        append("{\"type\": \"FeatureCollection\", \"features\": [")
                        listItems.forEachIndexed { index, farm ->
                            val regex = "\\(([^,]+), ([^)]+)\\)".toRegex()
                            val matches = regex.findAll(farm.coordinates.toString())
                            val geoJsonCoordinates =
                                matches
                                    .map { match ->
                                        val (lat, lon) = match.destructured
                                        "[$lon, $lat]"
                                    }.joinToString(", ", prefix = "[", postfix = "]")
                            val latitude =
                                farm.latitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0
                            val longitude =
                                farm.longitude.toDoubleOrNull()?.takeIf { it != 0.0 } ?: 0.0

                            val feature =
                                """
                                    {
                                        "type": "Feature",
                                        "properties": {
                                            "remote_id": "${farm.remoteId}",
                                            "farmer_name":"${
                                    farm.farmerName.split(" ").joinToString(" ")
                                }",
                                            "member_id": "${farm.memberId}",
                                            "collection_site": "${getSiteById?.name ?: ""}",
                                            "agent_name": "${getSiteById?.agentName ?: ""}",
                                            "farm_village": "${farm.village}",
                                            "farm_district": "${farm.district}",
                                             "farm_size": ${farm.size},
                                            "latitude": $latitude,
                                            "longitude": $longitude,
                                             "accuracyArray": "${farm.accuracyArray ?: ""}",
                                            "created_at": "${Date(farm.createdAt)}",
                                            "updated_at": "${Date(farm.updatedAt)}
                                        },
                                        "geometry": {
                                            "type": "${if ((farm.coordinates?.size ?: 0) > 1) "Polygon" else "Point"}",
                                             "coordinates": ${if ((farm.coordinates?.size ?: 0) > 1) "[$geoJsonCoordinates]" else "[$latitude, $longitude]"}
                                        }
                                    }
                                    """.trimIndent()
                            append(feature)
                            if (index < listItems.size - 1) append(",")
                        }
                        append("]}")
                    }
                writer.write(geoJson)
            }
        }
        return file
    } catch (e: IOException) {
        Toast.makeText(context, R.string.error_export_msg, Toast.LENGTH_SHORT).show()
        return null
    }
}