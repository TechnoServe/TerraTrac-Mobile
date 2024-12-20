package org.technoserve.farmcollector.database.models


/**
 * This class represents the response from the server for retrieving farms.
 *
 * @property device_id The device ID of the farm collector app.
 * @property collection_site The restored collection site.
 * @property farms The list of restored farms.
 */
class ServerFarmResponse(
    val device_id: String,
    val collection_site: CollectionSiteRestore,
    val farms: List<FarmRestore>
)
