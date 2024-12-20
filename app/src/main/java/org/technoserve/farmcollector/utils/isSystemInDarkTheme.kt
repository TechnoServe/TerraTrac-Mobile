package org.technoserve.farmcollector.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
/**
 * Returns true if the system is in dark mode, false otherwise.
 *
 * @return True if the system is in dark mode, false otherwise.
 *
 */
@Composable
fun isSystemInDarkTheme(): Boolean {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("theme_mode", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("dark_mode", false)
}
