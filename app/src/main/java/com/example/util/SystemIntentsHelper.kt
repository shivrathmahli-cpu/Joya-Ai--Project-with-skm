package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.widget.Toast
import java.net.URLEncoder

object SystemIntentsHelper {

    fun openCamera(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback for newer Android versions where resolveActivity returns null
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera app not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openGallery(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gallery not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeCall(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialer not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSMS(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${phoneNumber.trim()}")).apply {
            putExtra("sms_body", message)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsApp(context: Context, phoneNumber: String, message: String) {
        val encodedMsg = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }
        // Clean phone number (needs country code but no '+' or spaces for API)
        val cleanPhone = phoneNumber.filter { it.isDigit() }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openYouTube(context: Context, query: String) {
        val url = if (query.isNotEmpty()) {
            "https://www.youtube.com/results?search_query=${URLEncoder.encode(query, "UTF-8")}"
        } else {
            "https://www.youtube.com"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "YouTube app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openChrome(context: Context, url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Browser not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGoogleMaps(context: Context, location: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${URLEncoder.encode(location, "UTF-8")}"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Google Maps not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCalculator(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Backup package launcher for custom calculators
            val fallbackIntent = Intent().apply {
                setClassName("com.android.calculator2", "com.android.calculator2.Calculator")
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Calculator not found on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setAlarm(context: Context, label: String, hour: Int, minutes: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Alarm app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun setTimer(context: Context, label: String, seconds: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Clock/Timer app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun createCalendarEvent(context: Context, title: String, description: String, startTime: Long, endTime: Long) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Calendar app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun createNote(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Save Note with:"))
        } catch (e: Exception) {
            Toast.makeText(context, "Notes app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFileManager(context: Context) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open Files with:"))
        } catch (e: Exception) {
            Toast.makeText(context, "File Manager not found", Toast.LENGTH_SHORT).show()
        }
    }
}
