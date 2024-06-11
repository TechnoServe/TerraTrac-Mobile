package org.technoserve.farmcollector.utils

import android.content.Context
import java.util.Locale

fun updateLocale(context: Context, locale: Locale) {
    Locale.setDefault(locale)
    val resources = context.resources

    val configuration = resources.configuration
    configuration.locale = locale
    configuration.setLayoutDirection(locale)

    resources.updateConfiguration(configuration, resources.displayMetrics)
}
