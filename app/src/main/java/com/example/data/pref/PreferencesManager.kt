package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("zoya_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_AI_ENGINE = "ai_engine"
        private const val KEY_OPENAI_API_KEY = "openai_api_key"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_VOICE_NAME = "voice_name"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "youtubecom568@gmail.com") ?: "youtubecom568@gmail.com"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var aiEngine: String
        get() = prefs.getString(KEY_AI_ENGINE, "Gemini") ?: "Gemini"
        set(value) = prefs.edit().putString(KEY_AI_ENGINE, value).apply()

    var openaiApiKey: String
        get() = prefs.getString(KEY_OPENAI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OPENAI_API_KEY, value).apply()

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, value).apply()

    var voiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_ENABLED, value).apply()

    var voiceName: String
        get() = prefs.getString(KEY_VOICE_NAME, "hi-IN-Language-Female") ?: "hi-IN-Language-Female"
        set(value) = prefs.edit().putString(KEY_VOICE_NAME, value).apply()

    var darkTheme: Boolean?
        get() = if (prefs.contains(KEY_DARK_THEME)) prefs.getBoolean(KEY_DARK_THEME, false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_DARK_THEME).apply()
            } else {
                prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()
            }
        }
}
