package org.technoserve.farmcollector.database.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("privacy_policy", Context.MODE_PRIVATE)

    private val AGREED_KEY = "has_agreed_to_privacy_policy_terms"

    var hasAgreedToTerms: Boolean
        get() = sharedPreferences.getBoolean(AGREED_KEY, false)
        set(value) = sharedPreferences.edit { putBoolean(AGREED_KEY, value) }
}