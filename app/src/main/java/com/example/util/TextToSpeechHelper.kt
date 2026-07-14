package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(
    context: Context,
    private val onReady: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            onReady()
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized) return
        
        val locale = if (languageCode == "hi") {
            Locale("hi", "IN")
        } else {
            Locale.US
        }

        try {
            tts?.apply {
                val langResult = setLanguage(locale)
                if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Filter markdown symbols from text before reading for a cleaner voice output
                    val cleanText = text
                        .replace(Regex("[*#`_~-]"), "")
                        .replace(Regex("\\(.*\\)"), "")
                        .trim()
                    speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "ZoyaTTS")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {}
        tts = null
    }
}
