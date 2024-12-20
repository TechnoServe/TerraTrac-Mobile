package org.technoserve.farmcollector.database.models.map
/*
 * This class represents the state of the location
 * - latitude: The current latitude
 * - longitude: The current longitude
 * - isLoading: True if the location is being fetched, false otherwise
 * - error: An error message if fetching the location fails
 * - isUsingGPS: True if the location is being fetched using GPS, false otherwise
 * - accuracy: The accuracy of the location being captured
 */
data class LocationState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUsingGPS: Boolean = true,
    val accuracy: Float = 0f
)
