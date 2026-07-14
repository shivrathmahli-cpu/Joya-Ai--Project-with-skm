package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.UiEvent
import com.example.util.SpeechToTextHelper
import com.example.util.SystemIntentsHelper
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val partialSpeech by viewModel.partialSpeechText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val appLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val selectedImageBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // 1. Text To Speech helper
    var ttsHelper by remember { mutableStateOf<TextToSpeechHelper?>(null) }
    
    // 2. Speech To Text helper
    val sttHelper = remember {
        SpeechToTextHelper(
            context = context,
            onResult = { result ->
                viewModel.onPartialSpeech(result)
                viewModel.sendMessage(result)
                viewModel.isListening.value = false
            },
            onError = { error ->
                viewModel.onPartialSpeech("")
                viewModel.isListening.value = false
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            },
            onPartialResult = { partial ->
                viewModel.onPartialSpeech(partial)
            }
        )
    }

    // Initialize TTS and listen for events
    LaunchedEffect(Unit) {
        ttsHelper = TextToSpeechHelper(context) {
            // TTS is ready
        }

        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.Speak -> {
                    if (viewModel.preferences.voiceEnabled) {
                        ttsHelper?.speak(event.text, event.lang)
                    }
                }
                is UiEvent.ExecuteAction -> {
                    // Launch device-level Intents
                    when (val action = event.action) {
                        is com.example.util.SystemAction.Camera -> SystemIntentsHelper.openCamera(context)
                        is com.example.util.SystemAction.Gallery -> SystemIntentsHelper.openGallery(context)
                        is com.example.util.SystemAction.Calculator -> SystemIntentsHelper.openCalculator(context)
                        is com.example.util.SystemAction.FileManager -> SystemIntentsHelper.openFileManager(context)
                        is com.example.util.SystemAction.PhoneCall -> SystemIntentsHelper.makeCall(context, action.number)
                        is com.example.util.SystemAction.SendSMS -> SystemIntentsHelper.sendSMS(context, action.number, action.message)
                        is com.example.util.SystemAction.SendWhatsApp -> SystemIntentsHelper.sendWhatsApp(context, action.number, action.message)
                        is com.example.util.SystemAction.YouTube -> SystemIntentsHelper.openYouTube(context, action.query)
                        is com.example.util.SystemAction.Chrome -> SystemIntentsHelper.openChrome(context, action.url)
                        is com.example.util.SystemAction.GoogleMaps -> SystemIntentsHelper.openGoogleMaps(context, action.location)
                        is com.example.util.SystemAction.SetAlarm -> SystemIntentsHelper.setAlarm(context, action.label, action.hour, action.minute)
                        is com.example.util.SystemAction.SetTimer -> SystemIntentsHelper.setTimer(context, action.label, action.seconds)
                        is com.example.util.SystemAction.CalendarEvent -> SystemIntentsHelper.createCalendarEvent(context, action.title, action.description, System.currentTimeMillis(), System.currentTimeMillis() + 3600000)
                        is com.example.util.SystemAction.CreateNote -> SystemIntentsHelper.createNote(context, action.text)
                    }
                }
            }
        }
    }

    // Scroll to bottom on load or new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sttHelper.stopListening()
            ttsHelper?.shutdown()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.selectImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Zoya AI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                if (isGenerating) {
                                    if (appLang == "hi") "विचार कर रही हूँ..." else "Thinking..."
                                } else {
                                    if (appLang == "hi") "सक्रिय" else "Online"
                                },
                                fontSize = 11.sp,
                                color = if (isGenerating) Color(0xFFFF9800) else Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.White)
                    }
                    IconButton(onClick = { onNavigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        },
        containerColor = Color(0xFF0D0D14)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat Logs
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF2E2E3E)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (appLang == "hi") "नमस्ते! मैं आपकी जोया सहायक हूँ।" else "Hello! I am your Zoya AI Assistant.",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (appLang == "hi") 
                                    "मुझसे कुछ भी पूछें या 'Hey Zoya' बोलकर कोई कार्य करवाएं।" 
                                else 
                                    "Ask me anything, or tap the microphone to trigger system commands.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        ChatBubble(message = message, appLang = appLang)
                    }
                }

                // Custom Thinking/Typing dots
                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    TypingAnimation()
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Speech Transcription Captions
            AnimatedVisibility(
                visible = isListening && partialSpeech.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x995C6BC0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = partialSpeech,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Panel for Input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    
                    // Selected Image attachment preview
                    if (selectedImageBitmap != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color(0xFF0F0F16), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Attachment preview",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (appLang == "hi") "तस्वीर संलग्न है" else "Photo Attached",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.selectImage(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }

                    // Input Text / Buttons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment buttons
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = Color.LightGray)
                        }

                        IconButton(onClick = { onNavigate("camera_ocr") }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.LightGray)
                        }

                        // Text Field
                        TextField(
                            value = inputText,
                            onValueChange = { viewModel.inputText.value = it },
                            placeholder = {
                                Text(
                                    text = if (appLang == "hi") "बोलो या यहाँ टाइप करें..." else "Talk or type here...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Transparent),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4
                        )

                        // Action Button (Send or Microphone)
                        if (inputText.isNotBlank() || selectedImageBitmap != null) {
                            IconButton(
                                onClick = { viewModel.sendMessage(inputText) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF5C6BC0), CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            // Mic Button with pulsing indicator
                            IconButton(
                                onClick = {
                                    if (isListening) {
                                        sttHelper.stopListening()
                                        viewModel.isListening.value = false
                                        viewModel.onPartialSpeech("")
                                    } else {
                                        ttsHelper?.stop() // Stop TTS reading when user wants to talk
                                        viewModel.isListening.value = true
                                        viewModel.onPartialSpeech("")
                                        sttHelper.startListening(appLang)
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isListening) Color(0xFFE94057) else Color(0xFF5C6BC0),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Voice Waves visualizer row if active
                    AnimatedVisibility(
                        visible = isListening,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            VoiceAnimationWave()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, appLang: String) {
    val isModel = message.role == "model"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isModel) 2.dp else 16.dp,
                bottomEnd = if (isModel) 16.dp else 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isModel) Color(0xFF1E1E2C) else Color(0xFF3F51B5)
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // If attachment path present, show mock thumbnail (for prototype visual richness)
                if (message.attachmentPath != null) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attachment image",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.type == "voice") {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Vocalized response",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isModel) "Zoya" else "You",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotCount = 3
    val dots = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0f at (index * 150)
                    0.5f at (index * 150 + 300)
                    1f at (index * 150 + 600)
                },
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$index"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Zoya is typing", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        dots.forEach { progress ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = (-4 * progress.value).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE94057))
            )
        }
    }
}

@Composable
fun VoiceAnimationWave() {
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val barCount = 6
    val bars = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = 28f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 800
                    4f at (index * 100)
                    28f at (index * 100 + 200)
                    4f at (index * 100 + 400)
                },
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        bars.forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8A2387), Color(0xFFE94057))
                        )
                    )
            )
        }
    }
}
