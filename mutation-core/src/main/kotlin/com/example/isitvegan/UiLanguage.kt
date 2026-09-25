package com.example.isitvegan

import java.util.Locale

/** Stable interface language codes shared by the Android adapter and analysis core. */
enum class UiLanguage(val code: String, val localeTag: String) {
    FR("FR", "fr"), EN("EN", "en"), NL("NL", "nl");

    companion object {
        fun fromCode(value: String?): UiLanguage = entries.firstOrNull { it.code == value } ?: FR

        fun fromDeviceLanguage(value: String?): UiLanguage = when (value?.lowercase(Locale.ROOT)) {
            "en" -> EN
            "nl" -> NL
            else -> FR
        }
    }
}
