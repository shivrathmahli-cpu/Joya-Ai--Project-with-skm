package com.example.util

sealed class SystemAction {
    object Camera : SystemAction()
    object Gallery : SystemAction()
    data class PhoneCall(val number: String) : SystemAction()
    data class SendSMS(val number: String, val message: String) : SystemAction()
    data class SendWhatsApp(val number: String, val message: String) : SystemAction()
    data class YouTube(val query: String) : SystemAction()
    data class Chrome(val url: String) : SystemAction()
    data class GoogleMaps(val location: String) : SystemAction()
    object Calculator : SystemAction()
    data class SetAlarm(val label: String, val hour: Int, val minute: Int) : SystemAction()
    data class SetTimer(val label: String, val seconds: Int) : SystemAction()
    data class CalendarEvent(val title: String, val description: String) : SystemAction()
    data class CreateNote(val text: String) : SystemAction()
    object FileManager : SystemAction()
}

object IntentParser {

    fun parsePrompt(prompt: String): SystemAction? {
        val text = prompt.lowercase().trim()

        // 1. Camera
        if (text.contains("camera") || text.contains("कैमरा") || text.contains("फोटो खींचो") || text.contains("photo khicho")) {
            return SystemAction.Camera
        }

        // 2. Gallery
        if (text.contains("gallery") || text.contains("गैलरी") || text.contains("गैलेरी") || text.contains("फ़ोटो खोलो")) {
            return SystemAction.Gallery
        }

        // 3. Calculator
        if (text.contains("calculator") || text.contains("कैलकुलेटर") || text.contains("हिसाब")) {
            return SystemAction.Calculator
        }

        // 4. File Manager
        if (text.contains("file manager") || text.contains("फाइल") || text.contains("फोल्डर") || text.contains("documents")) {
            return SystemAction.FileManager
        }

        // 5. WhatsApp
        if (text.contains("whatsapp") || text.contains("व्हाट्सएप") || text.contains("व्हाट्सऐप")) {
            val phone = extractPhoneNumber(text) ?: "9876543210"
            val body = extractMessageAfterKeyword(text, listOf("saying", "लिखो", "बोलो", "message")) ?: "Hello from Zoya AI"
            return SystemAction.SendWhatsApp(phone, body)
        }

        // 6. SMS
        if (text.contains("sms") || text.contains("एसएमएस") || text.contains("संदेश")) {
            val phone = extractPhoneNumber(text) ?: "1234567890"
            val body = extractMessageAfterKeyword(text, listOf("saying", "लिखो", "बोलो", "message")) ?: "Hello"
            return SystemAction.SendSMS(phone, body)
        }

        // 7. Phone Call
        if (text.contains("call") || text.contains("कॉल") || text.contains("फ़ोन मिलाओ") || text.contains("phone milao")) {
            val phone = extractPhoneNumber(text) ?: "9876543210"
            return SystemAction.PhoneCall(phone)
        }

        // 8. YouTube
        if (text.contains("youtube") || text.contains("यूट्यूब")) {
            val query = extractQueryAfterKeyword(text, listOf("search", "खोजो", "चलाओ", "play")) ?: ""
            return SystemAction.YouTube(query)
        }

        // 9. Alarm
        if (text.contains("alarm") || text.contains("अलार्म")) {
            val time = extractTime(text)
            return SystemAction.SetAlarm("Zoya AI Alarm", time.first, time.second)
        }

        // 10. Timer
        if (text.contains("timer") || text.contains("टाइमर")) {
            val sec = extractNumber(text) ?: 30
            return SystemAction.SetTimer("Zoya AI Timer", sec)
        }

        // 11. Google Maps
        if (text.contains("maps") || text.contains("map") || text.contains("मैप्स") || text.contains("दिशा") || text.contains("रास्ता") || text.contains("location")) {
            val location = extractQueryAfterKeyword(text, listOf("for", "to", "का", "के लिए")) ?: "Delhi"
            return SystemAction.GoogleMaps(location)
        }

        // 12. Calendar Event
        if (text.contains("calendar") || text.contains("कैलेंडर") || text.contains("इवेंट") || text.contains("event")) {
            val title = extractQueryAfterKeyword(text, listOf("event", "इवेंट", "meeting", "meeting with")) ?: "Zoya AI Event"
            return SystemAction.CalendarEvent(title, "Created by Zoya AI Assistant")
        }

        // 13. Create Note
        if (text.contains("note") || text.contains("नोट") || text.contains("लिखो") || text.contains("diary")) {
            val noteText = extractQueryAfterKeyword(text, listOf("note", "लिखो", "write")) ?: text
            return SystemAction.CreateNote(noteText)
        }

        // 14. Chrome / Web Search
        if (text.startsWith("http") || text.contains(".com") || text.contains(".org") || text.contains(".in")) {
            val words = text.split(" ")
            val url = words.firstOrNull { it.startsWith("http") || it.contains(".") } ?: "https://google.com"
            return SystemAction.Chrome(url)
        }

        return null
    }

    private fun extractPhoneNumber(text: String): String? {
        val regex = Regex("\\d{10}")
        val match = regex.find(text)
        return match?.value
    }

    private fun extractNumber(text: String): Int? {
        val regex = Regex("\\d+")
        val match = regex.find(text)
        return match?.value?.toIntOrNull()
    }

    private fun extractTime(text: String): Pair<Int, Int> {
        // Look for pattern like "7:30" or "19:00"
        val colonRegex = Regex("(\\d{1,2}):(\\d{2})")
        val match = colonRegex.find(text)
        if (match != null) {
            val h = match.groupValues[1].toInt()
            val m = match.groupValues[2].toInt()
            return Pair(h, m)
        }
        // Extract plain numbers (e.g., "alarm at 7")
        val numberRegex = Regex("\\d+")
        val matches = numberRegex.findAll(text).toList()
        if (matches.isNotEmpty()) {
            val h = matches[0].value.toInt()
            val m = if (matches.size > 1) matches[1].value.toInt() else 0
            return Pair(h, m)
        }
        return Pair(8, 0) // Default 8:00 AM
    }

    private fun extractMessageAfterKeyword(text: String, keywords: List<String>): String? {
        for (kw in keywords) {
            val idx = text.indexOf(kw)
            if (idx != -1) {
                val sub = text.substring(idx + kw.length).trim()
                if (sub.isNotEmpty()) {
                    return sub
                }
            }
        }
        return null
    }

    private fun extractQueryAfterKeyword(text: String, keywords: List<String>): String? {
        for (kw in keywords) {
            val idx = text.indexOf(kw)
            if (idx != -1) {
                val sub = text.substring(idx + kw.length).trim()
                if (sub.isNotEmpty()) {
                    return sub
                }
            }
        }
        return null
    }
}
