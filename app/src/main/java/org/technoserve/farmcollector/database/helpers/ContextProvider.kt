package org.technoserve.farmcollector.database.helpers

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextProvider {
    private lateinit var context: Context

    fun initialize(context: Context) {
        ContextProvider.context = context.applicationContext
    }

    fun getContext(): Context = context
}
