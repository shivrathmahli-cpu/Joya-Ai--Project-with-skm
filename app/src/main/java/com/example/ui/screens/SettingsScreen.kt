package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appLang by viewModel.appLanguage.collectAsState()
    val activeEngine by viewModel.activeEngine.collectAsState()
    val wakeWordEnabled by viewModel.wakeWordEnabled.collectAsState()
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    
    var openAiKey by remember { mutableStateOf(viewModel.preferences.openaiApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (appLang == "hi") "व्यवस्था सेटिंग्स" else "System Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Localization
            Text(
                text = if (appLang == "hi") "भाषा और वार्तालाप" else "Language & Conversation",
                color = Color(0xFF5C6BC0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Language Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (appLang == "hi") "मुख्य भाषा" else "Primary Language",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        // Toggle Buttons (EN / HI)
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF0F0F16), RoundedCornerShape(12.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (appLang == "en") Color(0xFF5C6BC0) else Color.Transparent)
                                    .clickable { viewModel.updateLanguage("en") }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("EN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (appLang == "hi") Color(0xFF5C6BC0) else Color.Transparent)
                                    .clickable { viewModel.updateLanguage("hi") }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("हिन्दी", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2E2E3E))

                    // Voice output (TTS) toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (appLang == "hi") "आवाज प्रतिक्रिया (TTS)" else "Voice Responses (TTS)",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = { viewModel.toggleVoice(it) }
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2E2E3E))

                    // Wake Word Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hearing, contentDescription = null, tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (appLang == "hi") "हे जोया वेक वर्ड (Hey Zoya)" else "Wake Word (Hey Zoya)",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = { viewModel.toggleWakeWord(it) }
                        )
                    }
                }
            }

            // Section 2: AI Core Configurations
            Text(
                text = if (appLang == "hi") "एआई इंजन विन्यास" else "AI Engine Config",
                color = Color(0xFF5C6BC0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Engine Picker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (appLang == "hi") "एआई प्रदाता" else "AI Provider Engine",
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .background(Color(0xFF0F0F16), RoundedCornerShape(12.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activeEngine == "Gemini") Color(0xFF5C6BC0) else Color.Transparent)
                                    .clickable { viewModel.updateEngine("Gemini") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Gemini", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activeEngine == "ChatGPT") Color(0xFF5C6BC0) else Color.Transparent)
                                    .clickable { viewModel.updateEngine("ChatGPT") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("ChatGPT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Conditional OpenAI API Key configuration fields
                    if (activeEngine == "ChatGPT") {
                        HorizontalDivider(color = Color(0xFF2E2E3E))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (appLang == "hi") "अपना कस्टम OpenAI API Key दर्ज करें" else "Enter custom OpenAI API Key",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            OutlinedTextField(
                                value = openAiKey,
                                onValueChange = {
                                    openAiKey = it
                                    viewModel.preferences.openaiApiKey = it
                                },
                                placeholder = { Text("sk-...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle key visibility"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF5C6BC0),
                                    unfocusedBorderColor = Color(0xFF2E2E3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Text(
                                text = if (appLang == "hi") 
                                    "आपका OpenAI की सुरक्षित रूप से आपके डिवाइस में स्टोर किया जाता है।" 
                                else 
                                    "Your API key is securely saved locally on this device and called via direct REST requests.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Section 3: Cleaning
            Text(
                text = if (appLang == "hi") "इतिहास और रखरखाव" else "History & Maintenance",
                color = Color(0xFF5C6BC0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { viewModel.clearChatHistory() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = if (appLang == "hi") "चैट इतिहास खाली करें" else "Clear All Chat History",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
