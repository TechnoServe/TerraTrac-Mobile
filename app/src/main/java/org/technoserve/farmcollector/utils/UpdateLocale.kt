package org.technoserve.farmcollector.utils

import android.content.Context
import java.util.Locale

fun updateLocale(context: Context, locale: Locale) {
    // Set the default locale
    Locale.setDefault(locale)

    // Get the configuration object
    val config = context.resources.configuration

    // Set the locale in the configuration
    config.setLocale(locale)

    // Update the layout direction if necessary
    config.setLayoutDirection(locale)

    // Apply the updated configuration
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    // Optionally, refresh the current activity or application
    val resources = context.resources
    val dm = resources.displayMetrics
    val conf = resources.configuration
    conf.setLocale(locale)
    resources.updateConfiguration(conf, dm)
}
