package org.technoserve.farmcollector.database.models

/**
 * Language model for storing and retrieving language data from the database
 *
 * @property code The ISO 639-1 language code
 * @property displayName The display name for the language
 *
 */

data class Language(val code: String, val displayName: String)
