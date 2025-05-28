package org.technoserve.farmcollector.database.models.map

import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.clustering.ClusterItem
import org.technoserve.farmcollector.utils.map.getCenterOfPolygon

/**
 * Represents a cluster item representing a zone in the map.
 *
 * @param id Unique identifier for the zone.
 * @param title Title of the zone.
 * @param snippet Description of the zone.
 * @param polygonOptions PolygonOptions representing the boundaries of the zone.
 */
data class ZoneClusterItem(
    val id: String,
    private val title: String,
    private val snippet: String,
    val polygonOptions: PolygonOptions
) : ClusterItem {

    override fun getSnippet() = snippet

    override fun getTitle() = title

    override fun getPosition() = polygonOptions.points.getCenterOfPolygon().center

}