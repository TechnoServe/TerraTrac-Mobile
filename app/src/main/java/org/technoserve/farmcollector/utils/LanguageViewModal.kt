package org.technoserve.farmcollector.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(getDefaultLanguage())
    val currentLanguage: StateFlow<Language> = _currentLanguage

    fun selectLanguage(language: Language, context: Context) {
        _currentLanguage.value = language
        savePreferredLanguage(language)
        updateLocale(context, Locale(language.code))
    }

    private fun getDefaultLanguage(): Language {
        val savedLanguageCode =
            sharedPreferences.getString("preferred_language", Locale.getDefault().language)
        return languages.find { it.code == savedLanguageCode } ?: languages.first()
    }

    private fun savePreferredLanguage(language: Language) {
        sharedPreferences.edit().putString("preferred_language", language.code).apply()
    }
}
