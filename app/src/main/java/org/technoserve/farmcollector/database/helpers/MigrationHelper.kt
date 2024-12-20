package org.technoserve.farmcollector.database.helpers

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * This class helps to run the migrations of a database from one version to another
 */
class MigrationHelper(private val context: Context) {
    fun executeSqlFromFile(database: SupportSQLiteDatabase, fileName: String) {
        val sql = context.assets.open("migrations/$fileName").bufferedReader().use { it.readText() }
        sql.split(";").forEach { statement ->
            val trimmed = statement.trim()
            if (trimmed.isNotEmpty()) {
                database.execSQL(trimmed)
            }
        }
    }
}
