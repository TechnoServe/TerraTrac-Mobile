package org.technoserve.farmcollector.utils

import android.content.Context
import java.util.Locale

fun updateLocale(context: Context, locale: Locale) {
    Locale.setDefault(locale)

    val config = context.resources.configuration

    config.setLocale(locale)

    config.setLayoutDirection(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    val resources = context.resources
    val dm = resources.displayMetrics
    val conf = resources.configuration
    conf.setLocale(locale)
    resources.updateConfiguration(conf, dm)
}
