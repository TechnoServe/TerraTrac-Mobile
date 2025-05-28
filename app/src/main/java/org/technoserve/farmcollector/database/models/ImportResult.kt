package org.technoserve.farmcollector.database.models

/**
 * This class represents the result of importing farms from a CSV file
 * @param success true if the import was successful, false otherwise
 * @param message a message describing the outcome of the import
 * @param importedFarms the farms that were successfully imported
 * @param duplicateFarms a list of farms that were already in the database
 * @param farmsNeedingUpdate a list of farms that need to be updated in the database
 */
data class ImportResult(
    val success: Boolean,
    val message: String,
    val importedFarms: List<Farm>,
    val duplicateFarms: List<String> = emptyList(),
    val farmsNeedingUpdate: List<Farm> = emptyList(),
    val invalidFarms: List<String> = emptyList()
)
