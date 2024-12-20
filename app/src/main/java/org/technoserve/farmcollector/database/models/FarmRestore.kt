package org.technoserve.farmcollector.database.models
/**
 * This class represents the restored farm data
 *
 * @property id The unique ID of the farm
 * @property remote_id The remote ID of the farm
 * @property farmer_name The name of the farmer
 * @property member_id The unique ID of the farmer's member
 * @property size The size of the farm in acres
 * @property agent_name The name of the agent assigned to the farm
 * @property village The village of the farm
 * @property district The district of the farm
 * @property latitude The latitude of the farm
 * @property longitude The longitude of the farm
 * @property coordinates The coordinates of the farm
 * @property accuracyArray The array of accuracies for each coordinate
 * @property created_at The timestamp when the farm was created
 * @property updated_at The timestamp when the farm was last updated
 * @property site_id The unique ID of
 */
data class FarmRestore(
    val id: Long,
    val remote_id: String,
    val farmer_name: String,
    val member_id: String?,
    val size: Double,
    val agent_name: String?,
    val village: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val coordinates: List<List<Double>>,
    val accuracyArray: List<Float?>?,
    val created_at: String,
    val updated_at: String,
    val site_id: Long
)
