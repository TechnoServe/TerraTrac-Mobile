package org.technoserve.farmcollector.database.models

/**
 * This class represents the parsed farms with valid and invalid farms
 *
 * @param validFarms A list of valid farms
 * @param invalidFarms A list of invalid farms (e.g., farms with missing or invalid data)
 *
 * @author
 */
data class ParsedFarms(
    val validFarms: List<Farm>,
    val invalidFarms: List<String>
)