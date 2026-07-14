package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.OpenAiMessage
import com.example.data.api.OpenAiRequest
import com.example.data.api.RetrofitClient
import com.example.data.model.ChatMessage
import com.example.data.pref.PreferencesManager
import com.example.data.repository.ChatRepository
import com.example.util.IntentParser
import com.example.util.SystemAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ExecuteAction(val action: SystemAction) : UiEvent()
    data class Speak(val text: String, val lang: String) : UiEvent()
}

class MainViewModel(
    private val repository: ChatRepository,
    val preferences: PreferencesManager
) : ViewModel() {

    // Chat History from Room
    val messages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI States
    val inputText = MutableStateFlow("")
    val isListening = MutableStateFlow(false)
    val partialSpeechText = MutableStateFlow("")
    val isGenerating = MutableStateFlow(false)
    val appLanguage = MutableStateFlow(preferences.language)
    val activeEngine = MutableStateFlow(preferences.aiEngine)
    val wakeWordEnabled = MutableStateFlow(preferences.wakeWordEnabled)
    val voiceEnabled = MutableStateFlow(preferences.voiceEnabled)

    // OCR & Screen analysis base64 states
    val selectedImageBase64 = MutableStateFlow<String?>(null)
    val selectedImageBitmap = MutableStateFlow<Bitmap?>(null)

    // Shared Flow for Navigation and System Actions
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    fun updateLanguage(lang: String) {
        preferences.language = lang
        appLanguage.value = lang
    }

    fun updateEngine(engine: String) {
        preferences.aiEngine = engine
        activeEngine.value = engine
    }

    fun toggleWakeWord(enabled: Boolean) {
        preferences.wakeWordEnabled = enabled
        wakeWordEnabled.value = enabled
    }

    fun toggleVoice(enabled: Boolean) {
        preferences.voiceEnabled = enabled
        voiceEnabled.value = enabled
    }

    fun selectImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            selectedImageBitmap.value = null
            selectedImageBase64.value = null
            return
        }
        selectedImageBitmap.value = bitmap
        viewModelScope.launch(Dispatchers.Default) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
            val bytes = stream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            selectedImageBase64.value = base64
        }
    }

    fun sendMessage(prompt: String, type: String = "text") {
        if (prompt.isBlank() && selectedImageBase64.value == null) return

        val trimmedPrompt = prompt.trim()
        val hasImage = selectedImageBase64.value != null
        val currentImgBase64 = selectedImageBase64.value
        val messageType = if (hasImage) "image" else type

        inputText.value = ""
        // Clear selected image
        selectedImageBitmap.value = null
        selectedImageBase64.value = null

        viewModelScope.launch {
            // 1. Insert User Message
            val userMsg = ChatMessage(
                role = "user",
                content = trimmedPrompt,
                type = messageType,
                attachmentPath = if (hasImage) "image_attachment" else null
            )
            repository.insertMessage(userMsg)

            // 2. Check for Local Intent (Pattern matching)
            val matchedAction = IntentParser.parsePrompt(trimmedPrompt)
            if (matchedAction != null) {
                handleSystemAction(matchedAction, trimmedPrompt)
                return@launch
            }

            // 3. AI Generation (Gemini or OpenAI)
            isGenerating.value = true
            try {
                val assistantReply = if (preferences.aiEngine == "Gemini") {
                    generateGeminiResponse(trimmedPrompt, currentImgBase64)
                } else {
                    generateOpenAiResponse(trimmedPrompt)
                }

                // Insert Assistant Message
                val modelMsg = ChatMessage(
                    role = "model",
                    content = assistantReply,
                    type = if (preferences.voiceEnabled) "voice" else "text"
                )
                repository.insertMessage(modelMsg)

                // Play Audio Response if voice enabled
                if (preferences.voiceEnabled) {
                    _uiEvents.emit(UiEvent.Speak(assistantReply, preferences.language))
                }

            } catch (e: Exception) {
                val errMsg = "Error: ${e.localizedMessage ?: "Failed to generate AI response"}"
                val modelMsg = ChatMessage(role = "model", content = errMsg, type = "text")
                repository.insertMessage(modelMsg)
            } finally {
                isGenerating.value = false
            }
        }
    }

    private suspend fun handleSystemAction(action: SystemAction, prompt: String) {
        val replyText = when (action) {
            is SystemAction.Camera -> if (preferences.language == "hi") "आपका कैमरा खोला जा रहा है..." else "Opening your camera..."
            is SystemAction.Gallery -> if (preferences.language == "hi") "आपकी गैलरी खोली जा रही है..." else "Opening your gallery..."
            is SystemAction.Calculator -> if (preferences.language == "hi") "कैलकुलेटर खोला जा रहा है..." else "Opening calculator..."
            is SystemAction.FileManager -> if (preferences.language == "hi") "फ़ाइल प्रबंधक खोला जा रहा है..." else "Opening file manager..."
            is SystemAction.PhoneCall -> if (preferences.language == "hi") "नंबर ${action.number} पर कॉल मिलाया जा रहा है..." else "Dialing number ${action.number}..."
            is SystemAction.SendSMS -> if (preferences.language == "hi") "${action.number} को संदेश भेजा जा रहा है..." else "Sending SMS to ${action.number}..."
            is SystemAction.SendWhatsApp -> if (preferences.language == "hi") "${action.number} को व्हाट्सएप संदेश भेजा जा रहा है..." else "Sending WhatsApp message to ${action.number}..."
            is SystemAction.YouTube -> if (preferences.language == "hi") "यूट्यूब खोला जा रहा है..." else "Opening YouTube..."
            is SystemAction.Chrome -> if (preferences.language == "hi") "क्रोम में लिंक खोला जा रहा है..." else "Opening link in browser..."
            is SystemAction.GoogleMaps -> if (preferences.language == "hi") "नक्शे पर ${action.location} की खोज की जा रही है..." else "Searching for ${action.location} on Maps..."
            is SystemAction.SetAlarm -> if (preferences.language == "hi") "${action.hour}:${action.minute} बजे का अलार्म सेट किया जा रहा है..." else "Setting alarm for ${action.hour}:${action.minute}..."
            is SystemAction.SetTimer -> if (preferences.language == "hi") "${action.seconds} सेकंड का टाइमर चालू किया जा रहा है..." else "Starting timer for ${action.seconds} seconds..."
            is SystemAction.CalendarEvent -> if (preferences.language == "hi") "इवेंट '${action.title}' बनाया जा रहा है..." else "Creating calendar event '${action.title}'..."
            is SystemAction.CreateNote -> if (preferences.language == "hi") "आपकी डायरी में नोट सुरक्षित किया जा रहा है..." else "Saving note to diary..."
        }

        // Add assistant notification message
        val assistantMsg = ChatMessage(
            role = "model",
            content = replyText,
            type = "text"
        )
        repository.insertMessage(assistantMsg)

        // Speak the reply
        if (preferences.voiceEnabled) {
            _uiEvents.emit(UiEvent.Speak(replyText, preferences.language))
        }

        // Dispatch action execution to UI layer
        _uiEvents.emit(UiEvent.ExecuteAction(action))
    }

    private suspend fun generateGeminiResponse(prompt: String, imgBase64: String?): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key missing. Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        val partsList = mutableListOf<GeminiPart>()
        if (imgBase64 != null) {
            partsList.add(GeminiPart(inlineData = com.example.data.api.GeminiInlineData(mimeType = "image/jpeg", data = imgBase64)))
        }
        partsList.add(GeminiPart(text = prompt))

        // System Instruction to shape Zoya's persona
        val sysInstruction = GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = "You are Zoya AI, a highly intuitive and friendly bilingual (Hindi + English) voice-enabled Android AI Assistant. " +
                            "Reply concisely in the user's input language. Avoid extremely long paragraphs since your replies are spoken. " +
                            "If the user asks for help in Hindi, reply in clear, sweet Devanagari Hindi. Otherwise, use English. " +
                            "Your purpose is to assist the user in general queries, web searching, summarizing, translating, and device management."
                )
            )
        )

        // Build history context (max last 6 messages)
        val historyContent = mutableListOf<GeminiContent>()
        val recentMsgs = messages.value.takeLast(6)
        recentMsgs.forEach { msg ->
            if (msg.content.isNotBlank() && !msg.content.startsWith("Error:") && !msg.content.startsWith("API Key")) {
                val role = if (msg.role == "user") "user" else "model"
                historyContent.add(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = msg.content))
                    )
                )
            }
        }
        // Add the current prompt contents as the latest item
        historyContent.add(GeminiContent(parts = partsList))

        val request = GeminiRequest(
            contents = historyContent,
            generationConfig = GeminiGenerationConfig(temperature = 0.7f),
            systemInstruction = sysInstruction
        )

        try {
            val response = RetrofitClient.service.generateGeminiContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No text response received from Zoya AI."
        } catch (e: Exception) {
            "API Call failed: ${e.localizedMessage ?: "Connection error"}. Please check your network and API key settings."
        }
    }

    private suspend fun generateOpenAiResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = preferences.openaiApiKey
        if (apiKey.isBlank()) {
            return@withContext "OpenAI API Key is missing! Please enter your custom OpenAI API Key in Settings to use ChatGPT."
        }

        // Build history context (max last 6 messages)
        val apiMessages = mutableListOf<OpenAiMessage>()
        apiMessages.add(
            OpenAiMessage(
                role = "system",
                content = "You are Zoya AI, a warm bilingual (Hindi + English) AI assistant driven by gpt-4o-mini. " +
                        "Speak concisely. Tailor your language to Hindi or English based on user's query."
            )
        )

        val recentMsgs = messages.value.takeLast(6)
        recentMsgs.forEach { msg ->
            val role = if (msg.role == "user") "user" else "assistant"
            apiMessages.add(OpenAiMessage(role = role, content = msg.content))
        }
        // Add current prompt
        apiMessages.add(OpenAiMessage(role = "user", content = prompt))

        val request = OpenAiRequest(messages = apiMessages)
        val authHeader = "Bearer $apiKey"

        try {
            val response = RetrofitClient.service.generateOpenAiContent(
                authHeader = authHeader,
                request = request
            )
            response.choices?.firstOrNull()?.message?.content
                ?: "No response received from ChatGPT."
        } catch (e: Exception) {
            "ChatGPT Call failed: ${e.localizedMessage ?: "Authentication/Network Error"}. Ensure your custom OpenAI API key is correct."
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearAllMessages()
            _uiEvents.emit(UiEvent.ShowToast("Chat history cleared!"))
        }
    }

    fun onPartialSpeech(text: String) {
        partialSpeechText.value = text
    }

    fun resetPartialSpeech() {
        partialSpeechText.value = ""
    }
}

class MainViewModelFactory(
    private val repository: ChatRepository,
    private val preferences: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
