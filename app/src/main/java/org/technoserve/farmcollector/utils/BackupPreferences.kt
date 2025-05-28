package org.technoserve.farmcollector.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ Define the DataStore instance for the Context
val Context.dataStore by preferencesDataStore(name = "backup_preferences")

object BackupPreferences {
    private val BACKUP_ENABLED_KEY = booleanPreferencesKey("backup_enabled")
    private val BACKUP_DECISION_MADE_KEY = booleanPreferencesKey("backup_decision_made")
    private val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")

    // ✅ Check if the user has enabled backup
    fun isBackupEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BACKUP_ENABLED_KEY] ?: false // Default is disabled
        }
    }

    // ✅ Check if the user has made a decision
    fun isBackupDecisionMade(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BACKUP_DECISION_MADE_KEY] ?: false
        }
    }

    // ✅ Save user decision
    suspend fun saveBackupChoice(context: Context, isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_ENABLED_KEY] = isEnabled
            preferences[BACKUP_DECISION_MADE_KEY] = true
        }
    }

    // ✅ Read the last sync timestamp
    fun getLastBackupTime(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val timestamp = preferences[LAST_SYNC_TIMESTAMP_KEY] ?: 0L
            if (timestamp > 0) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
            } else {
                "Never backed up"
            }
        }
    }

    // ✅ Save backup toggle state
    suspend fun setBackupEnabled(context: Context, isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKUP_ENABLED_KEY] = isEnabled
        }
    }

    // ✅ Save the last sync timestamp
    suspend fun setLastBackupTime(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }



}
