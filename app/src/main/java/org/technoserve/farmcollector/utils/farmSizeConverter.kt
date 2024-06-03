package org.technoserve.farmcollector.utils

fun convertSize(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "Ha" -> size // If already in hectares, return as is
        "Acres" -> size * 0.404686 // Convert Acres to hectares
        "sqm" -> size * 0.0001 // Convert square meters to hectares
        "Timad" -> size * 0.24
        "Fichesa" -> size * 0.25
        "Manzana" -> (size * 0.0001) * 7000
        "Tarea" -> (size * 0.0001) * 432
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}

fun convertSizeToAcres(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "Ha" -> size * 2.47105 // Convert hectares to Acres
        "Acres" -> size // If already in Acres, return as is
        "sqm" -> size * 0.000247105 // Convert square meters to Acres
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}