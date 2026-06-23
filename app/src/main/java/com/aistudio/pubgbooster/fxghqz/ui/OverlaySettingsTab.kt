package com.aistudio.pubgbooster.fxghqz.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.pubgbooster.fxghqz.service.OverlayService
import com.aistudio.pubgbooster.fxghqz.ui.theme.*
import com.aistudio.pubgbooster.fxghqz.util.NotificationController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OverlaySettingsContent(isArabic: Boolean) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE) }
    
    // Alert Window Permission check
    var hasAlertPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    // Listener service warning / check
    val isNotificationListenerEnabled = remember {
        val cn = android.content.ComponentName(context, "com.aistudio.pubgbooster.fxghqz.service.GameNotificationListenerService")
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        flat != null && flat.contains(cn.flattenToString())
    }

    var isOverlayRunning by remember { mutableStateOf(false) }

    // Read initial settings states
    var autoShowOverlay by remember { mutableStateOf(prefs.getBoolean("auto_show_overlay", true)) }
    var blockAllNotifications by remember { mutableStateOf(NotificationController.isBlockAllNotifications(context)) }
    var excludeSingleApp by remember { mutableStateOf(NotificationController.isExcludeAppEnabled(context)) }
    var selectedExcludedPkg by remember { mutableStateOf(NotificationController.getExcludedPackage(context) ?: "") }

    var excludeWhatsApp by remember { mutableStateOf(NotificationController.isAppExcluded(context, "com.whatsapp")) }
    var excludeMessenger by remember { mutableStateOf(NotificationController.isAppExcluded(context, "com.facebook.orca")) }
    var excludeTelegram by remember { mutableStateOf(NotificationController.isAppExcluded(context, "org.telegram.messenger")) }
    var excludeFacebook by remember { mutableStateOf(NotificationController.isAppExcluded(context, "com.facebook.katana")) }
    
    // Apps fetching state
    var appsList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var appsLoading by remember { mutableStateOf(false) }
    var appsDropdownExpanded by remember { mutableStateOf(false) }

    // Check overlay service running status
    LaunchedEffect(Unit) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        val running = am?.getRunningServices(Integer.MAX_VALUE)?.any {
            it.service.className == OverlayService::class.java.name
        } ?: false
        isOverlayRunning = running
    }

    // Fetch instaled apps in thread
    LaunchedEffect(excludeSingleApp) {
        if (excludeSingleApp && appsList.isEmpty()) {
            appsLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                    val loaded = resolveInfos.map {
                        it.activityInfo.packageName to it.loadLabel(pm).toString()
                    }.distinctBy { it.first }.sortedBy { it.second }
                    withContext(Dispatchers.Main) {
                        appsList = loaded
                        appsLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        appsLoading = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. System Overlay Overlay Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "🖥️ شاشة الأداء العائمة (HUD)" else "🖥️ Floating Performance HUD",
                    color = CyberPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) 
                        "عرض الفريمات الفعلي (FPS)، الرامات المتاحة والحرارة بشكل مباشر أعلى اللعبة."
                    else 
                        "Displays real-time FPS, RAM updates, and temperature floating nicely over your screen.",
                    color = CyberGrayText,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Permission Warning Button if not granted
                if (!hasAlertPermission) {
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert Permission", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "منح إذن الظهور فوق التطبيقات" else "Grant Draw Over Other Apps Permission",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Immediate toggle to start/stop the HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تشغيل نافذة الأداء الآن" else "Show Floating HUD Now",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isArabic) "تفعيل وإظهار النافذة يدوياً" else "Force-activate floating overlay instantly",
                            color = CyberGrayText,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isOverlayRunning,
                        enabled = hasAlertPermission,
                        onCheckedChange = { checked ->
                            isOverlayRunning = checked
                            val intent = Intent(context, OverlayService::class.java)
                            if (checked) {
                                context.startService(intent)
                            } else {
                                context.stopService(intent)
                            }
                            prefs.edit().putBoolean("overlay_enabled", checked).apply()
                        }
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                // Auto Trigger Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تفعيل تلقائي عند تشغيل PUBG" else "Auto Show on PUBG Launch",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isArabic) "النافذة تظهر تلقائياً مع اللعبة وتختفي عند إغلاقها" else "Automatically shows up or dismisses based on active gameplay state",
                            color = CyberGrayText,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = autoShowOverlay,
                        onCheckedChange = { checked ->
                            autoShowOverlay = checked
                            prefs.edit().putBoolean("auto_show_overlay", checked).apply()
                        }
                    )
                }
            }
        }

        // --- 2. Notification Management Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "🔕 إدارة إشعارات الألعاب" else "🔕 In-Game Notification Management",
                    color = CyberAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) 
                        "حظر الإشعارات المنبثقة والفقاعات للتركيز الكامل باللعب بدون تقطيع بنج أو إزعاج."
                    else 
                        "Block heads-up notifications, and channel system popups so you focus purely on winning.",
                    color = CyberGrayText,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notification Service Grant Warning
                if (blockAllNotifications && !isNotificationListenerEnabled) {
                    Button(
                        onClick = {
                            context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAA00)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Listener Service", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "مطلوب إذن قارئ الإشعارات" else "Required Notification Access",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Toggle 1: Block All Gameplay Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "إيقاف كل الإشعارات أثناء اللعب" else "Disable All Notifications in Game",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isArabic) "حظر كامل للأكواد المنبثقة والفقاعات" else "Prevents banners, bubbles and annoying push notifications",
                            color = CyberGrayText,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = blockAllNotifications,
                        onCheckedChange = { checked ->
                            blockAllNotifications = checked
                            NotificationController.setBlockAllNotifications(context, checked)
                        }
                    )
                }

                if (blockAllNotifications) {
                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = if (isArabic) "💬 تطبيقات ريادية مسموح بمرور إشعاراتها:" else "💬 Whitelisted Communication Apps:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Social bypass apps checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        val socialApps = listOf(
                            Triple("com.whatsapp", if (isArabic) "واتساب (WhatsApp)" else "WhatsApp", excludeWhatsApp),
                            Triple("com.facebook.orca", if (isArabic) "ماسينجر (Messenger)" else "Messenger", excludeMessenger),
                            Triple("org.telegram.messenger", if (isArabic) "تيليجرام (Telegram)" else "Telegram", excludeTelegram),
                            Triple("com.facebook.katana", if (isArabic) "فيسبوك (Facebook)" else "Facebook", excludeFacebook)
                        )

                        socialApps.forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextVal = !app.third
                                        NotificationController.setAppExcluded(context, app.first, nextVal)
                                        when (app.first) {
                                            "com.whatsapp" -> excludeWhatsApp = nextVal
                                            "com.facebook.orca" -> excludeMessenger = nextVal
                                            "org.telegram.messenger" -> excludeTelegram = nextVal
                                            "com.facebook.katana" -> excludeFacebook = nextVal
                                        }
                                        // Auto-turn on whitelisting mode in controller
                                        NotificationController.setExcludeAppEnabled(context, true)
                                        excludeSingleApp = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AppIconImage(
                                        packageName = app.first,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = app.second,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Checkbox(
                                    checked = app.third,
                                    onCheckedChange = { checked ->
                                        NotificationController.setAppExcluded(context, app.first, checked)
                                        when (app.first) {
                                            "com.whatsapp" -> excludeWhatsApp = checked
                                            "com.facebook.orca" -> excludeMessenger = checked
                                            "org.telegram.messenger" -> excludeTelegram = checked
                                            "com.facebook.katana" -> excludeFacebook = checked
                                        }
                                        NotificationController.setExcludeAppEnabled(context, true)
                                        excludeSingleApp = true
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CyberPrimary,
                                        uncheckedColor = Color.White.copy(alpha = 0.2f),
                                        checkmarkColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                    // Toggle 2: Exclude App Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "استثناء تطبيق يدوي مخصص" else "Bypass single custom app",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isArabic) "السماح بالمرور لإشعارات تطبيق تختاره بنفسك من القائمة" else "Bypass notification filter for an app of your selection",
                                color = CyberGrayText,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = excludeSingleApp,
                            onCheckedChange = { checked ->
                                excludeSingleApp = checked
                                NotificationController.setExcludeAppEnabled(context, checked)
                            }
                        )
                    }

                    if (excludeSingleApp) {
                        Spacer(modifier = Modifier.height(10.dp))

                        if (appsLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator(color = CyberPrimary)
                            }
                        } else {
                            // Single App Selection Dropdown UI
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberSurface, RoundedCornerShape(10.dp))
                                    .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { appsDropdownExpanded = !appsDropdownExpanded }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (selectedExcludedPkg.isNotBlank()) {
                                            AppIconImage(
                                                packageName = selectedExcludedPkg,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }
                                        val currentAppName = appsList.firstOrNull { it.first == selectedExcludedPkg }?.second
                                            ?: if (selectedExcludedPkg.isNotBlank()) selectedExcludedPkg else (if (isArabic) "اختر التطبيق المستثنى من الحظر..." else "Select bypass app...")
                                        Text(
                                            text = currentAppName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = if (appsDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = CyberPrimary
                                    )
                                }

                                DropdownMenu(
                                    expanded = appsDropdownExpanded,
                                    onDismissRequest = { appsDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(CyberSurface)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                ) {
                                    appsList.forEach { appItem ->
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                AppIconImage(
                                                    packageName = appItem.first,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                )
                                            },
                                            text = {
                                                Column {
                                                    Text(text = appItem.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text(text = appItem.first, color = CyberGrayText, fontSize = 9.sp)
                                                }
                                            },
                                            onClick = {
                                                selectedExcludedPkg = appItem.first
                                                NotificationController.setExcludedPackage(context, appItem.first)
                                                appsDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppIconImage(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var drawableState by remember(packageName) { mutableStateOf<android.graphics.drawable.Drawable?>(null) }

    LaunchedEffect(packageName) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val icon = context.packageManager.getApplicationIcon(packageName)
                drawableState = icon
            } catch (_: Exception) {
                // Fallback can stay null
            }
        }
    }

    val drawable = drawableState
    if (drawable != null) {
        val bitmap = remember(drawable) {
            try {
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = modifier
            )
        } else {
            Box(
                modifier = modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            )
        }
    } else {
        Box(
            modifier = modifier
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
        )
    }
}
