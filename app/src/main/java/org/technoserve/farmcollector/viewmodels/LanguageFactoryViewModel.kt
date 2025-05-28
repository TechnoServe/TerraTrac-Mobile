package org.technoserve.farmcollector.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating instances of the LanguageViewModel class.
 *
 * @param application The application context.
 * @constructor Creates a new instance of the LanguageViewModelFactory.
 *
 * @see ViewModelProvider.Factory
 */
class LanguageViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanguageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
