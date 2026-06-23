package com.aistudio.pubgbooster.fxghqz.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.pubgbooster.fxghqz.BoostViewModel
import com.aistudio.pubgbooster.fxghqz.viewmodel.NetworkViewModel
import com.aistudio.pubgbooster.fxghqz.viewmodel.NetworkUiState
import com.aistudio.pubgbooster.fxghqz.viewmodel.WifiBandPreference
import com.aistudio.pubgbooster.fxghqz.viewmodel.OptimizationImpact
import com.aistudio.pubgbooster.fxghqz.util.PingMonitor
import com.aistudio.pubgbooster.fxghqz.util.NetworkAnalyzer
import com.aistudio.pubgbooster.fxghqz.ui.theme.*
import com.aistudio.pubgbooster.fxghqz.ServerCardGridItem
import com.aistudio.pubgbooster.fxghqz.RadarScanner
import kotlinx.coroutines.launch

@Composable
fun NetworkScreen(
    isArabic: Boolean,
    viewModel: BoostViewModel,
    networkViewModel: NetworkViewModel
) {
    val context = LocalContext.current
    val state by networkViewModel.state.collectAsState()
    val middleEastStats by PingMonitor.middleEastStats.collectAsState()
    val europeStats by PingMonitor.europeStats.collectAsState()
    val asiaStats by PingMonitor.asiaStats.collectAsState()
    val globalStats by PingMonitor.globalStats.collectAsState()

    val networkInfo by NetworkAnalyzer.networkInfo.collectAsState()
    val isAnalyzing by NetworkAnalyzer.isAnalyzing.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        networkViewModel.detectCurrentBand(context)
        networkViewModel.scanBandCongestion(context)
        networkViewModel.runPreflightCheck()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // ── SECTION 1: Game Servers Ping Grid (Replicated/Preserved) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "خوادم ألعاب PUBG (مباشر)" else "PUBG Game Servers (Live)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Green "Live" active badge
            Row(
                modifier = Modifier
                    .background(Color(0xFF0A2B1D), RoundedCornerShape(12.dp))
                    .border(1.dp, CyberGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(CyberGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "مباشر" else "Live",
                    color = CyberGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 2x2 Grid Server Columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ServerCardGridItem(
                    flagEmoji = "🇦🇪",
                    serverName = if (isArabic) "الشرق الأوسط" else "Middle East",
                    stats = middleEastStats,
                    isArabic = isArabic,
                    defaultPing = 15,
                    defaultStats = Triple(12, 15, 22),
                    graphColor = CyberGreen,
                    statusLabel = if (isArabic) "ممتاز" else "Excellent",
                    isSecure = true,
                    statusColor = CyberGreen,
                    maxGraphValueLabel = "100ms",
                    midGraphValueLabel = "50ms",
                    defaultHistory = listOf(14, 15, 13, 16, 15, 14, 18, 15, 13, 14, 16, 15, 12, 15, 14, 15)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ServerCardGridItem(
                    flagEmoji = "🇪🇺",
                    serverName = if (isArabic) "أوروبا" else "Europe",
                    stats = europeStats,
                    isArabic = isArabic,
                    defaultPing = 65,
                    defaultStats = Triple(48, 65, 92),
                    graphColor = CyberYellow,
                    statusLabel = if (isArabic) "كفؤ" else "Proficient",
                    isSecure = true,
                    statusColor = CyberYellow,
                    maxGraphValueLabel = "100ms",
                    midGraphValueLabel = "50ms",
                    defaultHistory = listOf(60, 65, 68, 62, 65, 70, 64, 65, 68, 72, 65, 63, 67, 65, 62, 65)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ServerCardGridItem(
                    flagEmoji = "🇨🇳",
                    serverName = if (isArabic) "آسيا" else "Asia",
                    stats = asiaStats,
                    isArabic = isArabic,
                    defaultPing = 137,
                    defaultStats = Triple(98, 137, 188),
                    graphColor = CyberAccent,
                    statusLabel = if (isArabic) "غير مستقر" else "Unstable",
                    isSecure = false,
                    statusColor = CyberAccent,
                    maxGraphValueLabel = "200ms",
                    midGraphValueLabel = "100ms",
                    defaultHistory = listOf(130, 137, 140, 135, 137, 145, 132, 137, 138, 142, 137, 135, 139, 137, 131, 137)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ServerCardGridItem(
                    globeIcon = true,
                    serverName = if (isArabic) "الشبكة العالمية" else "Global Network",
                    stats = globalStats,
                    isArabic = isArabic,
                    defaultPing = 266,
                    defaultStats = Triple(210, 266, 356),
                    graphColor = CyberAccent,
                    statusLabel = if (isArabic) "غير مستقر" else "Unstable",
                    isSecure = false,
                    statusColor = CyberAccent,
                    maxGraphValueLabel = "400ms",
                    midGraphValueLabel = "200ms",
                    defaultHistory = listOf(250, 266, 270, 260, 266, 280, 255, 266, 268, 275, 266, 262, 271, 266, 258, 266)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SECTION 3: Xiaomi / Redmi / POCO Warning Pre-flight Card ──
        if (state.deviceWarnings.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberYellow.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221A0F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "🔧 متطلبات ضبط جهاز Xiaomi / Redmi / POCO" else "🔧 Xiaomi/Redmi/POCO Adjustment Needed",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    state.deviceWarnings.forEach { warning ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "• ",
                                color = CyberYellow,
                                fontSize = 14.sp
                            )
                            Text(
                                text = warning,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── SECTION 4: Preferred Wi-Fi Band Select Card (Segmented/Chip UI) ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "📡 تفضيل نطاق الواي فاي المفضل" else "📡 Preferred Wi-Fi Band Selection",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic)
                        "تغيير الترددات إجبارياً محظور بواسطة نظام الأندرويد. نقوم بكشف وإرشاد توجيه الإشارة الخاص بجهازك لتفادي الذبذبة."
                    else "Direct frequency injection is restricted in newer Android versions. We detect, match, and guide your active routing interface.",
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Segmented / Multi-Choice Buttons Group
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WifiBandPreference.values().forEach { pref ->
                        val isSelected = state.bandPreference == pref
                        Button(
                            onClick = { networkViewModel.setBandPreference(pref, context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF1D5CFF) else Color(0xFF1B2A47)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isArabic) pref.labelAr else pref.labelEn,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connected Band HUD Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "النطاق المتصل حالياً:" else "Current Connected Band:",
                        color = CyberGrayText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = state.currentConnectedBand,
                        color = if (state.bandMismatchWarning) CyberYellow else CyberGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (state.bandMismatchWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF221A0F), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic)
                                "⚠️ تنبيه تعارض: التردد الحالي لا يطابق خيار النطاق المفضل!"
                                else "⚠️ Preference mismatch: Connected band does not match preferred band!",
                            color = CyberYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SECTION 5: Nearby Networks Congestion Indicator ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "📉 ازدحام الشبكات المحلية والتشويش" else "📉 Local Radio Symmetrical Interference",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "الشبكات القريبة المكتشفة:" else "Nearby Networks Detected:",
                        color = CyberGrayText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (state.nearbyCongestionCount == -1) "..." else "${state.nearbyCongestionCount}",
                        color = CyberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مستوى الازدحام اللاسلكي:" else "RF Congestion Level:",
                        color = CyberGrayText,
                        fontSize = 11.sp
                    )
                    val congestionTextValues = if (isArabic) {
                        listOf("منخفض (مثالي)", "متوسط (مقبول)", "عالي جداً (مزدحم)")
                    } else {
                        listOf("Low (Optimal)", "Moderate (Normal)", "High (Heavy Jitter)")
                    }
                    val levelIdx = when {
                        state.nearbyCongestionCount in 0..4 -> 0
                        state.nearbyCongestionCount in 5..9 -> 1
                        state.nearbyCongestionCount >= 10 -> 2
                        else -> 1
                    }
                    Text(
                        text = congestionTextValues[levelIdx],
                        color = when (levelIdx) {
                            0 -> CyberGreen
                            1 -> CyberYellow
                            else -> CyberAccent
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "النطاق اللاسلكي المقترح:" else "Recommended Wireless Band:",
                        color = CyberGrayText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (state.nearbyCongestionCount >= 8) "5GHz / 6GHz" else "Dual Band Auto",
                        color = CyberGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bluetooth interference switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "🚫 تعطيل البلوتوث لتقليل تداخل الموجات" else "🚫 Stop Co-existence Bluetooth Radio",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic)
                                "يقوم بإطفاء البلوتوث لمنع مشاركة موجات 2.4GHz مع راديو الواي فاي."
                                else "Auto toggles Bluetooth off during optimization. Avoids packets collisions on 2.4G.",
                            color = CyberGrayText,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = state.bluetoothOffEnabled,
                        onCheckedChange = { networkViewModel.toggleBluetoothOffEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberPrimary,
                            checkedTrackColor = Color(0xFF1B2A47)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SECTION 6: Network Response Optimization Actions ──
        Button(
            onClick = { networkViewModel.optimizeNetwork() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE02B37), Color(0xFFC01F2B))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            enabled = !state.isOptimizing
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (state.isOptimizing) {
                                if (isArabic) "جاري تهيئة شبكة الاستجابة..." else "Applying Response Settings..."
                            } else {
                                if (isArabic) "تفعيل شبكة الاستجابة الفورية (Hit Registration)" else "Enable Instant Hit-Registration Network"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row {
                            Text(
                                text = if (isArabic) "حزم مستقرة" else "Stable Packets",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = " • ",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (isArabic) "منع التشتت" else "Jitter Guarded",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = " • ",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (isArabic) "بنج ثابت" else "Constant Low Ping",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (state.isOptimizing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (state.lastOptimizedCommandCount > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E16)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isArabic)
                            "تم تهيئة لـ ${state.lastOptimizedCommandCount} من الأوامر بنجاح ⚡"
                            else "Configured ${state.lastOptimizedCommandCount} total speed commands successfully ⚡",
                        color = CyberGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.lastOptimizedFailedNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (isArabic) "تعذر تطبيق بعض المميزات لأن النظام يحيدها: " else "Unused/System Overridden commands: ") +
                                    state.lastOptimizedFailedNames.joinToString(", "),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // ── SECTION 8: Real-Time Signal Jitter Stability Impact ──
        state.optimizationImpact?.let { impact ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1520)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "📊 تأثير التحسين الفعلي في دقة واستقرار البنج" else "📊 Real-Time Signal Jitter Stability Impact",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF101B2E), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isArabic) "معدل الذبذبة (قبل)" else "Jitter (Before)",
                                color = CyberGrayText,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${impact.jitterBefore} ms",
                                color = CyberAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF101B2E), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isArabic) "معدل الذبذبة (بعد)" else "Jitter (After)",
                                color = CyberGrayText,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${impact.jitterAfter} ms",
                                color = CyberGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222C3A), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "الاستقرار العام وكفاءة الـ Hit-Reg:" else "Overall Jitter Suppression Gain:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "+${impact.improvementPercent}%",
                            color = CyberPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SECTION 9: Action Buttons to Restore Defaults ──
        OutlinedButton(
            onClick = { networkViewModel.restoreNetworkDefaults() },
            border = BorderStroke(1.dp, CyberAccent.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAccent),
            enabled = !state.isOptimizing
        ) {
            Text(
                text = if (isArabic) "إعادة الافتراضي" else "Restore Defaults",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isArabic) "⚠️ عملية التحسين متوافقة 100% مع أنظمة الحماية للعبة ومصممة لمنع حدوث الحظر اللاسلكي." else "⚠️ Optimization protocol matches system safety standards. Safe for wireless and game engine profiles.",
            color = CyberGrayText,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 14.sp
        )
    }
}
