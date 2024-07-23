package org.technoserve.farmcollector.database

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.technoserve.farmcollector.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date
import java.util.UUID

data class ImportResult(
    val success: Boolean,
    val message: String,
    val importedFarms: List<Farm>,
    val duplicateFarms: List<String> = emptyList(),
    val farmsNeedingUpdate: List<Farm> = emptyList()
)

data class FarmAddResult(
    val success: Boolean,
    val message: String,
    val farm: Farm
)



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
            if (!repository.isFarmDuplicateBoolean(farm)) {
                repository.addFarm(farm)
                FarmAddResult(success = true, message = "Farm added successfully", farm)
                // Update the LiveData list
                _farms.postValue(repository.readAllFarms(siteId).value ?: emptyList())
            }
            else {
                FarmAddResult(success = false, message = "Duplicate farm: ${farm.farmerName}, Site ID: ${farm.siteId}. Needs update.", farm)
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

    // Updates an existing farm in the repository and updates the LiveData list
    fun updateFarm(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFarm(farm)

            // Fetch the updated list and post the value to LiveData
            val updatedFarms = repository.readAllFarms(farm.siteId).value ?: emptyList()
            _farms.postValue(updatedFarms)
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

    fun refreshData(siteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Logic to refresh data, typically re-fetching from the database or repository
            repository.readAllFarms(siteId)
        }
    }


    private fun parseDateStringToTimestamp(dateString: String): Long {
        val dateFormatter = java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US)
        return dateFormatter.parse(dateString).time
    }

    private suspend fun parseGeoJson(geoJsonString: String, siteId: Long): List<Farm> {
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
                    val coordArray = geometry.getJSONArray("coordinates")
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

                val newFarm = coordinates?.let {
                    Farm(
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
                        coordinates = it,
                        synced = false,
                        scheduledForSync = false,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                }
                if (newFarm != null) {
                    farms.add(newFarm)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return farms
    }

    fun parseCoordinates(coordinatesString: String): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        val cleanedString = coordinatesString.trim().removeSurrounding("\"", "").replace(" ", "")

        if (cleanedString.isNotEmpty()) {
            // Check if the coordinates are in polygon or point format
            val isPolygon = cleanedString.startsWith("[[") && cleanedString.endsWith("]]")
            val isPoint = cleanedString.startsWith("[") && cleanedString.endsWith("]") && !isPolygon

            if (isPolygon) {
                // Handle Polygon Format
                val pairs = cleanedString.removePrefix("[[").removeSuffix("]]").split("],[").map { it.split(",") }
                for (pair in pairs) {
                    if (pair.size == 2) {
                        try {
                            val lat = pair[1].toDouble()
                            val lon = pair[0].toDouble()
                            result.add(Pair(lat, lon))
                        } catch (e: NumberFormatException) {
                            println("Error parsing polygon coordinate pair: ${pair.joinToString(",")}")
                        }
                    }
                }
            } else if (isPoint) {
                // Handle Point Format
                val coords = cleanedString.removePrefix("[").removeSuffix("]").split(",")
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




    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun importFile(context: Context, uri: Uri, siteId: Long): ImportResult = withContext(Dispatchers.IO) {
        var message = ""
        var success = false
        val importedFarms = mutableListOf<Farm>()
        val duplicateFarms = mutableListOf<String>()
        val farmsNeedingUpdate = mutableListOf<Farm>()
        try {
            // Check file extension before proceeding
            val fileName = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid file URI")
            if (!fileName.endsWith(".csv", true) && !fileName.endsWith(".geojson", true)) {
                message = "Unsupported file format. Please upload a CSV or GeoJSON file."
                return@withContext ImportResult(success, message, importedFarms)
            }

            val inputStream = context.contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open file input stream")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val firstLine = reader.readLine()
            val farms = mutableListOf<Farm>()

            println("First line: $firstLine")

            if (firstLine.trim().startsWith("{")) {
                // It's a GeoJSON file
                val content = StringBuilder()
                content.append(firstLine)
                reader.lines().forEach { content.append(it) }
                reader.close()
                val newFarms = parseGeoJson(content.toString(), siteId)
                println("Parsed farms from GeoJSON: $newFarms")
                for (newFarm in newFarms) {
                    if (!repository.isFarmDuplicateBoolean(newFarm)) {
                        println("Adding farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                        addFarm(newFarm, newFarm.siteId)
                        importedFarms.add(newFarm)
                    }
                    val existingFarm = newFarm.remoteId?.let { repository.getFarmByRemoteId(it) }
                    if (existingFarm != null) {
                        if (repository.farmNeedsUpdate(existingFarm, newFarm)) {
                            // Farm needs an update
                            println("Farm needs update: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                            farmsNeedingUpdate.add(newFarm)
                        } else {
                            // Farm is a duplicate but does not need an update
                            val duplicateMessage = "Duplicate farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}"
                            println(duplicateMessage)
                            duplicateFarms.add(duplicateMessage)
                        }
                    } else {
                        // Handle case where farm exists in the system but not in the repository
                        val unknownFarmMessage = "Farm with Site ID: ${newFarm.siteId} found in repository but not in the system."
                        println(unknownFarmMessage)
                    }
                }
                message = "GeoJSON import successful"
                success = true
            } else if (firstLine.contains(",")) {
                // It's a CSV file
                var line: String? = firstLine
                line = reader.readLine() // Read first data line
                while (line != null) {
                    val values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()) // Split CSV line, ignoring commas within quotes

                    if (values.size >= 13) {
                        val remoteId = try {
                            if (values[0].isNotEmpty()) UUID.fromString(values[0]) else UUID.randomUUID()
                        } catch (e: IllegalArgumentException) {
                            UUID.randomUUID()
                        }

                        val farmerName = values.getOrNull(1) ?: ""
                        val memberId = values.getOrNull(2) ?: ""
                        val siteName = values.getOrNull(3) ?: ""
                        val agentName = values.getOrNull(4) ?: ""
                        val village = values.getOrNull(5) ?: ""
                        val district = values.getOrNull(6) ?: ""
                        val size = values.getOrNull(7)?.toFloatOrNull()
                        val latitude = values.getOrNull(8)
                        val longitude = values.getOrNull(9)

                        // Extract and parse coordinates
                        val coordinatesString = values.getOrNull(10)?.removeSurrounding("\"", "\"") ?: ""
                        val coordinates = parseCoordinates(coordinatesString)
                        println("Coordinates $coordinates")

                        val currentTime = System.currentTimeMillis()
                        val createdAt = try {
                            if (values.getOrNull(11)?.isNotEmpty() == true) parseDateStringToTimestamp(values[11]) else currentTime
                        } catch (e: Exception) {
                            currentTime
                        }

                        val updatedAt = try {
                            if (values.getOrNull(12)?.isNotEmpty() == true) parseDateStringToTimestamp(values[12]) else currentTime
                        } catch (e: Exception) {
                            currentTime
                        }

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
                            size = size ?: 0f, // Use 0 as default if size is null
                            latitude = latitude ?: "0.0", // Use "0.0" as default if latitude is null
                            longitude = longitude ?: "0.0", // Use "0.0" as default if longitude is null
                            coordinates = coordinates ?: emptyList(), // Use empty list if coordinates are null
                            synced = false,
                            scheduledForSync = false,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )

                        if (!repository.isFarmDuplicateBoolean(newFarm)) {
                            println("Adding farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                            addFarm(newFarm, newFarm.siteId)
                            importedFarms.add(newFarm)
                        }

                        val existingFarm = newFarm.remoteId?.let { repository.getFarmByRemoteId(it) }
                        if (existingFarm != null) {
                            if (repository.farmNeedsUpdate(existingFarm, newFarm)) {
                                // Farm needs an update
                                println("Farm needs update: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}")
                                farmsNeedingUpdate.add(newFarm)
                            } else {
                                // Farm is a duplicate but does not need an update
                                val duplicateMessage = "Duplicate farm: ${newFarm.farmerName}, Site ID: ${newFarm.siteId}"
                                println(duplicateMessage)
                                duplicateFarms.add(duplicateMessage)
                            }
                        } else {
                            // Handle case where farm exists in the system but not in the repository
                            val unknownFarmMessage = "Farm with Site ID: ${newFarm.siteId} found in repository but not in the system."
                            println(unknownFarmMessage)

//                            // Remove farm from repository
//                            runBlocking {
//                                newFarm.remoteId?.let { repository.deleteFarmByRemoteId(it) }
//                            }
                        }
                    } else {
                        println("Line does not contain enough data: $line")
                    }
                    line = reader.readLine()
                }
                reader.close()
                println("Parsed farms from CSV: $farms")
                //repository.importFarms(farms)
                //importFarms(siteId,farms)

                message = "CSV import successful"
                success = true
            }
            else {
                message = "Unrecognized file format. Please upload a valid CSV or GeoJSON file."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Import failed: ${e.message}"
        }

        // Show a toast message for duplicate farms
        withContext(Dispatchers.Main) {
            if (duplicateFarms.isNotEmpty()) {
                Toast.makeText(context, "Duplicate farms exist", Toast.LENGTH_LONG).show()
            }
        }
        // Show a toast message for farms that needs updates
        withContext(Dispatchers.Main) {
            if (farmsNeedingUpdate.isNotEmpty()) {
                Toast.makeText(context, "${farmsNeedingUpdate.size} farms need to be updated", Toast.LENGTH_LONG).show()
            }
        }
        // Flag farmers with new plot info
        flagFarmersWithNewPlotInfo(siteId, farmsNeedingUpdate, this@FarmViewModel)

        return@withContext ImportResult(success, message, importedFarms, duplicateFarms,farmsNeedingUpdate)
    }

    private suspend fun flagFarmersWithNewPlotInfo(siteId: Long, farmsNeedingUpdate: List<Farm>, farmViewModel: FarmViewModel) = withContext(Dispatchers.IO) {
        val existingFarms = farmViewModel.getExistingFarms(siteId)
        val existingFarmMap = existingFarms.associateBy { it.remoteId }

        for (farmNeedingUpdate in farmsNeedingUpdate) {
            val existingFarm = existingFarmMap[farmNeedingUpdate.remoteId]

            if (existingFarm != null && existingFarm != farmNeedingUpdate) {
                existingFarm.needsUpdate = true
                farmViewModel.updateFarm(existingFarm)
                println("Flagging farm for update: ${existingFarm.farmerName}, Site ID: ${existingFarm.siteId}, NeedsUpdate:${existingFarm.needsUpdate}")
                repository.updateFarm(existingFarm)
                println("Farm updated successfully")
            } else if (existingFarm == null) {
                farmNeedingUpdate.needsUpdate = false
                println("New farm detected, flagging for update: ${farmNeedingUpdate.farmerName}, Site ID: ${farmNeedingUpdate.siteId}")
            }
        }
        farmsNeedingUpdate.forEach { farm ->
            farmViewModel.updateFarm(farm)
            println("Updating farm: ${farm.farmerName}, Site ID: ${farm.siteId}, needsUpdate: ${farm.needsUpdate}")
        }
    }

    fun getTemplateContent(fileType: String): String {
        return when (fileType) {
            "csv" -> "remote_id,farmer_name,member_id,collection_site,agent_name,farm_village,farm_district,farm_size,latitude,longitude,polygon,created_at,updated_at\n"
            "geojson" -> """{
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

    suspend fun getExistingFarms(siteId: Long): List<Farm> {
        return withContext(Dispatchers.IO) {
            repository.readAllFarmsSync(siteId)
        }
    }

//    fun importFarms(siteId: Long, importedFarms: List<Farm>) {
//        viewModelScope.launch {
//            flagFarmersWithNewPlotInfo(siteId, importedFarms, this@FarmViewModel)
//            // Update the farms LiveData after importing
//            _farms.postValue(getExistingFarms(siteId))
//        }
//    }

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