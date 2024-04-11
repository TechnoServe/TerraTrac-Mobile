package org.technoserve.farmcollector.utils

fun convertSize(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "ha" -> size // If already in hectares, return as is
        "Acres" -> size * 0.404686 // Convert Acres to hectares
        "sqm" -> size * 0.0001 // Convert square meters to hectares
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}

fun convertSizeToAcres(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "ha" -> size * 2.47105 // Convert hectares to Acres
        "Acres" -> size // If already in Acres, return as is
        "sqm" -> size * 0.000247105 // Convert square meters to Acres
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}

fun convertSizeToSquareMeters(size: Double, selectedUnit: String): Double {
    return when (selectedUnit) {
        "ha" -> size * 10000 // Convert hectares to square meters
        "Acres" -> size * 4046.86 // Convert Acres to square meters
        "sqm" -> size // If already in square meters, return as is
        else -> throw IllegalArgumentException("Unsupported unit: $selectedUnit")
    }
}