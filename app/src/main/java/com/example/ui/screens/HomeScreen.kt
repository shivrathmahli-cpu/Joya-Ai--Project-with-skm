package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

data class QuickActionCard(
    val titleEn: String,
    val titleHi: String,
    val subtitleEn: String,
    val subtitleHi: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userName = remember { viewModel.preferences.userName }
    val appLang by viewModel.appLanguage.collectAsState()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.selectImage(bitmap)
                onNavigate("chat")
                viewModel.sendMessage(
                    if (appLang == "hi") "इस इमेज की व्याख्या करें" else "Describe this image",
                    "image"
                )
            } catch (e: Exception) {
                viewModel.selectImage(null)
            }
        }
    }

    val quickActions = listOf(
        QuickActionCard(
            "Voice Conversation", "आवाज बातचीत",
            "Hey Zoya: Real-time talk", "जोया से सीधी बात करें",
            Icons.Default.Mic, Color(0xFF3F51B5), "chat_voice"
        ),
        QuickActionCard(
            "Zoya Vision Cam", "जोया विजन कैमरा",
            "OCR & Object Analysis", "फोटो से टेक्स्ट और ऑब्जेक्ट पहचानें",
            Icons.Default.CameraAlt, Color(0xFF009688), "camera_ocr"
        ),
        QuickActionCard(
            "Upload Image/Document", "फ़ाइल/फोटो अपलोड करें",
            "Analyze photos or PDFs", "इमेज या पीडीएफ का सारांश",
            Icons.Default.Description, Color(0xFFFF9800), "gallery_upload"
        ),
        QuickActionCard(
            "System Action Panel", "सिस्टम एक्शन टूल्स",
            "Alarms, Calls, SMS, Maps", "अलार्म, फोन और व्हाट्सएप टूल्स",
            Icons.Default.Build, Color(0xFFE91E63), "tools_info"
        )
    )

    // Pulsing animation for the Zoya glowing core
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val gradient = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1E2F), Color(0xFF0D0D14)),
                    center = Offset(size.width / 2, size.height / 3),
                    radius = size.maxDimension / 1.2f
                )
                drawRect(brush = gradient)
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (appLang == "hi") "नमस्ते, $userName!" else "Namaste, $userName!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = if (appLang == "hi") "मैं आपकी क्या मदद करूँ?" else "How can I assist you today?",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
                
                IconButton(
                    onClick = { onNavigate("profile") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2C2C3C), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Assistant Core
            Box(
                modifier = Modifier
                    .size(160.dp * pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x665C6BC0),
                                Color(0x223F51B5),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable {
                        onNavigate("chat")
                    },
                contentAlignment = Alignment.Center
            ) {
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
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Zoya AI Core Logo",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            Text(
                text = if (appLang == "hi") "Hey Zoya: सक्रिय सहायक" else "Hey Zoya: Active Assistant",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE94057),
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick actions Section Grid
            Text(
                text = if (appLang == "hi") "त्वरित विकल्प" else "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(quickActions) { action ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable {
                                when (action.route) {
                                    "chat_voice" -> {
                                        onNavigate("chat")
                                        // Let Chat screen trigger voice mic automatically
                                    }
                                    "gallery_upload" -> {
                                        launcher.launch("image/*")
                                    }
                                    "tools_info" -> {
                                        onNavigate("chat")
                                        viewModel.sendMessage(
                                            if (appLang == "hi") "आप कौन कौन से सिस्टम एक्शन कर सकते हैं?" else "What system actions can you perform?"
                                        )
                                    }
                                    else -> {
                                        onNavigate(action.route)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E2C)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(action.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.color
                                )
                            }
                            
                            Column {
                                Text(
                                    text = if (appLang == "hi") action.titleHi else action.titleEn,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (appLang == "hi") action.subtitleHi else action.subtitleEn,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Floating Chat Entry
            Button(
                onClick = { onNavigate("chat") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C6BC0)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (appLang == "hi") "जोया चैट रूम खोलें" else "Open Zoya Chat Room",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
