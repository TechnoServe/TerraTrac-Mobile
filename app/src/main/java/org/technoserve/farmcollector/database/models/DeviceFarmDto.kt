package org.technoserve.farmcollector.database.models
/**
 * This DTO class represents the device_farm table from the database.
 * It contains the device_id, collection_site, and a list of farms.
 *
 * @property device_id the unique identifier for the device
 * @property collection_site the collection site associated with the device
 * @property farms the list of farms collected by the device
 */
data class DeviceFarmDto(
    val device_id: String,
    val collection_site: CollectionSiteDto,
    val farms: List<FarmDetailDto>
)
