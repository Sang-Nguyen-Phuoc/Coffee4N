package com.example.coffee4n.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val LANGUAGE_KEY = "current_language"

    fun setLocale(context: Context, languageCode: String) {
        persistLanguage(context, languageCode)
        updateResources(context, languageCode)
    }

    private fun persistLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    fun updateResources(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "🇺🇸"),
            Language("vi", "Tiếng Việt", "🇻🇳"),
            Language("fr", "Français", "🇫🇷")
        )
    }

    data class Language(
        val code: String,
        val name: String,
        val flag: String
    )
}