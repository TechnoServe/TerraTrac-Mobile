package org.technoserve.farmcollector.database.models

/**
 * This class represents the Collection Site data transfer object.
 *
 * @property local_cs_id local Collection Site ID
 * @property name Collection Site name
 * @property agent_name Agent name
 * @property phone_number Phone number
 * @property email Email address
 * @property village VillageName
 * @property district DistrictName
 */
data class CollectionSiteDto(
    val local_cs_id: Long,
    val name: String,
    val agent_name: String,
    val phone_number: String?,
    val email: String?,
    val village: String?,
    val district: String?
)

