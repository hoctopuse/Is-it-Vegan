package com.example.isitvegan

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

internal object UiLanguagePreferences {
    private const val FILE = "ui_preferences"
    private const val KEY = "interface_language"

    fun read(context: Context): UiLanguage {
        val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val savedCode = preferences.getString(KEY, null)
        if (savedCode != null) return UiLanguage.fromCode(savedCode)
        return UiLanguage.fromDeviceLanguage(context.resources.configuration.locales[0]?.language)
    }

    fun write(context: Context, language: UiLanguage) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString(KEY, language.code).apply()
    }
}

internal fun localizedContext(context: Context, language: UiLanguage): Context {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(Locale.forLanguageTag(language.localeTag))
    return context.createConfigurationContext(configuration)
}
