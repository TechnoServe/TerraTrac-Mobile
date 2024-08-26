package org.technoserve.farmcollector.utils

import android.content.Context
import org.technoserve.farmcollector.R

fun getLocalizedLanguages(context: Context): List<Language> {
    val languages = listOf(
        Language("en", context.getString(R.string.english)),
        Language("fr", context.getString(R.string.french)),
        Language("es", context.getString(R.string.spanish)),
        Language("am", context.getString(R.string.amharic)),
        Language("om", context.getString(R.string.oromo)),
        Language("sw",context.getString(R.string.swahili))
    )

    return languages.map { language ->
        Language(language.code, language.displayName)
    }
}
