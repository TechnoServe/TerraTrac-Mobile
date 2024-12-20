package org.technoserve.farmcollector.database.models

/**
    This DTO class represents the farm details fetched from the remote server.
    It includes fields for remote_id, farmer_name, member_id, village, district, size, latitude, longitude, coordinates, and accuracies.
    These fields represent the data needed to display farm details in the app.
    Note: The actual fields in the DTO class may vary depending on the structure of the remote server's data.
    The names of the fields are provided as placeholders.
 */
data class FarmDetailDto(
    val remote_id: String,
    val farmer_name: String,
    val member_id: String,
    val village: String,
    val district: String,
    val size: Float,
    val latitude: Double,
    val longitude: Double,
    val coordinates: List<List<Double?>>?,
    val accuracies: List<Float?>?,
)
