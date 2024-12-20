package org.technoserve.farmcollector.database.models
/**
 * This class represents the result of adding a new farm to the database
 *
 * @property success A boolean indicating whether the farm was added successfully
 * @property message A string containing the message explaining the success or failure of the operation
 * @property farm The Farm object that was added to the database if the operation was successful
 */
data class FarmAddResult(
    val success: Boolean,
    val message: String,
    val farm: Farm,
)
