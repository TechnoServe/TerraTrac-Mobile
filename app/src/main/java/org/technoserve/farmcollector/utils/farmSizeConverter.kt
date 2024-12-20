package org.technoserve.farmcollector.utils

/**
 * Converts a given size to hectares based on the selected unit.
 *
 * @param size The size to be converted.
 * @param selectedUnit The unit of the given size.
 * @return The converted size in hectares.
 */
fun convertSize(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "Ha" -> size // If already in hectares, return as is
        "Acres" -> size * 0.404686 // Convert Acres to hectares
        "Sqm" -> size * 0.0001 // Convert square meters to hectares
        "Timad" -> size * 0.24 // Convert Timad to Hectares
        "Fichesa" -> size * 0.25 // Convert Fichesa to Hectares
        "Manzana" -> (size * 0.0001) * 7000 // Convert Manzana to Hectares
        "Tarea" -> (size * 0.0001) * 432 // Convert Tarea to Hectares
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}