package org.technoserve.farmcollector.database

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.converters.DateConverter
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.Date
import java.util.UUID

data class ImportResult(val success: Boolean, val message: String, val importedFarms: List<Farm>)

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FarmRepository
    val readAllSites: RefreshableLiveData<List<CollectionSite>>
    val readData: RefreshableLiveData<List<Farm>>

    private val _farms = MutableLiveData<List<Farm>>()
    val farms: LiveData<List<Farm>> get() = _farms

    init {
        val farmDAO = AppDatabase.getInstance(application).farmsDAO()
        repository = FarmRepository(farmDAO)
        readAllSites = RefreshableLiveData { repository.readAllSites }
        readData = RefreshableLiveData { repository.readData }
    }

    fun readAllData(siteId: Long): LiveData<List<Farm>> {
        return repository.readAllFarms(siteId)
    }

    fun getSingleFarm(farmId: Long): LiveData<List<Farm>> {
        return repository.readFarm(farmId)
    }

    fun addFarm(farm: Farm, siteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isDuplicateFarm(farm, siteId)) {
                repository.addFarm(farm)
                // Update the LiveData list
                _farms.postValue(repository.readAllFarms(siteId).value ?: emptyList())
            }
        }
    }

    fun addSite(site: CollectionSite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSite(site)
        }
    }

    fun getLastFarm(): LiveData<List<Farm>> {
        return repository.getLastFarm()
    }

    fun updateFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFarm(farm)
        }
    }

    fun updateSite(site: CollectionSite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSite(site)
        }
    }


    fun deleteFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFarm(farm)
        }
    }

    fun deleteAllFarms() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllFarms()
        }
    }

    fun updateSyncStatus(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSyncStatus(id)
        }
    }

    fun updateSyncListStatus(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSyncListStatus(ids)
        }
    }

    fun deleteList(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteList(ids)
        }
    }

    fun deleteListSite(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteListSite(ids)
        }
    }


    private fun parseDateStringToTimestamp(dateString: String): Long {
        val dateFormatter = java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US)
        return dateFormatter.parse(dateString).time
    }

    private fun parseGeoJson(geoJsonString: String,siteId: Long): List<Farm> {
        val farms = mutableListOf<Farm>()

        try {
            val geoJson = JSONObject(geoJsonString)
            val features = geoJson.getJSONArray("features")

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")

                val remoteId = UUID.fromString(properties.getString("remote_id"))
                val farmerName = properties.getString("farmer_name")
                val memberId = properties.getString("member_id")
                val village = properties.getString("farm_village")
                val district = properties.getString("farm_district")
                val size = properties.getDouble("farm_size").toFloat()
                val latitude = properties.getDouble("latitude").toString()
                val longitude = properties.getDouble("longitude").toString()
                val createdAt = Date(properties.getString("created_at")).time
                val updatedAt = Date(properties.getString("updated_at")).time

                var coordinates: List<Pair<Double, Double>>? = null
                val geoType = geometry.getString("type")
                if (geoType == "Point") {
                    // Handle Point geometry
                    val coordArray = geometry.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0)
                    val lon = coordArray.getDouble(1)
                    val lat = coordArray.getDouble(0)
                    coordinates = listOf(Pair(lon, lat))
                }
                else if (geoType == "Polygon") {
                    val coordArray = geometry.getJSONArray("coordinates").getJSONArray(0)
                    val coordList = mutableListOf<Pair<Double, Double>>()
                    for (j in 0 until coordArray.length()) {
                        val coord = coordArray.getJSONArray(j)
                        coordList.add(Pair(coord.getDouble(0), coord.getDouble(1)))
                    }
                    coordinates = coordList
                }

                val newFarm = Farm(
                    siteId = siteId,
                    remoteId = remoteId,
                    farmerPhoto = "farmer-photo",
                    farmerName = farmerName,
                    memberId = memberId,
                    village = village,
                    district = district,
                    purchases = 2.30f,
                    size = size,
                    latitude = latitude.toString(),
                    longitude = longitude.toString(),
                    coordinates = coordinates,
                    synced = false,
                    scheduledForSync = false,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

                if (!isDuplicateFarm(newFarm, siteId)) {
                    farms.add(newFarm)
                } else {
                    println("Duplicate farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return farms
    }


    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun importFile(context: Context, uri: Uri, siteId:Long): ImportResult {
        var message = "Import failed" // Default message in case of failure
        var success = true// Default to failure until proven otherwise
        var importedFarms= mutableListOf<Farm>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val firstLine = reader.readLine()
                val farms = mutableListOf<Farm>()


                println("first line $firstLine")

                // Determine if the file is CSV or GeoJSON
                if (firstLine.trim().startsWith("{")) {
                    // It's a GeoJSON file
                    val content = StringBuilder()
                    content.append(firstLine)
                    reader.lines().forEach { content.append(it) }
                    reader.close()
                    val newFarms = parseGeoJson(content.toString(),siteId)
                    println("Parsed farms from GeoJSON: $newFarms")
                    for (newFarm in newFarms) {
                        if (!isDuplicateFarm(newFarm, newFarm.siteId)) {
                            println("Adding farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                            addFarm(newFarm, newFarm.siteId)
                            importedFarms.add(newFarm)
                        }
                        else{
                            println("Duplicate farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                        }
                    }
                    message = "GeoJSON import successful"
                    success = true
                }
                else {
                    // It's a CSV file
                    var line: String? = firstLine
                    line = reader.readLine() // Read first data line
                    while (line != null) {
                        val values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()) // Split CSV line, ignoring commas within quotes
                        if (values.size >= 13) {
                            val remoteId = UUID.fromString(values[0])
                            val farmerName = values[1]
                            val memberId = values[2]
                            val siteName = values[3]
                            val agentName = values[4]
                            val village = values[5]
                            val district = values[6]
                            val size = values[7].toFloat()
                            val latitude = values[8]
                            val longitude = values[9]

                            // Parsing the polygon coordinates
                            val coordinatesString = values[10].removeSurrounding("\"", "\"")
                            val coordinates = coordinatesString.replace("[[", "").replace("]]", "")
                                .split("],\\s*\\[".toRegex()) // Adjust regex to handle spaces
                                .map {
                                    val latLng = it.split(",\\s*".toRegex()) // Adjust regex to handle spaces
                                    Pair(latLng[1].toDouble(), latLng[0].toDouble())
                                }
                            val createdAt = parseDateStringToTimestamp(values[11])
                            val updatedAt = parseDateStringToTimestamp(values[12])

                            // Process each record here
                            println("Processing record for remote ID: $remoteId")

                            val newFarm = Farm(
                                siteId = siteId,
                                remoteId = remoteId,
                                farmerPhoto = "farmer-photo",
                                farmerName = farmerName,
                                memberId = memberId,
                                village = village,
                                district = district,
                                purchases = 2.30f,
                                size = size,
                                latitude = latitude,
                                longitude = longitude,
                                coordinates = coordinates,
                                synced = false,
                                scheduledForSync = false,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                            if (!isDuplicateFarm(newFarm, siteId)) {
                                println("Adding farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                                farms.add(newFarm)
                                importedFarms.add(newFarm)
                            }
                            else{
                                println("Duplicate farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                            }
                        }
                        line = reader.readLine()
                    }
                    reader.close()
                    println("Parsed farms from CSV: $farms")
                    // Update LiveData with the imported farms
                    repository.importFarms(farms)
                    message = "CSV import successful"
                    success = true
                    importedFarms=farms
                }
            } catch (e: Exception) {
                e.printStackTrace()
                message = "Import failed: ${e.message}"
                success = false
            }
        }
        return ImportResult(success, message, importedFarms)
    }


    private fun isDuplicateFarm(farm: Farm,siteId: Long): Boolean {
        val existingFarms = repository.readAllFarms(siteId).value ?: return false
        println("Existing farms $existingFarms")
        return existingFarms.any { it.remoteId == farm.remoteId }
    }

    fun getTemplateContent(fileType: String): String {
        return when (fileType) {
            "csv" -> "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n"
            "json" -> """{
                        "type": "FeatureCollection",
                        "features": [
                            {
                                "type": "Feature",
                                "properties": {
                                    "remote_id": "",
                                    "farmer_name": "",
                                    "member_id": "",
                                    "collection_site": "",
                                    "agent_name": "",
                                    "farm_village": "",
                                    "farm_district": "",
                                    "farm_size": 0.0,
                                    "latitude": "",
                                    "longitude": "",
                                    "created_at": "",
                                    "updated_at": ""
                                },
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": ["longitude", "latitude"]
                                }
                            },
                            {
                                "type": "Feature",
                                "properties": {
                                    "remote_id": "",
                                    "farmer_name": "",
                                    "member_id": "",
                                    "collection_site": "",
                                    "agent_name": "",
                                    "farm_village": "",
                                    "farm_district": "",
                                    "farm_size": "farm size is double",
                                    "latitude": "latitude value in double",
                                    "longitude": "longitude value in double",
                                    "created_at": "",
                                    "updated_at": ""
                                },
                                "geometry": {
                                    "type": "Polygon",
                                    "coordinates": [[["longitude","latitude"], ["longitude","latitude"],["longitude", "latitude"], ["longitude", "latitude"], ["longitude", "latitude"], ["longitude", "latitude"]]]
                                }
                            }
                        ]
                    }"""
            else -> throw IllegalArgumentException("Unsupported file type: $fileType")
        }
    }


    // Define the method for saving the file to the URI
    suspend fun saveFileToUri(
        context: Context,
        uri: Uri,
        templateContent: String
    ) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(templateContent.toByteArray())
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.template_downloaded, Toast.LENGTH_SHORT).show()
                }
            } ?: withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.template_download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun flagFarmersWithNewPlotInfo(siteId: Long, importedFarms: List<Farm>) {
        withContext(Dispatchers.IO) {
            val existingFarms = repository.readAllFarms(siteId).value

            // Create a map of existing farms by their remoteId for quick lookup
            val existingFarmMap = existingFarms?.associateBy { it.remoteId } ?: emptyMap()

            // Iterate through imported farms and check for updates
            for (importedFarm in importedFarms) {
                val existingFarm = existingFarmMap[importedFarm.remoteId]

                // If the farm is new or has different details, flag it for update
                if (existingFarm == null || existingFarm != importedFarm) {
                    importedFarm.needsUpdate = true
                }
            }

            // Update farms that need updates
            importedFarms.filter { it.needsUpdate }.forEach { updateFarm(it) }
        }
    }












}

class FarmViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(FarmViewModel::class.java)) {
            return FarmViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}