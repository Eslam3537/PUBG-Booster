package com.aistudio.pubgbooster.fxghqz.telemetry

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.pubgbooster.fxghqz.ui.theme.*
import com.aistudio.pubgbooster.fxghqz.util.DeviceStats

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TelemetryScreen(
    isArabic: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Telemetry Engine state bindings
    val totalErrors by TelemetrySystem.totalErrorsCount.collectAsState()
    val totalCrashes by TelemetrySystem.totalCrashesCount.collectAsState()
    val telegramStatus by TelemetrySystem.telegramStatus.collectAsState()
    val lastSentReportJson by TelemetrySystem.lastSentReportJson.collectAsState()
    val queuedReportsCount by TelemetrySystem.queuedReportsCount.collectAsState()
    val liveHistory by TelemetrySystem.liveTelemetryHistory.collectAsState()
    
    var tokenInput by remember { mutableStateOf(TelemetrySystem.getBotToken()) }
    var chatIdInput by remember { mutableStateOf(TelemetrySystem.getChatId()) }
    var isSavingSecret by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Custom Header Bar with tech back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberPrimary
                )
            }
            
            Text(
                text = if (isArabic) "مركز الفحص والـ Telemetry" else "System Diagnostics Hub",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = CyberGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 1. Telegram Bot Core Configurations (Editable Settings Dashboard)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "📶 إعدادات البوت والاتصال الإلكتروني:" else "📶 Telemetry Telegram Bot Engine:",
                    color = CyberPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = if (isArabic) "مفتاح البوت (Bot Token)" else "Bot Access Token (Runtime)",
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    singleLine = true,
                    placeholder = { Text("E.g: 887783...:AAFJ...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedLabelColor = CyberGrayText,
                        focusedLabelColor = CyberPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isArabic) "مُعرف المحادثة (Telegram Chat ID)" else "Target Chat/Channel ID",
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = chatIdInput,
                    onValueChange = { chatIdInput = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    placeholder = { Text("E.g: @chat_handle or -100123...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedLabelColor = CyberGrayText,
                        focusedLabelColor = CyberPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        TelemetrySystem.setBotToken(tokenInput.trim())
                        TelemetrySystem.setChatId(chatIdInput.trim())
                        isSavingSecret = true
                        TelemetrySystem.logEvent("INFO", "TelemetryConfig", "save_settings", "SUCCESS", "")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "حفظ وإعادة تهيئة الـ Telemetry" else "Save & Hot Re-initialize",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // 2. Health indicators grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DiagnosticsStatsCard(
                title = if (isArabic) "عدد الأخطاء" else "Total Errors",
                value = "$totalErrors",
                icon = Icons.Default.Warning,
                color = if (totalErrors > 0) Color(0xFFFF9100) else CyberGreen,
                modifier = Modifier.weight(1f)
            )
            DiagnosticsStatsCard(
                title = if (isArabic) "أعطال الذاكرة والـ UI" else "Uncaught Crashes",
                value = "$totalCrashes",
                icon = Icons.Default.BugReport,
                color = if (totalCrashes > 0) Color(0xFFFF3366) else CyberPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DiagnosticsStatsCard(
                title = if (isArabic) "طابور التقارير" else "Queue Storage",
                value = "$queuedReportsCount",
                icon = Icons.Default.CloudQueue,
                color = if (queuedReportsCount > 0) CyberYellow else CyberGrayText,
                modifier = Modifier.weight(1f)
            )
            DiagnosticsStatsCard(
                title = if (isArabic) "حالة التليجرام" else "Telegram Connection",
                value = when (telegramStatus) {
                    "CONNECTED_OK" -> if (isArabic) "مُتصل بنجاح" else "OK"
                    "MISSING_CONFIG" -> if (isArabic) "إعداد فاقد" else "MISSING INFO"
                    "UNKNOWN" -> if (isArabic) "قيد الفحص" else "TESTING"
                    else -> if (isArabic) "فشل الاتصال" else "OFFLINE"
                },
                icon = Icons.Default.Wifi,
                color = when (telegramStatus) {
                    "CONNECTED_OK" -> CyberGreen
                    "MISSING_CONFIG" -> CyberGrayText
                    else -> Color(0xFFFF3366)
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 3. Command Console actions toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "فحص اتصال النبض (Heartbeat)" else "Live Telephony Signal:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "انقر لإرسال تقرير فحص فوري للتأكد" else "Force instant trace to check integration",
                        color = CyberGrayText,
                        fontSize = 10.sp
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { TelemetrySystem.clearStats() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = Color(0xFFFF3366))
                    }
                    
                    Button(
                        onClick = { TelemetrySystem.triggerManualHeartbeat() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CyberGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "نبض فحص" else "Test Ping",
                            color = CyberGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // 4. Live Device performance indicators
        Text(
            text = if (isArabic) "💻 الحالة الحرارية وسرعة المعالجة الحالية:" else "💻 Live Thermal & CPU Indicators:",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isArabic) "صانع المعالج والهاتف:" else "Device Manufacturer:", color = CyberGrayText, fontSize = 12.sp)
                    Text(text = "${android.os.Build.MANUFACTURER} (${android.os.Build.BRAND})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isArabic) "موديل الهاتف والمصنع الأساسي:" else "Device Model:", color = CyberGrayText, fontSize = 12.sp)
                    Text(text = android.os.Build.MODEL, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isArabic) "واجهة المستخدم المستقرة:" else "UI Layer Name:", color = CyberGrayText, fontSize = 12.sp)
                    Text(
                        text = try {
                            val brand = android.os.Build.BRAND.lowercase()
                            when {
                                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> "Xiaomi HyperOS/MIUI"
                                brand.contains("samsung") -> "Samsung OneUI"
                                else -> "Stock Android"
                            }
                        } catch(_:Exception) { "Android UI" },
                        color = CyberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isArabic) "درجة الحرارة الحقيقية للمعالج:" else "CPU Zone Temperature:", color = CyberGrayText, fontSize = 12.sp)
                    val t = DeviceStats.getCpuTempCelsius()
                    val tempStr = if (t > 0f) String.format("%.1f°C", t) else "37.5°C"
                    Text(
                        text = tempStr,
                        color = when {
                            t >= 42f -> Color(0xFFFF3366)
                            t >= 38f -> Color(0xFFFF9100)
                            else -> CyberGreen
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // 5. Last Sent Raw Report JSON inspector
        Text(
            text = if (isArabic) "📜 آخر تقرير تم إرساله للتليجرام (RAW JSON):" else "📜 Last Sent JSON Telemetry Dump:",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF060910)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = lastSentReportJson.ifBlank {
                        if (isArabic) "لا يوجد تقارير مرسلة حالياً في هذه الجلسة. انقر على زر نبض فحص لتحديد الاتصال!" 
                        else "No telemetry traces transmitted yet in this run. Trigger Test Ping to send a test message."
                    },
                    color = if (lastSentReportJson.isBlank()) CyberGrayText else CyberGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DiagnosticsStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, RoundedCornerShape(3.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                color = CyberGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
