package com.example.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appLang by viewModel.appLanguage.collectAsState()
    val activeEngine by viewModel.activeEngine.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var userName by remember { mutableStateOf(viewModel.preferences.userName) }
    var userEmail by remember { mutableStateOf(viewModel.preferences.userEmail) }

    // Save profile settings
    fun saveProfile() {
        viewModel.preferences.userName = userName
        viewModel.preferences.userEmail = userEmail
    }

    // Checking system permissions
    val microphoneGranted = checkPermission(context, android.Manifest.permission.RECORD_AUDIO)
    val cameraGranted = checkPermission(context, android.Manifest.permission.CAMERA)
    val callGranted = checkPermission(context, android.Manifest.permission.CALL_PHONE)
    val smsGranted = checkPermission(context, android.Manifest.permission.SEND_SMS)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (appLang == "hi") "आपका प्रोफ़ाइल" else "User Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        saveProfile()
                        onNavigate("home") 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        saveProfile()
                        onNavigate("home")
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Profile", tint = Color.White)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Premium Profile Card
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // User Profile Entry Fields
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (appLang == "hi") "व्यक्तिगत विवरण" else "Personal Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C6BC0)
                    )

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text(if (appLang == "hi") "आपका नाम" else "Your Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5C6BC0),
                            unfocusedBorderColor = Color(0xFF2E2E3E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text(if (appLang == "hi") "ईमेल आईडी" else "Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5C6BC0),
                            unfocusedBorderColor = Color(0xFF2E2E3E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // Statistics Metrics Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (appLang == "hi") "जोया सांख्यिकी" else "Zoya Usage Stats",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C6BC0)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (appLang == "hi") "कुल चैट संदेश" else "Total Chat Messages", color = Color.LightGray, fontSize = 13.sp)
                        Text(text = "${messages.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (appLang == "hi") "सक्रिय एआई इंजन" else "Active AI Engine", color = Color.LightGray, fontSize = 13.sp)
                        Text(text = activeEngine, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (appLang == "hi") "पसंदीदा भाषा" else "Preferred Language", color = Color.LightGray, fontSize = 13.sp)
                        Text(
                            text = if (appLang == "hi") "हिन्दी (HI)" else "English (EN)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Permissions Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (appLang == "hi") "सिस्टम अनुमतियां स्थिति" else "System Permissions Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C6BC0)
                    )

                    PermissionStatusItem(
                        title = if (appLang == "hi") "माइक्रोफ़ोन (आवाज चैट)" else "Microphone (STT Voice)",
                        granted = microphoneGranted
                    )

                    PermissionStatusItem(
                        title = if (appLang == "hi") "कैमरा (विजन टूल्स)" else "Camera (Vision Lens)",
                        granted = cameraGranted
                    )

                    PermissionStatusItem(
                        title = if (appLang == "hi") "फोन डायल (कॉल टूल्स)" else "Phone Dial (Call Tools)",
                        granted = callGranted
                    )

                    PermissionStatusItem(
                        title = if (appLang == "hi") "एसएमएस टूल्स (SMS संदेश)" else "SMS Tools (Send Text)",
                        granted = smsGranted
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStatusItem(title: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.LightGray, fontSize = 13.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (granted) Color(0x334CAF50) else Color(0x33F44336))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (granted) "Granted" else "Not Granted",
                color = if (granted) Color(0xFF81C784) else Color(0xFFE57373),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
