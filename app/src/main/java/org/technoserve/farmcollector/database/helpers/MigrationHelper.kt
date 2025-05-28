package org.technoserve.farmcollector.database.helpers

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * This class helps to run the migrations of a database from one version to another
 */
class MigrationHelper(private val context: Context) {
    fun executeSqlFromFile(database: SupportSQLiteDatabase, fileName: String) {
        val sql = context.assets.open("migrations/$fileName").bufferedReader().use { it.readText() }
        sql.split(";").forEach { statement ->
            val trimmed = statement.trim()
            if (trimmed.isNotEmpty()) {
                //database.execSQL(trimmed)
                try {
                    database.execSQL(trimmed)
                } catch (e: SQLiteException) {
                    // Log the error, potentially clear the database or offer a recovery mechanism
                    Timber.e(e, "Error executing migration SQL")
                    // Example: database.execSQL("DELETE FROM Farm") // Clear corrupt data
                }
            }
        }
    }
}
