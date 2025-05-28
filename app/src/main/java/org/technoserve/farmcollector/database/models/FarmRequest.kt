package org.technoserve.farmcollector.database.models
/**
 * This class represents a request to add a new farm to the database
 * device_id: unique identifier for the device requesting the farm addition
 * email: optional email address for the user
 * phone_number: optional phone number for the user
 *
 * Note: This is a basic model for a FarmRequest. In a real-world application, you might need to add additional fields or make changes based on your specific requirements.
 */
data class FarmRequest(
    val device_id: String,
    val email: String = "",
    val phone_number: String = ""
)
