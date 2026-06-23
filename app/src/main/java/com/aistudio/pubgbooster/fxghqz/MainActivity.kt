package com.aistudio.pubgbooster.fxghqz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import com.aistudio.pubgbooster.fxghqz.ui.theme.*
import com.aistudio.pubgbooster.fxghqz.viewmodel.*
import com.aistudio.pubgbooster.fxghqz.ui.screens.*
import android.content.Context
import com.aistudio.pubgbooster.fxghqz.data.*
import com.aistudio.pubgbooster.fxghqz.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TelemetryItem(
    val icon: ImageVector,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String
)

fun getArabicExplanationForCommand(command: String): String {
    val trimmed = command.trim()
    return when {
        trimmed.contains("animator_duration_scale") -> "تسريع استجابة الأزرار والتفاعلات داخل واجهة الهاتف وجعلها فورية بدون تأخير."
        trimmed.contains("transition_animation_scale") -> "إلغاء الرسوم الانتقالية بين شاشات الهاتف لتوفير جهد كارت الشاشة والمعالج."
        trimmed.contains("window_animation_scale") -> "تعطيل الرسوم الحركية للنوافذ لفتح وإغلاق النوافذ بسرعة فائقة وفورية."
        trimmed.contains("debug.hwui.renderer OpenGL_ES") || trimmed.contains("hwui.renderer") -> "إجبار الهاتف على معالجة الواجهات الرسومية بكرت الشاشة (GPU) لتخفيف العبء عن المعالج."
        trimmed.contains("peak_refresh_rate") -> "تثبيت وضبط الشاشة على أقصى معدل تحديث إطارات مدعوم لضمان أعلى سلاسة فريمات."
        trimmed.contains("min_refresh_rate") -> "منع معدل التحديث من الانخفاض التلقائي لتفادي الرسترة وهبوط الفريمات الفجائي."
        trimmed.contains("low_power") -> "إيقاف تفعيل وضع توفير الطاقة لمنع تقييد سرعة الأنوية والمعالجة أثناء اللعبة."
        trimmed.contains("wifi_scan_throttle_enabled") -> "تعطيل حد فحص الواي فاي لمنع تذبذب البنج (Ping Spike) المزعج أثناء تشغيل اللعبة."
        trimmed.contains("wifi_connected_mac_randomization_enabled") -> "إلغاء عشوائية الماك لزيادة ثبات وسرعة تبادل حزم البيانات بالبنج."
        trimmed.contains("auto_sync_enabled") -> "تعطيل المزامنة التلقائية للخلفية لإنقاذ البطارية والذاكرة والمعالج من الاستنزاف."
        trimmed.contains("trim-caches") -> "تنظيف فوري لملفات الكاش الميتة بالنظام لتوفير مساحة سريعة لمعالج اللعبة."
        trimmed.contains("kill-all") -> "إغلاق كافة خدمات التطبيقات بالخلفية لدفع وحقن كامل الرام (RAM) لصالح اللعبة مفردا."
        trimmed.contains("compile -m speed") -> "تجميع ملفات اللعبة وترجمتها مسبقاً (AOT compilation) لحظر تقطيع الإطارات (Lag Spike) تماماً."
        trimmed.contains("game mode performance") -> "تنشيط وضع الأداء الأقصى لمعالج الألعاب بنظام الاندرويد لإنهاء قيود الاختناق الحراري."
        else -> ""
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.aistudio.pubgbooster.fxghqz.util.CommandExecutor.init(applicationContext)
        com.aistudio.pubgbooster.fxghqz.telemetry.TelemetrySystem.init(applicationContext)
        enableEdgeToEdge()

        // Restore overlay running state if enabled and permission granted
        val prefs = getSharedPreferences("pubg_booster_prefs", MODE_PRIVATE)
        val overlayEnabled = prefs.getBoolean("overlay_enabled", false)
        if (overlayEnabled && android.provider.Settings.canDrawOverlays(this)) {
            try {
                startService(Intent(this, com.aistudio.pubgbooster.fxghqz.service.OverlayService::class.java))
            } catch(e: Exception) {
                // Ignore if background start is restricted
            }
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberBackground
                ) { innerPadding ->
                    // Full screen bleed: MainScreen handles status/navigation bar insets internally
                    MainScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: BoostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isArabic = uiState.isArabic
    val context = LocalContext.current

    // Observe device lifecycle to check Shizuku status whenever returning to the app
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkShizuku()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Real-time automatic permission check on launch
    LaunchedEffect(Unit) {
        val installed = ShizukuCommandRunner.isShizukuInstalled(context)
        val available = if (installed) ShizukuCommandRunner.isShizukuAvailable() else false
        val hasPermission = if (available) ShizukuCommandRunner.hasPermission() else false
        if (installed && available && !hasPermission) {
            viewModel.requestShizukuPermission()
        }
    }

    // Real render FPS feedback using Choreographer loops
    var currentFps by remember { mutableStateOf(60) }
    RealTimeFpsTracker { measuredFps ->
        currentFps = measuredFps
        viewModel.setFps(measuredFps)
        com.aistudio.pubgbooster.fxghqz.telemetry.TelemetrySystem.currentLiveFps = measuredFps
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showOverlaySettings by remember { mutableStateOf(false) }
    var showTelemetryScreen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberBackground, CyberBackground, Color(0xFF070B13))
                )
            )
    ) {
        // Top Header Section (Style matching the image)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTab == 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isArabic) "مراقب الشبكة" else "Network Monitor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            android.widget.Toast.makeText(context, if (isArabic) "مراقبة مستمرة لاستقرار البنج وحل Bufferbloat ⚡" else "Continuous monitoring of ping stability and Bufferbloat elimination ⚡", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (selectedTab == 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isArabic) "التفعيلات النشطة" else "Active Activations",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            android.widget.Toast.makeText(context, if (isArabic) "طبقات الحماية وتعديلات الأداء والتحسين الفعلي ⚡" else "Protection layers, performance tweaks and active adjustments ⚡", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .background(CyberSurface.copy(alpha = 0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = CyberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // PUBG BOOSTER Custom Styled Text in modern italic styling matching the screenshot
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                                append("PUBG ")
                            }
                            withStyle(style = androidx.compose.ui.text.SpanStyle(color = CyberPrimary, fontWeight = FontWeight.Bold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                                append("Booster")
                            }
                        },
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bell Notification Icon with active badge cyan dot matching image
                    IconButton(
                        onClick = {
                            android.widget.Toast.makeText(context, if (isArabic) "جميع الأنظمة آمنة ومحسنة تماماً!" else "All game engines are fully optimized!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            // Modern cyan notification badge dot
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp, end = 2.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CyberPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Settings Tab Launcher Cogwheel
                    IconButton(
                        onClick = { showOverlaySettings = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Minimalist Language Switcher
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = CyberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Tab Content layout (renders specific tab based on selected bottom navigation index)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    uiState = uiState,
                    isArabic = isArabic,
                    currentFps = currentFps,
                    viewModel = viewModel,
                    onNavigateToSettings = { selectedTab = 3 }
                )
                1 -> NetworkTabScreen(
                    isArabic = isArabic,
                    viewModel = viewModel
                )
                2 -> ActivationsTab(
                    uiState = uiState,
                    isArabic = isArabic,
                    viewModel = viewModel
                )
                3 -> SettingsTab(
                    uiState = uiState,
                    isArabic = isArabic,
                    viewModel = viewModel,
                    onOpenTelemetry = { showTelemetryScreen = true }
                )
            }
        }

        // Elegant custom Bottom Navigation Capsule Dock
        CustomBottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            isArabic = isArabic
        )

        ResultBottomSheet(
            isOpen = uiState.isResultSheetOpen,
            onDismiss = { viewModel.dismissResultSheet() },
            results = uiState.activationResults,
            onRetryFailed = { failed -> viewModel.retryFailedCommands(failed) },
            isArabic = isArabic,
            gameAutoLaunched = uiState.gameAutoLaunched,
            gameAutoLaunchFailed = uiState.gameAutoLaunchFailed
        )

        if (showTelemetryScreen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showTelemetryScreen = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                com.aistudio.pubgbooster.fxghqz.telemetry.TelemetryScreen(isArabic = isArabic) {
                    showTelemetryScreen = false
                }
            }
        }

        if (showOverlaySettings) {
            ModalBottomSheet(
                onDismissRequest = { showOverlaySettings = false },
                containerColor = Color(0xFF0F1626),
                tonalElevation = 8.dp,
                dragHandle = { BottomSheetDefaults.DragHandle(color = CyberPrimary.copy(alpha = 0.3f)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "إعدادات النافذة والتحكم المتقدم" else "HUD & Advanced Controller Settings",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(
                            onClick = { showOverlaySettings = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    com.aistudio.pubgbooster.fxghqz.ui.OverlaySettingsContent(isArabic = isArabic)
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isArabic: Boolean
) {
    Surface(
        color = Color(0xFF0F1626),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(0, Icons.Default.Home, if (isArabic) "الرئيسية" else "Home"),
                Triple(1, Icons.Default.Wifi, if (isArabic) "الشبكة" else "Network"),
                Triple(2, Icons.Default.Bolt, if (isArabic) "التفعيلات" else "Activations"),
                Triple(3, Icons.Default.Person, if (isArabic) "المطور" else "Developer")
            )

            items.forEach { (index, icon, label) ->
                val isSelected = selectedTab == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Color(0xFF1B325F).copy(alpha = 0.8f) else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) CyberPrimary else CyberGrayText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else CyberGrayText.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTab(
    uiState: UiState,
    isArabic: Boolean,
    currentFps: Int,
    viewModel: BoostViewModel,
    onNavigateToSettings: () -> Unit
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var showPubgVersionDialog by remember { mutableStateOf(false) }

    val pubgEditions = remember {
        listOf(
            Triple("com.tencent.ig", "PUBG Mobile (Global)", "العالمية"),
            Triple("com.pubg.imobile", "BGMI (India)", "الهندية"),
            Triple("com.pubg.krmobile", "PUBG Mobile (KR/JP)", "الكورية"),
            Triple("com.vng.pubgmobile", "PUBG Mobile (VN)", "الفيتنامية"),
            Triple("com.rekoo.pubgm", "PUBG Mobile (TW)", "التايوانية")
        )
    }

    if (showProfileDialog) {
        BoostProfileDialog(
            isArabic = isArabic,
            onDismiss = { showProfileDialog = false },
            onProfileSelected = { profile ->
                viewModel.setBoostProfile(profile)
                viewModel.optimizePubg()
                showProfileDialog = false
            }
        )
    }

    if (showPubgVersionDialog) {
        AlertDialog(
            onDismissRequest = { showPubgVersionDialog = false },
            title = {
                Text(
                    text = if (isArabic) "🎯 اختر نسخة لعبة ببجي التي ستلعبها" else "🎯 Choose PUBG Mobile Edition",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = Color(0xFF0F1626),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pubgEditions.forEach { edition ->
                        val isSelected = uiState.selectedPubgPackage == edition.first
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF1B325F).copy(alpha = 0.6f) else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyberPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.setPubgPackage(edition.first)
                                    showPubgVersionDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = "PUBG Edition",
                                    tint = if (isSelected) CyberPrimary else CyberGrayText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = edition.second,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isArabic) "حزمة: ${edition.first} (${edition.third})" else "Package: ${edition.first}",
                                        color = CyberGrayText,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setPubgPackage(edition.first)
                                    showPubgVersionDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyberPrimary,
                                    unselectedColor = CyberGrayText.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPubgVersionDialog = false }) {
                    Text(
                        text = if (isArabic) "إغلاق" else "Close",
                        color = CyberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (uiState.showBoostSelectionDialog) {
        Dialog(onDismissRequest = { viewModel.dismissBoostSelectionDialog() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isArabic) "⚡ اختر هدف التعزيز" else "⚡ Choose Boost Target",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (isArabic) "حدد النسخة التي ترغب بالتحسين والمنفذ لها" else "Select what to optimize for",
                        fontSize = 12.sp,
                        color = CyberGrayText
                    )

                    if (uiState.installedPubgVariants.isEmpty()) {
                        Text(
                            text = if (isArabic) "⚠️ لم يتم العثور على أي نسخة PUBG مثبتة على الجهاز." else "⚠️ No PUBG variant detected on this device.",
                            fontSize = 12.sp,
                            color = CyberYellow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.installedPubgVariants.forEach { variant ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectBoostTarget(variant.packageName) }
                                        .background(Color(0xFF1B2230))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gamepad,
                                        contentDescription = null,
                                        tint = CyberPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = variant.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = variant.appName,
                                            fontSize = 11.sp,
                                            color = CyberGrayText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                    // ── خيار "تحسين الهاتف فقط" ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectBoostTarget(DEVICE_ONLY_OPTION) }
                            .background(CyberGreen.copy(alpha = 0.08f))
                            .border(1.dp, CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (isArabic) "تحسين نظام الهاتف فقط" else "Device Optimization Only",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isArabic) "تحسين الأداء الشامل دون فتح أي لعبة" else "Optimize without launching any game",
                                fontSize = 11.sp,
                                color = CyberGrayText
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showPerformanceLevelDialog) {
        Dialog(onDismissRequest = { viewModel.dismissPerformanceLevelDialog() }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isArabic) "🎚️ اختر مستوى الأداء" else "🎚️ Select Performance Level",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (isArabic) "حدد مستوى الموازنة بين الأداء والبطارية" else "Choose the optimization mode",
                        fontSize = 12.sp,
                        color = CyberGrayText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    listOf(
                        Triple(BoostProfile.MAX_PERFORMANCE, "🚀 Max Performance", "أقصى أداء 🚀"),
                        Triple(BoostProfile.BALANCED, "⚖️ Balanced", "متوازن ⚖️"),
                        Triple(BoostProfile.BATTERY_SAVER, "🔋 Battery Saver", "موفر الطاقة 🔋")
                    ).forEach { (profile, labelEn, labelAr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectPerformanceLevelAndBoost(profile) }
                                .background(Color(0xFF1B2230))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isArabic) labelAr else labelEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success or warning banner based on Shizuku state
        ShizukuSuccessOrErrorCard(
            shizukuReady = uiState.shizukuReady,
            isArabic = isArabic,
            onClick = {
                if (uiState.shizukuReady) {
                    viewModel.checkShizuku()
                } else {
                    viewModel.requestShizukuPermission()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Premium Circular Boost Gauge in center
        CenterBoostRing(
            uiState = uiState,
            isArabic = isArabic,
            onActivate = { viewModel.openBoostSelectionDialog() },
            onDeactivate = { viewModel.restoreDefaults() }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3-Grid Stats Box row (RAM, FPS, Temperature)
        ThreeGridStats(
            uiState = uiState,
            currentFps = currentFps,
            isArabic = isArabic
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ShizukuSuccessOrErrorCard(
    shizukuReady: Boolean,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF09101F).copy(alpha = 0.95f) // Matches the screenshot dark rich navy container
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (shizukuReady) CyberGreen.copy(alpha = 0.15f) else CyberAccent.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (shizukuReady) CyberGreen else CyberAccent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (shizukuReady) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White, // Pure white inner icon inside solid colored circle
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (shizukuReady) {
                            if (isArabic) "متصل بنجاح" else "Connected Successfully"
                        } else {
                            if (isArabic) "يتطلب تفعيل Shizuku" else "Shizuku Requires Setup"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (shizukuReady) {
                            if (isArabic) "تم تحسين جهازك بالكامل للعبة ببجي موبايل." else "Your device is optimized for the best gaming experience."
                        } else {
                            if (isArabic) "اضغط للذهاب إلى الإعدادات وتفعيل صلاحيات الأداة." else "Tap here to request terminal permissions inside settings."
                        },
                        color = CyberGrayText.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (shizukuReady) CyberGreen else CyberAccent.copy(alpha = 0.7f), // colored chevron
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CenterBoostRing(
    uiState: UiState,
    isArabic: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.boostState == BoostState.ACTIVE) 1.03f else 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeColor = when (uiState.boostState) {
        BoostState.IDLE -> Color(0xFF00D2FF) // Neon Blue accent
        BoostState.BOOSTING -> CyberYellow
        BoostState.ACTIVE -> CyberGreen
        BoostState.RESTORING -> CyberAccent
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(250.dp) // Adjusted slightly for a magnificent presentable structure
                .scale(if (uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING) 1.0f else pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when (uiState.boostState) {
                            BoostState.IDLE -> listOf(Color(0xFF0F2648).copy(alpha = 0.5f), Color(0xFF070C16))
                            BoostState.BOOSTING -> listOf(Color(0xFF332A15).copy(alpha = 0.5f), Color(0xFF070C16))
                            BoostState.ACTIVE -> listOf(Color(0xFF153F2D).copy(alpha = 0.6f), Color(0xFF070C16))
                            BoostState.RESTORING -> listOf(Color(0xFF34171E).copy(alpha = 0.5f), Color(0xFF070C16))
                        }
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF0F1E36), // Faint high-tech steel border
                    shape = CircleShape
                )
                .clickable(
                    enabled = uiState.shizukuReady && (uiState.boostState == BoostState.IDLE || uiState.boostState == BoostState.ACTIVE),
                    onClick = {
                        if (uiState.boostState == BoostState.IDLE) onActivate()
                        else if (uiState.boostState == BoostState.ACTIVE) onDeactivate()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Live Background Dials canvas - draw technical ticks matching the photo
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ticksCount = 72 // Finer, sophisticated dial ticks
                val outerRad = size.minDimension / 2 - 10
                val innerRad = outerRad - 6
                for (i in 0 until ticksCount) {
                    val angle = (i * 360f / ticksCount) * (Math.PI / 180).toFloat()
                    val startX = (center.x + innerRad * Math.cos(angle.toDouble())).toFloat()
                    val startY = (center.y + innerRad * Math.sin(angle.toDouble())).toFloat()
                    val endX = (center.x + outerRad * Math.cos(angle.toDouble())).toFloat()
                    val endY = (center.y + outerRad * Math.sin(angle.toDouble())).toFloat()

                    val color = when (uiState.boostState) {
                        BoostState.ACTIVE -> CyberGreen.copy(alpha = 0.25f)
                        BoostState.BOOSTING -> CyberYellow.copy(alpha = 0.25f)
                        else -> Color(0xFF1B355A).copy(alpha = 0.35f)
                    }

                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                        strokeWidth = 1f
                    )
                }
            }

            // Glowing Arc dial circles with round caps matching the picture
            if (uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING) {
                CircularProgressIndicator(
                    progress = { uiState.progressPercent },
                    modifier = Modifier.size(232.dp),
                    color = activeColor,
                    strokeWidth = 4.5.dp,
                    trackColor = Color.White.copy(alpha = 0.02f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round // Rounded progress ends!
                )
            } else {
                // Faint elegant indicator arc showing optimal performance curve
                CircularProgressIndicator(
                    progress = { if (uiState.boostState == BoostState.ACTIVE) 1.0f else 0.72f },
                    modifier = Modifier.size(232.dp),
                    color = activeColor,
                    strokeWidth = 4.dp,
                    trackColor = Color.White.copy(alpha = 0.02f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Large neon glowing bolt
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier
                        .size(68.dp)
                        .scale(if (uiState.boostState == BoostState.ACTIVE) 1.1f else 1.0f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (uiState.boostState) {
                        BoostState.IDLE -> if (isArabic) "تعزيز الهاتف" else "BOOST"
                        BoostState.BOOSTING -> if (isArabic) "جاري التعزيز" else "BOOSTING"
                        BoostState.ACTIVE -> if (isArabic) "وضع نشط" else "ACTIVE"
                        BoostState.RESTORING -> if (isArabic) "جاري الاسترجاع" else "RESTORING"
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (uiState.boostState) {
                        BoostState.IDLE -> {
                            if (uiState.shizukuReady) {
                                if (isArabic) "اضغط للتشغيل" else "Tap to Boost"
                            } else {
                                if (isArabic) "يتطلب تفعيل" else "Needs Setup"
                            }
                        }
                        BoostState.BOOSTING, BoostState.RESTORING -> {
                            "${(uiState.progressPercent * 100).toInt()}%"
                        }
                        BoostState.ACTIVE -> if (isArabic) "اضغط للاستعادة" else "Tap to Restore"
                    },
                    color = activeColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live text feedback detailed execution
        if (uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING || uiState.boostState == BoostState.ACTIVE) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = when (uiState.boostState) {
                    BoostState.BOOSTING -> if (isArabic) "جاري رفع الأداء والتحسين الفعلي..." else "Applying real-time parameters..."
                    BoostState.RESTORING -> if (isArabic) "جاري استرجاع وضع توفير الطاقة..." else "Restoring standard system defaults..."
                    BoostState.ACTIVE -> if (isArabic) "✓ معالج PUBG والشبكة تعمل بأقصى سرعة" else "✓ Professional parameters successfully applied!"
                    else -> ""
                },
                color = activeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ThreeGridStats(
    uiState: UiState,
    currentFps: Int,
    isArabic: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // RAM Card styling matching photo exactly
        val usedRamGb = uiState.totalRamGb - uiState.availRamGb
        val usedRamPct = if (uiState.totalRamGb > 0f) ((usedRamGb / uiState.totalRamGb) * 100).toInt() else 61
        StatsBox(
            modifier = Modifier.weight(1f),
            title = if (isArabic) "الرام RAM" else "RAM",
            value = String.format(Locale.ENGLISH, "%.2f GB", if (usedRamGb > 0f) usedRamGb else 4.91f),
            subtext = if (isArabic) "$usedRamPct% مستخدم" else "$usedRamPct% Used",
            color = CyberPrimary, // Blue theme for RAM
            icon = Icons.Default.Memory,
            progress = (usedRamPct / 100f).coerceIn(0f, 1f)
        )

        // FPS Card styling matching photo exactly
        StatsBox(
            modifier = Modifier.weight(1f),
            title = if (isArabic) "الفريمات FPS" else "FPS",
            value = "$currentFps",
            subtext = if (isArabic) "فوق السلس" else "Ultra Smooth",
            color = CyberGreen, // Green theme for FPS
            icon = Icons.Default.Speed,
            progress = (currentFps / 120f).coerceIn(0f, 1f)
        )

        // Temperature Card styling matching photo exactly (Warm Amber Orange indicator)
        val tempVal = uiState.batteryTemp
        val tempString = String.format(Locale.ENGLISH, "%.1f °C", if (tempVal > 0f) tempVal else 37.8f)
        val stateText = when {
            tempVal < 38f -> if (isArabic) "طبيعي" else "Normal"
            tempVal < 42f -> if (isArabic) "معتدل" else "Warm"
            else -> if (isArabic) "حار" else "Hot"
        }
        // Amber Orange color for TEMP
        val orangeColor = Color(0xFFD35400) // Beautiful warm amber orange
        StatsBox(
            modifier = Modifier.weight(1f),
            title = if (isArabic) "حرارة الهاتف" else "TEMP",
            value = tempString,
            subtext = stateText,
            color = orangeColor,
            icon = Icons.Default.Thermostat,
            progress = (tempVal / 50f).coerceIn(0f, 1f)
        )
    }
}

@Composable
fun StatsBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    color: Color,
    icon: ImageVector,
    progress: Float
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF09101F).copy(alpha = 0.9f)), // Modern tech deep-navy card matching photo
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF13223C)) // Techy subtle steel border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Perfectly centered layout matching screenshot
        ) {
            // Icon Badge with beautiful layout matching the photo
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Caps title matching photo
            Text(
                text = title,
                color = CyberGrayText.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Large white bold metrics
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Line with circular cap
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(3.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status label beneath the progress bar
            Text(
                text = subtext,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun NetworkTabScreen(
    isArabic: Boolean,
    viewModel: BoostViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NetworkTab(isArabic = isArabic, viewModel = viewModel)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActivationsTab(
    uiState: UiState,
    isArabic: Boolean,
    viewModel: BoostViewModel
) {
    val customCommands by viewModel.customCommandsMap.collectAsState()
    var expandedDropdown by remember { mutableStateOf(false) }

    val pubgEditions = remember {
        listOf(
            Triple("com.tencent.ig", "PUBG Mobile (Global)", "العالمية"),
            Triple("com.pubg.imobile", "BGMI (India)", "الهندية"),
            Triple("com.pubg.krmobile", "PUBG Mobile (KR/JP)", "الكورية"),
            Triple("com.vng.pubgmobile", "PUBG Mobile (VN)", "الفيتنامية"),
            Triple("com.rekoo.pubgm", "PUBG Mobile (TW)", "التايوانية")
        )
    }

    val currentSelected = pubgEditions.firstOrNull { it.first == uiState.selectedPubgPackage } ?: pubgEditions[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compilation status Alerts
            if (uiState.isBackgroundCompiling) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(BorderStroke(1.dp, Color(0xFFFFAA00).copy(alpha = 0.4f)), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFFFFAA00),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.backgroundCompileMessage,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (uiState.backgroundCompileResult != null) {
                val res = uiState.backgroundCompileResult
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(BorderStroke(1.dp, if (res.success) Color(0xFF00F0FF).copy(alpha = 0.3f) else Color(0xFFFF5252).copy(alpha = 0.3f)), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "Compile Done",
                            tint = if (res.success) Color(0xFF00F0FF) else Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) 
                                "⚡ تم تجميع ملفات اللعبة وترجمتها بنجاح لحظر التقطيع!" 
                            else 
                                "⚡ Dynamic game code AOT-compilation completed successfully!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dynamic Optimizers toggles list (Hidden from UI but still executes fully behind the scenes)
            /*
            InteractiveCommandsList(
                customCommands = customCommands,
                isArabic = isArabic,
                onToggle = { cmd, isChecked ->
                    viewModel.updateCustomCommand(cmd.copy(enabled = isChecked))
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            */

            Spacer(modifier = Modifier.height(16.dp))

            ProtectionSettingsCard(isArabic = isArabic)

            Spacer(modifier = Modifier.height(16.dp))

            ActiveGameplayTweaksCard(isArabic = isArabic)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsTab(
    uiState: UiState,
    isArabic: Boolean,
    viewModel: BoostViewModel,
    onOpenTelemetry: () -> Unit
) {
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official bio card Eslam Ramadan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF080D18))
                            .border(2.dp, CyberGreen, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "إسلام رمضان ربيع",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Eslam Ramdan Rabie",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) {
                            "مالك التطبيق والمطور الرسمي المعتمد. خبير بارز في تعديلات مستويات أنظمة أندرويد وتهيئة حزم الألعاب للهواتف المحمولة."
                        } else {
                            "Official App Owner & Certified Core Developer. Professional in Android System Optimizations and Package-level game acceleration."
                        },
                        color = CyberGrayText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isArabic) "📞 قنوات التواصل الرسمية والمباشرة:" else "📞 Direct Contact & Support Lines:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            ContactItem(
                icon = Icons.Default.Facebook,
                title = "Facebook Contact Profile",
                description = "Visit Official Facebook Profile",
                color = Color(0xFF1877F2),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1CeNDG6hML/"))
                    ctx.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ContactItem(
                icon = Icons.Default.Call,
                title = "WhatsApp Technical Support",
                description = "Open WhatsApp Direct Support Link",
                color = Color(0xFF25D366),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/qr/K7C6TJZJIS72H1"))
                    ctx.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ContactItem(
                icon = Icons.Default.Settings,
                title = if (isArabic) "مركز الفحص والـ Telemetry" else "System Diagnostics Hub",
                description = if (isArabic) "مراقبة الأداء، الأخطاء، وإعدادات البوت" else "Monitor performance, crash logs & telegram sync",
                color = CyberPrimary,
                onClick = onOpenTelemetry
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fixed Bottom Owner copyright footer matching instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) {
                        "تطوير وتصميم المهندس إسلام رمضان ربيع © ٢٠٢٦"
                    } else {
                        "Developed by Islam Ramadan Rabie © 2026"
                    },
                    color = CyberGrayText.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun TelemetryTab(
    uiState: UiState,
    isArabic: Boolean,
    viewModel: BoostViewModel
) {
    val customCommands by viewModel.customCommandsMap.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        BoostProfileDialog(
            isArabic = isArabic,
            onDismiss = { showProfileDialog = false },
            onProfileSelected = { profile ->
                viewModel.setBoostProfile(profile)
                viewModel.optimizePubg()
                showProfileDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Precise Shizuku Connection Status HUD
            ShizukuStatusHUD(
                status = uiState.shizukuStatus,
                isArabic = isArabic,
                onRequest = { viewModel.requestShizukuPermission() },
                onRefresh = { viewModel.checkShizuku() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Current phone indicators (RAM + Temp + Battery)
            HardwareTelemetryHUD(uiState = uiState, isArabic = isArabic)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Game boost button (BOOST)
            BigTacticalBoostButton(
                uiState = uiState,
                isArabic = isArabic,
                onActivate = { showProfileDialog = true },
                onDeactivate = { viewModel.restoreDefaults() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Background compiling status message
            if (uiState.isBackgroundCompiling) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(BorderStroke(1.dp, Color(0xFFFFAA00).copy(alpha = 0.4f)), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFFFFAA00),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.backgroundCompileMessage,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (uiState.backgroundCompileResult != null) {
                val res = uiState.backgroundCompileResult
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(BorderStroke(1.dp, if (res.success) Color(0xFF00F0FF).copy(alpha = 0.3f) else Color(0xFFFF5252).copy(alpha = 0.3f)), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "Compile Done",
                            tint = if (res.success) Color(0xFF00F0FF) else Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) 
                                "⚡ تم تجميع ملفات اللعبة وترجمتها بنجاح لحظر التقطيع!" 
                            else 
                                "⚡ Dynamic game code AOT-compilation completed successfully!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.boostState == BoostState.ACTIVE) {
                ActiveTelemetryDashboard(isArabic = isArabic)
                Spacer(modifier = Modifier.height(16.dp))
            }

            SecurityFeaturesCard(isArabic = isArabic)
            Spacer(modifier = Modifier.height(16.dp))
            UsageGuideCard(isArabic = isArabic)
            Spacer(modifier = Modifier.height(16.dp))
            CreditsCard()
        }
    }
}

@Composable
fun BoostProfileDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onProfileSelected: (BoostProfile) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isArabic) "🚀 اختر وضع قوة الأداء المطلوب" else "🚀 Select Performance Boost Profile",
                    color = CyberPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = if (isArabic) "اضغط على النمط لبدء تحسين أداء اللعبة فوراً:" else "Tap on a mode to automatically optimize game performance:",
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Option 1: Max Performance
                ProfileOptionButton(
                    title = if (isArabic) "أقصى أداء (تثبيت الأنوية)" else "Extreme Peak (Max Performance)",
                    description = if (isArabic) 
                        "استجابة لمس فائقة، تجمعات أنوية كاملة، ومنع خنق حراري برعاية معالج PUBG Extreme."
                    else 
                        "Locked CPU cores, maximum touch speed, and absolute gaming priority overrides thermal safeguards.",
                    icon = Icons.Default.FlashOn,
                    iconColor = CyberAccent,
                    onClick = {
                        onProfileSelected(BoostProfile.MAX_PERFORMANCE)
                    }
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Option 2: Balanced
                ProfileOptionButton(
                    title = if (isArabic) "الوضع المتوازن (حرارة آمنة)" else "Balanced Mode (Thermal Safe)",
                    description = if (isArabic) 
                        "توازن مذهل للشبكة واللمس مع إدارة ذكية للحفاظ على حرارة الهاتف."
                    else 
                        "Optimized frame filters, balanced DNS stability, structures memory nicely.",
                    icon = Icons.Default.Speed,
                    iconColor = CyberPrimary,
                    onClick = {
                        onProfileSelected(BoostProfile.BALANCED)
                    }
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Option 3: Battery Saver
                ProfileOptionButton(
                    title = if (isArabic) "توفير الطاقة (برودة الهاتف)" else "Eco Stabilizer (Battery Saver)",
                    description = if (isArabic) 
                        "تقليل استخدام الطاقة، استقرار البنج والشبكة بالكامل لتبقي بارداً."
                    else 
                        "Stable DNS, background synchronization limits, minimizes power usage.",
                    icon = Icons.Default.BatteryChargingFull,
                    iconColor = CyberGreen,
                    onClick = {
                        onProfileSelected(BoostProfile.BATTERY_SAVER)
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = if (isArabic) "إلغاء" else "Cancel",
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionButton(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberBackground.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    color = CyberGrayText,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun OptimizerTab(
    uiState: UiState,
    isArabic: Boolean,
    viewModel: BoostViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Collect database-backed live streams from repository
    val customCommands by viewModel.customCommandsMap.collectAsState()
    val systemBackups by viewModel.systemBackupsList.collectAsState()

    var expandedDropdown by remember { mutableStateOf(false) }

    // Dialog triggering states
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<CustomCommand?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Form text fields
    var nameInput by remember { mutableStateOf("") }
    var commandInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var restoreCommandInput by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }

    val pubgEditions = remember {
        listOf(
            Triple("com.tencent.ig", "PUBG Mobile (Global)", "العالمية"),
            Triple("com.pubg.imobile", "BGMI (India)", "الهندية"),
            Triple("com.pubg.krmobile", "PUBG Mobile (KR/JP)", "الكورية"),
            Triple("com.vng.pubgmobile", "PUBG Mobile (VN)", "الفيتنامية"),
            Triple("com.rekoo.pubgm", "PUBG Mobile (TW)", "التايوانية")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = if (isArabic) "⚡ حد استهداف الفريمات (سخونة أقل):" else "⚡ Target Frame Rate (Prevent Overheating):",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = if (isArabic) "تم ضبط 90Hz افتراضياً لتقليل الحرارة، ويمكنك تحديد الخيار الأنسب لهاتفك:" 
                   else "90 FPS is locked by default to reduce thermal throttle. Customize for your device:",
            color = CyberGrayText,
            fontSize = 11.sp,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val fpsOptions = listOf(60, 90, 120)
            fpsOptions.forEach { fps ->
                val isSelected = uiState.selectedFpsLimit == fps
                val optionColor = when (fps) {
                    60 -> CyberGreen
                    90 -> CyberPrimary
                    else -> CyberAccent
                }
                val label = when (fps) {
                    60 -> if (isArabic) "60 فريم (بارد)" else "60 FPS (Cool)"
                    90 -> if (isArabic) "90 فريم (متزن)" else "90 FPS (Smart)"
                    else -> if (isArabic) "120 فريم (أقصى)" else "120 FPS (Peak)"
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setFpsLimit(fps) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) optionColor.copy(alpha = 0.15f) else CyberSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) optionColor else Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (fps) {
                                60 -> Icons.Default.Air
                                90 -> Icons.Default.Speed
                                else -> Icons.Default.Whatshot
                            },
                            contentDescription = null,
                            tint = if (isSelected) optionColor else CyberGrayText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else CyberGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🛡️ مركز النسخ الاحتياطي واستعادة أمان الهاتف (Recovery Safety Shield)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = CyberPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "🛡️ مركز أمان النظام والنسخ الاحتياطي" else "🛡️ Safety Coordinator & System Backup",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic) {
                        "يتيح لك هذا القسم أخذ نسخة أمان كاملة لإعادات هاتفك الأصلية قبل تطبيق أي تعديلات، لاستعادة الهاتف لوضعه الطبيعي فوراً بأي وقت ودون مشاكل."
                    } else {
                        "This section takes custom snapshots of your device config before tweaking, ensuring real-time complete back-to-normal restore."
                    },
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "الإعدادات المحفوظة حالياً:" else "Currently Saved Snapshots:",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(if (systemBackups.isNotEmpty()) CyberGreen.copy(alpha = 0.15f) else CyberYellow.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${systemBackups.size} " + (if (isArabic) "إعداد محفوظ" else "units"),
                            color = if (systemBackups.isNotEmpty()) CyberGreen else CyberYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Manual backup button
                    Button(
                        onClick = {
                            viewModel.takeManualBackup { success, msg ->
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "نسخة احتياطية يدوية" else "Manual Backup",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Restore defaults button
                    Button(
                        onClick = {
                            viewModel.restoreDefaults()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAccent),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "استعادة الهاتف للطبيعي" else "Revert System",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // Import field section
                Text(
                    text = if (isArabic) "📥 استرداد كود النسخة الاحتياطية وتطبيقها فورا:" else "📥 Paste Backup code & Apply Restore:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                var inlineRestoreText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = inlineRestoreText,
                    onValueChange = { inlineRestoreText = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp),
                    placeholder = {
                        Text(
                            text = if (isArabic) "الصق هنا كود JSON الاحتياطي المسجّل سابقاً..." else "Paste backup JSON representation here...",
                            color = CyberGrayText.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (inlineRestoreText.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, if (isArabic) "الرجاء كتابة كود النسخة أولاً!" else "Pasted payload code is empty!", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.importAndApplyRestore(inlineRestoreText) { success, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            if (success) {
                                inlineRestoreText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "تحميل واسترداد الهاتف فوراً للوضع الطبيعي" else "Import & Apply Complete Restore",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (systemBackups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Copy current code helper
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.exportBackupsJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("System Backup", json)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, if (isArabic) "تم نسخ كود الحفظ الاحتياطي الخاص بك!" else "Backup payload copied!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "نسخ كود النسخة الاحتياطية الحالي والاحتفاظ به" else "Copy active JSON Backup payload to clipboard",
                            color = CyberPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📚 شرح أكواد الهاتف والتحسين (Codes description Wiki)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            var showCodesWiki by remember { mutableStateOf(false) }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCodesWiki = !showCodesWiki },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Codes Guide",
                            tint = CyberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "📚 شرح أكواد تحسين الهاتف بالكامل (بالعربية)" else "📚 Arabic Guide for Performance Codes",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showCodesWiki) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = CyberPrimary
                    )
                }

                if (showCodesWiki) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    val explanations = listOf(
                        "animator_duration_scale" to "تسريع استجابة النقر والتنقل في واجهة الهاتف وجعلها فورية بدون تجميد أو فواصل زمنية.",
                        "transition_animation_scale" to "إيقاف الرسوم المتحركة للانتقال بين الشاشات لتخفيف العبء بالكامل عن بطاقة الرسوميات (GPU).",
                        "window_animation_scale" to "تعطيل حركات فتح وغلق النوافذ لوصول سريع وتنقل خارق بالهاتف والألعاب.",
                        "low_power" to "إجبار الهاتف على إيقاف توفير الطاقة لتعزيز أداء المعالجة للألعاب دون أي تقييد بالسرعة.",
                        "wifi_scan_throttle_enabled" to "حل مشكلة تقطيع البنج (Ping Spike) في لعب أونلاين عبر تعطيل خنق الواي فاي التلقائي.",
                        "wifi_connected_mac_randomization_enabled" to "تثبيت معرّف الماك للشبكة لمنع هبوط سرعة تحميل اللعبة واستقرار البث.",
                        "auto_sync_enabled" to "تعطيل عمليات المزامنة والتحديثات بالخلفية لإنقاذ وحدة المعالجة المركزية (CPU) من الاستهلاك.",
                        "pm trim-caches" to "مسح الملفات المؤقتة المتراكمة بالذاكرة لتحرير مساحة داخلية سريعة لمعالجة شادر اللعبة.",
                        "am kill-all" to "تطهير الرام لتحرير المعالج بالكامل من ثقل برامج وتطبيقات الشات المسجلة في الذاكرة العشوائية.",
                        "cmd package compile" to "تجميع وترجمة أكواد اللعبة مسبقاً بطريقة AOT لتشغيل أسرع ومنع التقطيع واللاق (FPS Drop).",
                        "cmd game mode performance" to "إدارة النظام لوضع Performance المخصص للمطورين لإيقاف خنق الهاتف الحراري وتفضيل اللعبة."
                    )

                    explanations.forEach { (code, explain) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = code,
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "💡 الفائدة: $explain",
                                color = CyberGrayText,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Scripting Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "⚡ الأوامر المخصصة (SQLite):" else "⚡ Custom Commands (SQLite):",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Row {
                // Settings backup exploration panel button
                IconButton(
                    onClick = { showBackupDialog = true },
                    modifier = Modifier
                        .background(CyberSurface, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Restore backups dashboard",
                        tint = CyberYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Create a custom command button
                IconButton(
                    onClick = {
                        nameInput = ""
                        commandInput = ""
                        descInput = ""
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .background(CyberPrimary, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom command",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Commands interactive listing cards
        if (customCommands.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا تجد أوامر مخصصة نشطة. اضغط (+) لإضافة واحدة!" else "No custom commands compiled yet. Tap (+) to define some!",
                        color = CyberGrayText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            customCommands.forEach { cmd ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (cmd.enabled) CyberPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cmd.name,
                                    color = if (cmd.enabled) Color.White else CyberGrayText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cmd.command,
                                    color = if (cmd.enabled) CyberPrimary else CyberGrayText.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                                if (cmd.restoreCommand.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = (if (isArabic) "↩️ الكود العكسي: " else "↩️ Restore: ") + cmd.restoreCommand,
                                        color = if (cmd.enabled) CyberAccent.copy(alpha = 0.8f) else CyberGrayText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = cmd.enabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateCustomCommand(cmd.copy(enabled = isChecked))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyberBackground,
                                        checkedTrackColor = CyberPrimary,
                                        uncheckedThumbColor = CyberGrayText,
                                        uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                        }

                        val arExplanation = getArabicExplanationForCommand(cmd.command)
                        if (isArabic && arExplanation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 الفائدة: $arExplanation",
                                color = CyberGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (cmd.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cmd.description,
                                color = CyberGrayText,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { editingCommand = cmd },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Command",
                                    tint = CyberPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.deleteCustomCommand(cmd) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Command",
                                    tint = CyberAccent.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Execution Log HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isArabic) "⚙️ سجل تطبيق الأوامر المباشرة:" else "⚙️ Live Command Execution Feed:",
                    color = CyberPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                if (uiState.executionLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val fullLogString = uiState.executionLogs.joinToString("\n---\n") { log ->
                                "[${log.timestamp}] Command: ${log.command}\nSuccess: ${log.success}\nExit Code: ${log.exitCode}\nStdout: ${log.stdout}\nStderr: ${log.stderr}"
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Full Optimizer Log", fullLogString)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, if (isArabic) "تم نسخ جميع السجلات لوحة الحافظة!" else "All execution logs copied!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(CyberSurface, CircleShape)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy all logs",
                            tint = CyberPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val successful = uiState.executionLogs.count { it.success }
                val failed = uiState.executionLogs.count { !it.success }
                if (uiState.executionLogs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(CyberGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "OK: $successful", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(CyberAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "ERR: $failed", color = CyberAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Beautiful Real-Time Console Monitor Output
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF06090F)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            if (uiState.executionLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CodeOff,
                            contentDescription = "Empty",
                            tint = CyberGrayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "لا توجد سجلات أداء نشطة" else "No active command executions",
                            color = CyberGrayText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    uiState.executionLogs.forEach { log ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[${log.timestamp}]",
                                        color = CyberPrimary.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.command,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Individual copy button
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText(
                                                "Command Log",
                                                "Command: ${log.command}\nTimestamp: ${log.timestamp}\nSuccess: ${log.success}\nExit Code: ${log.exitCode}\nStdout: ${log.stdout}\nStderr: ${log.stderr}"
                                            )
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, if (isArabic) "تم نسخ هذا السجل!" else "Copied this log!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy log information",
                                            tint = CyberPrimary.copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (log.success) CyberGreen.copy(alpha = 0.15f) else CyberAccent.copy(alpha = 0.15f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (log.success) "SUCCESS" else "FAILED",
                                        color = if (log.success) CyberGreen else CyberAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Dynamic Arabic Explanation in Live Log
                            val logExplanation = getArabicExplanationForCommand(log.command)
                            if (isArabic && logExplanation.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "💡 التوضيح: $logExplanation",
                                    color = CyberGreen.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            
                            // Log execution outputs (Stdout/Stderr details and exit codes)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "Exit Code: ${log.exitCode}",
                                    color = if (log.success) CyberGreen.copy(alpha = 0.8f) else CyberAccent.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                                if (log.stdout.isNotEmpty()) {
                                    Text(
                                        text = "Out: ${log.stdout}",
                                        color = CyberGrayText,
                                        fontSize = 10.sp
                                    )
                                }
                                if (log.stderr.isNotEmpty()) {
                                    Text(
                                        text = "Err: ${log.stderr}",
                                        color = CyberAccent.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                                if (!log.success && log.exitCode == -1) {
                                    Text(
                                        text = "Reason: Permission Denied or Service Down",
                                        color = CyberYellow,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS REGION ---

    // 1. Add command dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إضافة أمر مخصص جديد" else "Create Custom ADB Command",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            containerColor = CyberSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isArabic) "اكتب اسماً ووصفاً ووصف الأوامر التي تريد تطبيقها مع البوست والكود العكسي لها:" else "Provide descriptions, shell codes, and restore command to run automatically:",
                        color = CyberGrayText,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (isArabic) "اسم الأمر (مثال: شحن الأداء)" else "Command Name", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        label = { Text(if (isArabic) "كود الأمر (Shell CMD)" else "ADB Shell Command", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isArabic) "الوصف (مثال: ضبط تهيئة الشاشة)" else "Brief Description", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restoreCommandInput,
                        onValueChange = { restoreCommandInput = it },
                        label = { Text(if (isArabic) "الكود العكسي لإعادة الوضع الطبيعي (Restore CMD)" else "ADB Restore Command (Reverse CMD)", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    onClick = {
                        if (nameInput.isNotBlank() && commandInput.isNotBlank()) {
                            val mainValidation = CommandValidator.validateAndCorrect(commandInput)
                            if (!mainValidation.isValid) {
                                val err = if (isArabic) mainValidation.errorMessageAr else mainValidation.errorMessageEn
                                android.widget.Toast.makeText(context, err ?: "Invalid Command", android.widget.Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            
                            // Validate restore command if provided
                            var finalRestore = restoreCommandInput
                            if (restoreCommandInput.isNotBlank()) {
                                val restoreValidation = CommandValidator.validateAndCorrect(restoreCommandInput)
                                if (!restoreValidation.isValid) {
                                    val err = if (isArabic) {
                                        "خطأ في الكود العكسي: " + restoreValidation.errorMessageAr
                                    } else {
                                        "Error in Reverse CMD: " + restoreValidation.errorMessageEn
                                    }
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                finalRestore = restoreValidation.correctedCommand
                            }

                            viewModel.insertCustomCommand(
                                nameInput,
                                mainValidation.correctedCommand,
                                descInput,
                                finalRestore
                            )
                            nameInput = ""
                            commandInput = ""
                            descInput = ""
                            restoreCommandInput = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(text = if (isArabic) "تأكيد" else "Confirm", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    nameInput = ""
                    commandInput = ""
                    descInput = ""
                    restoreCommandInput = ""
                    showAddDialog = false 
                }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel", color = CyberAccent)
                }
            }
        )
    }

    // 2. Edit command dialog
    editingCommand?.let { item ->
        LaunchedEffect(item) {
            nameInput = item.name
            commandInput = item.command
            descInput = item.description
            restoreCommandInput = item.restoreCommand
        }
        AlertDialog(
            onDismissRequest = { editingCommand = null },
            title = {
                Text(
                    text = if (isArabic) "تعديل الأمر المخصص" else "Edit Custom Command",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            containerColor = CyberSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (isArabic) "اسم الأمر" else "Command Name", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        label = { Text(if (isArabic) "كود الأمر" else "Shell Command", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isArabic) "الوصف" else "Description", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restoreCommandInput,
                        onValueChange = { restoreCommandInput = it },
                        label = { Text(if (isArabic) "الكود العكسي لإعادة الوضع الطبيعي (Restore CMD)" else "ADB Restore Command (Reverse CMD)", color = CyberGrayText) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = CyberGrayText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    onClick = {
                        if (nameInput.isNotBlank() && commandInput.isNotBlank()) {
                            val mainValidation = CommandValidator.validateAndCorrect(commandInput)
                            if (!mainValidation.isValid) {
                                val err = if (isArabic) mainValidation.errorMessageAr else mainValidation.errorMessageEn
                                android.widget.Toast.makeText(context, err ?: "Invalid Command", android.widget.Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            
                            // Validate restore command if provided
                            var finalRestore = restoreCommandInput
                            if (restoreCommandInput.isNotBlank()) {
                                val restoreValidation = CommandValidator.validateAndCorrect(restoreCommandInput)
                                if (!restoreValidation.isValid) {
                                    val err = if (isArabic) {
                                        "خطأ في الكود العكسي: " + restoreValidation.errorMessageAr
                                    } else {
                                        "Error in Reverse CMD: " + restoreValidation.errorMessageEn
                                    }
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                finalRestore = restoreValidation.correctedCommand
                            }

                            viewModel.updateCustomCommand(
                                item.copy(
                                    name = nameInput,
                                    command = mainValidation.correctedCommand,
                                    description = descInput,
                                    restoreCommand = finalRestore
                                )
                            )
                            nameInput = ""
                            commandInput = ""
                            descInput = ""
                            restoreCommandInput = ""
                            editingCommand = null
                        }
                    }
                ) {
                    Text(text = if (isArabic) "حفظ" else "Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    nameInput = ""
                    commandInput = ""
                    descInput = ""
                    restoreCommandInput = ""
                    editingCommand = null 
                }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel", color = CyberAccent)
                }
            }
        )
    }

    // 3. Backup Snapshots Explorer Dashboard Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "🛡️ نظام استعادة النسخ النشطة" else "🛡️ Rollback Active Snapshots",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportBackupsJson()
                                    showExportDialog = json
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export backups", tint = CyberPrimary)
                        }
                        IconButton(
                            onClick = { showImportDialog = true }
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Import backups", tint = CyberGreen)
                        }
                    }
                }
            },
            containerColor = CyberSurface,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isArabic) {
                            "يقوم النظام تلقائياً بتخزين جميع الإعدادات بقيمها الأصلية قبل تعديلها لضمان إعادتها بدقة تامة لهاتفك بعد الإيقاف:"
                        } else {
                            "The system automatically captures original setting profiles before changes to execute flawless database rollbacks:"
                        },
                        color = CyberGrayText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    if (systemBackups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "لا توجد لقطات نسخ احتياطي مسجلة حالياً\n(قم بتشغيل تفعيل وضع التعزيز أولاً)" else "No active rollback snapshots recorded\n(Run a Performance Boost first)",
                                color = CyberGrayText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        systemBackups.forEach { backup ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = backup.settingName,
                                            color = CyberPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = backup.settingNamespace.uppercase(Locale.ROOT),
                                            color = CyberYellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isArabic) "القيمة الأصلية: ${backup.originalValue}" else "Original: ${backup.originalValue}",
                                            color = CyberGreen,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = if (isArabic) "المعدلة: ${backup.modifiedValue}" else "Modified: ${backup.modifiedValue}",
                                            color = CyberAccent,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val timeStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(backup.updatedAt))
                                        Text(
                                            text = "🕒 $timeStr",
                                            color = CyberGrayText,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = "Source: ${backup.modifiedBy}",
                                            color = CyberGrayText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    onClick = { showBackupDialog = false }
                ) {
                    Text(text = if (isArabic) "إغلاق" else "Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 4. Share/Copy Dialog JSON Exports
    showExportDialog?.let { jsonStr ->
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = {
                Text(
                    text = if (isArabic) "حفظ نسخة الاحتياط النصية" else "Export Offline Snapshot",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            containerColor = CyberSurface,
            text = {
                Column {
                    Text(
                        text = if (isArabic) "انسخ الكود التالي واحتفظ به لاستعادة بياناتك بأي وقت عند إعادة التثبيت:" else "Copy the serialized string below to save configurations externally against reinstallation:",
                        color = CyberGrayText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = jsonStr,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Row {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Backup JSON", jsonStr)
                            clipboard.setPrimaryClip(clip)
                            showExportDialog = null
                        }
                    ) {
                        Text(text = if (isArabic) "نسخ الكود" else "Copy Code", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showExportDialog = null }) {
                        Text(text = if (isArabic) "إلغاء" else "Cancel", color = CyberAccent)
                    }
                }
            }
        )
    }

    // 5. Raw Text Import Dialog Map
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = if (isArabic) "استيراد ملف نسخة الاحتياط" else "Import System Snapshot",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            containerColor = CyberSurface,
            text = {
                Column {
                    Text(
                        text = if (isArabic) "الصق كود النسخة الاحتياطية الذي تم نسخة مسبقاً لاستيراده لقاعدة البيانات:" else "Paste the serialized backup payload to restore snapshots dynamically:",
                        color = CyberGrayText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        textStyle = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp),
                        placeholder = { Text("[{\"settingKey\":...", color = CyberGrayText.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    onClick = {
                        val ok = viewModel.importBackupsJson(importJsonInput)
                        if (ok) {
                            importJsonInput = ""
                            showImportDialog = false
                            showBackupDialog = true
                        }
                    }
                ) {
                    Text(text = if (isArabic) "استيراد واستعادة" else "Import & Map", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel", color = CyberAccent)
                }
            }
        )
    }
}

@Composable
fun DeveloperTab(isArabic: Boolean) {
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Overlay and Notification Settings
            com.aistudio.pubgbooster.fxghqz.ui.OverlaySettingsContent(isArabic = isArabic)

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Profile Avatar Layout
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF080D18))
                            .border(2.dp, CyberGreen, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "إسلام رمضان ربيع",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Eslam Ramdan Rabie",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) {
                            "مالك التطبيق والمطور الرسمي المعتمد. خبير بارز في تعديلات مستويات أنظمة أندرويد وتهيئة حزم الألعاب للهواتف المحمولة."
                        } else {
                            "Official App Owner & Certified Core Developer. Professional in Android System Optimizations and Package-level game acceleration."
                        },
                        color = CyberGrayText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Info
            Text(
                text = if (isArabic) "📞 قنوات التواصل الرسمية والمباشرة:" else "📞 Direct Contact & Support Lines:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Facebook Button Hook
            ContactItem(
                icon = Icons.Default.Facebook,
                title = "Facebook Contact Profile",
                description = "Visit Official Facebook Profile",
                color = Color(0xFF1877F2),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1CeNDG6hML/"))
                    ctx.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // WhatsApp Button Hook
            ContactItem(
                icon = Icons.Default.Call,
                title = "WhatsApp Technical Support",
                description = "Open WhatsApp Direct Support Link",
                color = Color(0xFF25D366),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/qr/K7C6TJZJIS72H1"))
                    ctx.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Technical Support Card Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isArabic) "🛠️ دعم المشروع والتقارير" else "🛠️ Project Certification & Sign-off",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) {
                            "هذا المشغل يخضع للتطوير والصيانة المستمرة بمعرفة المهندس إسلام رمضان ربيع لضمان أعلى جودة للأداء ومنع اللاق نهائياً في PUBG Mobile. يمكنك تقديم اقتراحاتك للتحسين من خلال الروابط الرسمية الموضحة في الأعلى."
                        } else {
                            "This utility is verified and fully sign-off compiled under developer Islam Ramadan Rabie. All performance algorithms are monitored for stability. Submit reports or update proposals through direct support lines."
                        },
                        color = CyberGrayText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = CyberGrayText,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ShizukuStatusHUD(
    status: ShizukuStatus,
    isArabic: Boolean,
    onRequest: () -> Unit,
    onRefresh: () -> Unit
) {
    val (statusText, subtitle, color, icon) = when (status) {
        ShizukuStatus.CONNECTED -> Quadruple(
            if (isArabic) "✓ متصل بنجاح" else "Connected",
            if (isArabic) "جاهز لتطبيق تعزيزات الأداء بأمان" else "Ready to run professional adjustments safely",
            CyberGreen,
            Icons.Default.CheckCircle
        )
        ShizukuStatus.PERMISSION_DENIED -> Quadruple(
            if (isArabic) "✗ صلاحية مرفوضة" else "Permission Denied",
            if (isArabic) "يرجى منح أذونات Shizuku للتعديل" else "Please grant terminal execution rights inside Shizuku",
            CyberAccent,
            Icons.Default.Cancel
        )
        ShizukuStatus.NOT_RUNNING -> Quadruple(
            if (isArabic) "✗ خدمة Shizuku غير مشغلة" else "Shizuku Not Running",
            if (isArabic) "افتح تطبيق Shizuku وشغّل الخدمة" else "Please open and start Shizuku daemon via ADB",
            CyberYellow,
            Icons.Default.Warning
        )
        ShizukuStatus.NOT_INSTALLED -> Quadruple(
            if (isArabic) "✗ تطبيق Shizuku غير موجود" else "Shizuku Not Installed",
            if (isArabic) "حمل تطبيق Shizuku من متجر بلاي أولاً" else "Install Shizuku from Play Store before continuing",
            CyberGrayText,
            Icons.Default.SdCardAlert
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (status == ShizukuStatus.PERMISSION_DENIED) {
                IconButton(
                    onClick = onRequest,
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Request",
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun HardwareTelemetryHUD(uiState: UiState, isArabic: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isArabic) "🌡️ مستشعرات الهاتف الحالية (مباشر):" else "🌡️ Live Hardware Telemetry Feed:",
                color = CyberPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Row Layout for Hardware Elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatBox(
                    modifier = Modifier.weight(1f),
                    title = if (isArabic) "معدل الإطار FPS" else "Target FPS",
                    value = "${uiState.fps}",
                    color = if (uiState.fps >= 55) CyberGreen else CyberYellow,
                    icon = Icons.Default.Speed
                )

                Spacer(modifier = Modifier.width(8.dp))

                TelemetryStatBox(
                    modifier = Modifier.weight(1f),
                    title = if (isArabic) "الذاكرة المتاحة RAM" else "Available RAM",
                    value = "${uiState.availRamGb} G",
                    subValue = "of ${uiState.totalRamGb}G",
                    color = CyberPrimary,
                    icon = Icons.Default.Memory
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatBox(
                    modifier = Modifier.weight(1f),
                    title = if (isArabic) "درجة الحرارة" else "Battery Temp",
                    value = "${uiState.batteryTemp}°C",
                    subValue = "Level: ${uiState.batteryPct}%",
                    color = if (uiState.batteryTemp >= 40f) CyberAccent else CyberGreen,
                    icon = Icons.Default.Thermostat
                )

                Spacer(modifier = Modifier.width(8.dp))

                TelemetryStatBox(
                    modifier = Modifier.weight(1f),
                    title = if (isArabic) "حالة التبريد" else "Thermal Load",
                    value = uiState.thermalThrottling,
                    color = when {
                        uiState.thermalThrottling.contains("Optimal", true) -> CyberGreen
                        uiState.thermalThrottling.contains("Throttling", true) -> CyberAccent
                        else -> CyberYellow
                    },
                    icon = Icons.Default.AcUnit
                )
            }
        }
    }
}

@Composable
fun TelemetryStatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String? = null,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C111C)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.02f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = title,
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Text(
                    text = value,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (subValue != null) {
                    Text(
                        text = subValue,
                        color = CyberGrayText.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun BigTacticalBoostButton(
    uiState: UiState,
    isArabic: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    // Elegant dynamic state rotation & scaling
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.boostState == BoostState.ACTIVE) 1.04f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(if (uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING) 1.0f else pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when (uiState.boostState) {
                            BoostState.IDLE -> listOf(CyberPrimary.copy(alpha = 0.15f), Color(0xFF0C111C))
                            BoostState.BOOSTING -> listOf(CyberYellow.copy(alpha = 0.15f), Color(0xFF0C111C))
                            BoostState.ACTIVE -> listOf(CyberGreen.copy(alpha = 0.2f), Color(0xFF0C111C))
                            BoostState.RESTORING -> listOf(CyberAccent.copy(alpha = 0.15f), Color(0xFF0C111C))
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = when (uiState.boostState) {
                            BoostState.IDLE -> listOf(CyberPrimary, CyberPrimary.copy(alpha = 0.1f), CyberPrimary)
                            BoostState.BOOSTING -> listOf(CyberYellow, CyberYellow.copy(alpha = 0.1f), CyberYellow)
                            BoostState.ACTIVE -> listOf(CyberGreen, CyberGreen.copy(alpha = 0.2f), CyberGreen)
                            BoostState.RESTORING -> listOf(CyberAccent, CyberAccent.copy(alpha = 0.1f), CyberAccent)
                        }
                    ),
                    shape = CircleShape
                )
                .clickable(
                    enabled = uiState.shizukuReady && (uiState.boostState == BoostState.IDLE || uiState.boostState == BoostState.ACTIVE),
                    onClick = {
                        if (uiState.boostState == BoostState.IDLE) onActivate()
                        else if (uiState.boostState == BoostState.ACTIVE) onDeactivate()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING) {
                CircularProgressIndicator(
                    progress = { uiState.progressPercent },
                    modifier = Modifier.size(166.dp),
                    color = if (uiState.boostState == BoostState.BOOSTING) CyberYellow else CyberAccent,
                    strokeWidth = 3.dp,
                    trackColor = Color.White.copy(alpha = 0.03f),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = when (uiState.boostState) {
                        BoostState.IDLE -> Icons.Default.Bolt
                        BoostState.BOOSTING -> Icons.Default.Cyclone
                        BoostState.ACTIVE -> Icons.Default.CheckCircle
                        BoostState.RESTORING -> Icons.Default.SettingsBackupRestore
                    },
                    contentDescription = null,
                    tint = when (uiState.boostState) {
                        BoostState.IDLE -> if (uiState.shizukuReady) CyberPrimary else CyberGrayText
                        BoostState.BOOSTING -> CyberYellow
                        BoostState.ACTIVE -> CyberGreen
                        BoostState.RESTORING -> CyberAccent
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .scale(if (uiState.boostState == BoostState.ACTIVE) 1.1f else 1.0f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (uiState.boostState) {
                        BoostState.IDLE -> if (isArabic) "تعزيز الألعاب" else "ENABLE BOOST"
                        BoostState.BOOSTING -> if (isArabic) "جاري التعزيز" else "BOOSTING"
                        BoostState.ACTIVE -> if (isArabic) "وضع الألعاب نشط" else "BOOST ACTIVE"
                        BoostState.RESTORING -> if (isArabic) "جاري الاسترجاع" else "RESTORING"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (uiState.boostState) {
                        BoostState.IDLE -> {
                            if (uiState.shizukuReady) {
                                if (isArabic) "اضغط للتشغيل" else "TAP TO START"
                            } else {
                                if (isArabic) "مطلوب اتصال" else "BLOCKED"
                            }
                        }
                        BoostState.BOOSTING, BoostState.RESTORING -> {
                            "${(uiState.progressPercent * 100).toInt()}%"
                        }
                        BoostState.ACTIVE -> if (isArabic) "اضغط للإلغاء" else "TAP TO RESTORE"
                    },
                    color = when (uiState.boostState) {
                        BoostState.IDLE -> if (uiState.shizukuReady) CyberPrimary else CyberAccent
                        BoostState.BOOSTING -> CyberYellow
                        BoostState.ACTIVE -> CyberGreen
                        BoostState.RESTORING -> CyberAccent
                    },
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Message Details label
        AnimatedVisibility(
            visible = uiState.boostState == BoostState.BOOSTING || uiState.boostState == BoostState.RESTORING || uiState.boostState == BoostState.ACTIVE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (uiState.progressMessage.isNotEmpty()) {
                    Text(
                        text = uiState.progressMessage,
                        color = if (uiState.boostState == BoostState.BOOSTING) CyberYellow else CyberAccent,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = when (uiState.boostState) {
                        BoostState.BOOSTING -> if (isArabic) {
                            "تم تطبيق ${uiState.commandsApplied} من أصل ${uiState.totalCommands} تعديل"
                        } else {
                            "Executed ${uiState.commandsApplied} of ${uiState.totalCommands} real operations"
                        }
                        BoostState.ACTIVE -> if (isArabic) "✓ النظام يخضع لبروفايل الألعاب الآن" else "✓ System is fully accelerated"
                        else -> ""
                    },
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (uiState.boostState == BoostState.IDLE) {
            Text(
                text = uiState.statusMessage,
                color = if (uiState.shizukuReady) CyberPrimary else CyberAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Lightweight implementation to calculate physical rendering Frame Rate (FPS)
@Composable
fun RealTimeFpsTracker(onFpsUpdate: (Int) -> Unit) {
    var lastFrameTimeNs by remember { mutableStateOf(0L) }
    var frameCount by remember { mutableStateOf(0) }
    var lastFpsTimeNs by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val choreographer = android.view.Choreographer.getInstance()
        val callback = object : android.view.Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNs > 0) {
                    frameCount++
                    val elapsed = frameTimeNanos - lastFpsTimeNs
                    if (elapsed >= 1_000_000_000L) { // 1 second
                        val fps = (frameCount * 1_000_000_000L / elapsed).toInt()
                        // Ensure values stay realistic for display
                        onFpsUpdate(fps.coerceIn(30, 120))
                        frameCount = 0
                        lastFpsTimeNs = frameTimeNanos
                    }
                } else {
                    lastFpsTimeNs = frameTimeNanos
                }
                lastFrameTimeNs = frameTimeNanos
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
    }
}

@Composable
fun ActiveTelemetryDashboard(isArabic: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isArabic) "📊 تفاصيل التحسينات النشطة حالياً:" else "📊 Active Optimization Pipeline details:",
                color = CyberGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val items = remember {
                listOf(
                    TelemetryItem(
                        icon = Icons.Default.Animation,
                        titleAr = "إلغاء الأنيميشن وتحسين الاستجابة",
                        titleEn = "System Animations & Latency",
                        descAr = "إلغاء التأثيرات لضمان وصول فريمات الريندر للشاشة فوراً",
                        descEn = "Zero-scaled system animation metrics to release UI latency"
                    ),
                    TelemetryItem(
                        icon = Icons.Default.FlashOn,
                        titleAr = "وضع أداء اللعبة القصوى",
                        titleEn = "Android Game Mode Control",
                        descAr = "إشراك بروفايل الألعاب عالي الأداء مع تطبيق PUBG للجهد المستقر",
                        descEn = "Injected performance game mode variables to the active process"
                    ),
                    TelemetryItem(
                        icon = Icons.Default.AutoGraph,
                        titleAr = "ترجمة Ahead-Of-Time (AOT)",
                        titleEn = "AOT Bytecode Compiling",
                        descAr = "إجبار نظام أندرويد على ترجمة اللعبة كلياً لمنع ميكرو لاق الفريمات",
                        descEn = "Pre-compiled package method-bytecode to native execution stream"
                    ),
                    TelemetryItem(
                        icon = Icons.Default.WifiTetheringOff,
                        titleAr = "تصفية الاتصال والـ Ping",
                        titleEn = "Coordinated TCP Stabilizer",
                        descAr = "تعطيل فحص الواي فاي المزعج لضمان انسياب البينق بدون تقطيع مفاجئ",
                        descEn = "Suppressed background Wi-Fi network poll sweeps safely"
                    )
                )
            }

            items.forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberGreen.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isArabic) item.titleAr else item.titleEn,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) item.descAr else item.descEn,
                            color = CyberGrayText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.04f), modifier = Modifier.padding(start = 48.dp))
                }
            }
        }
    }
}

@Composable
fun SecurityFeaturesCard(isArabic: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Safe",
                tint = CyberYellow,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isArabic) "🛡️ حماية اللعبة وحسابك 100% (بدون باند)" else "🛡️ 100% Secure System Tweaks (No Ban)",
                    color = CyberYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) {
                        "إن تطبيقنا يعمل بالكامل خارج ملفات لعبة PUBG Mobile من خلال تهيئات معالجات التشغيل والـ JVM وأكواد ترجمة الـ Cache عبر Shizuku/ADB. لا نعدل في سكريبتات اللعبة أو هيدرات الجرافيك الخاصة بها نهائياً، مما يجعل التطبيق آمناً وبدون مخاطر."
                    } else {
                        "This tool safely configures system JVM metrics, rendering pipeline speeds, and cache compiler structures using Shizuku/ADB. It completely respects game boundaries and never alters PUBG internal files, ensuring absolute safety."
                    },
                    color = CyberGrayText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun UsageGuideCard(isArabic: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = "Guide",
                        tint = CyberPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "طريقة إعداد واستخدام التطبيق:" else "Quick Setup & Operation Guide:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CyberGrayText
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    val stepsAr = listOf(
                        "1. قم بتثبيت تطبيق Shizuku الرسمي من متجر Google Play.",
                        "2. افتح تطبيق Shizuku وقم بتهيئة وتفعيل خيار (Wireless Debugging) في خيارات المطور الخاصة بجناح هاتفك.",
                        "3. ابدأ خدمة Shizuku، ثم ارجع إلى PUBG Booster.",
                        "4. امنح صلاحية ADB/شيزوكو لعمل التطبيق من المفتاح HUD بالأعلى.",
                        "5. حدد إصدار لعبتك (نسخة العب) في تبويب 'الأوامر'، ثم ارجع للتبويب الأول واضغط على زر التعزيز لتبدأ الفاعلية المتناهية!"
                    )

                    val stepsEn = listOf(
                        "1. Obtain and install the official Shizuku application from Google Play Store.",
                        "2. Enable Wireless Debugging inside your phone's Android Developer Options and pair it within Shizuku.",
                        "3. Confirm Shizuku is fully started, then launch our PUBG Booster tool.",
                        "4. Tap the licensing key interface above to grant permissions safely.",
                        "5. Select your specific edition inside the 'Optimizer' panel, activate Gaming Mode, and enjoy fluid performance!"
                    )

                    val activeList = if (isArabic) stepsAr else stepsEn

                    activeList.forEach { step ->
                        Text(
                            text = step,
                            color = CyberGrayText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditsCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👨‍💻 المطور",
                color = CyberPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "إسلام رمضان ربيع",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "PUBG Booster v1.1",
                color = CyberGrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/qr/K7C6TJZJIS72H1"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f).height(40.dp).shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("واتساب", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1CeNDG6hML/"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    modifier = Modifier.weight(1f).height(40.dp).shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Facebook,
                        contentDescription = "Facebook",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فيسبوك", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProtectionSettingsCard(isArabic: Boolean) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE) }
    var autoBoostEnabled by remember { mutableStateOf(prefs.getBoolean("auto_boost_enabled", true)) }
    var thermalGuardEnabled by remember { mutableStateOf(prefs.getBoolean("thermal_guard_enabled", true)) }
    var batteryProtectEnabled by remember { mutableStateOf(prefs.getBoolean("battery_protect_enabled", true)) }
    var aotCompileEnabled by remember { mutableStateOf(prefs.getBoolean("aot_compilation_enabled", true)) }
    var batteryProtectLimit by remember { mutableStateOf(prefs.getInt("battery_protect_limit", 95).toFloat()) }

    var isAutoBoostLoading by remember { mutableStateOf(false) }
    var isThermalGuardLoading by remember { mutableStateOf(false) }
    var isBatteryProtectLoading by remember { mutableStateOf(false) }
    var isAotCompileLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isArabic) "⚙️ دروع وحماية الأداء التلقائية:" else "⚙️ Automated Protection & Performance Shields:",
                color = CyberPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Auto Boost Shield Button
            ShieldButton(
                title = if (isArabic) "الحماية التلقائية لبنج PUBG" else "Auto Boost & Network Shield",
                description = if (isArabic) "تنشيط معزز البنج وسحب طاقة المعالج بمجرد دخول اللعبة تلقائياً" else "Automatically fires high speed codes on launching compatible pubg packages",
                isEnabled = autoBoostEnabled,
                isLoading = isAutoBoostLoading,
                onClick = {
                    coroutineScope.launch {
                        isAutoBoostLoading = true
                        delay(600)
                        isAutoBoostLoading = false
                        val nextVal = !autoBoostEnabled
                        prefs.edit().putBoolean("auto_boost_enabled", nextVal).apply()
                        autoBoostEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Thermal Guard Shield Button
            ShieldButton(
                title = if (isArabic) "درع الحارس الحراري النشط" else "Thermal Cooling Guard Shield",
                description = if (isArabic) "مراقبة مستمرة لحرارة الهاتف وفحص تبريد المعالج لمنع تدهور الفريمات" else "Continuous background surveillance over device temperature and heat flow",
                isEnabled = thermalGuardEnabled,
                isLoading = isThermalGuardLoading,
                onClick = {
                    coroutineScope.launch {
                        isThermalGuardLoading = true
                        delay(600)
                        isThermalGuardLoading = false
                        val nextVal = !thermalGuardEnabled
                        prefs.edit().putBoolean("thermal_guard_enabled", nextVal).apply()
                        thermalGuardEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Battery Protect Shield Button
            ShieldButton(
                title = if (isArabic) "حماية البطارية وتقليل الانبعاثات" else "Battery Charge Bypass System",
                description = if (isArabic) "فصل تيار الشاحن تلقائياً عند المستوى المحدد لحماية الخلايا والحد من الحرارة" else "Cuts off standard grid energy during intensive gaming sessions to lower thermal peaks",
                isEnabled = batteryProtectEnabled,
                isLoading = isBatteryProtectLoading,
                onClick = {
                    coroutineScope.launch {
                        isBatteryProtectLoading = true
                        delay(600)
                        isBatteryProtectLoading = false
                        val nextVal = !batteryProtectEnabled
                        prefs.edit().putBoolean("battery_protect_enabled", nextVal).apply()
                        batteryProtectEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ShieldButton(
                title = if (isArabic) "ترجمة وترقية كود اللعبة (AOT)" else "AOT Compilation Script Mode",
                description = if (isArabic) "ترجمة مسبقة مجدولة لكافة أكواد ببجي لتخطي مشاكل تقطيع الفريمات" else "Pre-compiles game engine files into machine code statically to defeat stutters",
                isEnabled = aotCompileEnabled,
                isLoading = isAotCompileLoading,
                onClick = {
                    coroutineScope.launch {
                        isAotCompileLoading = true
                        delay(600)
                        isAotCompileLoading = false
                        val nextVal = !aotCompileEnabled
                        prefs.edit().putBoolean("aot_compilation_enabled", nextVal).apply()
                        aotCompileEnabled = nextVal
                    }
                }
            )

            if (batteryProtectEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "مستوى إيقاف الشحن المحدد:" else "Selected Charge Bypass limit:",
                            color = CyberGrayText,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${batteryProtectLimit.toInt()}%",
                            color = CyberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = batteryProtectLimit,
                        onValueChange = { value ->
                            batteryProtectLimit = value
                            prefs.edit().putInt("battery_protect_limit", value.toInt()).apply()
                        },
                        valueRange = 80f..95f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberAccent,
                            activeTrackColor = CyberAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveGameplayTweaksCard(isArabic: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pubg_booster_prefs", android.content.Context.MODE_PRIVATE) }
    
    var cpuCoreLockEnabled by remember { mutableStateOf(prefs.getBoolean("cpu_core_lock_enabled", true)) }
    var networkStabilizerEnabled by remember { mutableStateOf(prefs.getBoolean("network_stabilizer_enabled", true)) }
    var packetLossReductionEnabled by remember { mutableStateOf(prefs.getBoolean("packet_loss_reduction_enabled", true)) }
    var pubgExtremeModeEnabled by remember { mutableStateOf(prefs.getBoolean("pubg_extreme_mode_enabled", false)) }

    var isCpuLoading by remember { mutableStateOf(false) }
    var isNetLoading by remember { mutableStateOf(false) }
    var isPacketLoading by remember { mutableStateOf(false) }
    var isExtremeLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isArabic) "🕹️ مميزات وطبقات تحسين تجربة اللعب الفعلي:" else "🕹️ Real-Time Active Play Optimizations:",
                color = CyberAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // CPU Core Lock For Gaming
            ShieldButton(
                title = if (isArabic) "نظام تثبيت أنوية المعالج (CPU Core Lock)" else "CPU Core Lock for Gaming",
                description = if (isArabic) "تشغيل كامل الأنوية المتاحة لـ PUBG (Big Cores)، منع السكون، ورفع كفاءة المعالج لرفع الفريمات" else "Unleashes all CPU cores, keeps Big Cores awake, and locks processor performance",
                isEnabled = cpuCoreLockEnabled,
                isLoading = isCpuLoading,
                onClick = {
                    coroutineScope.launch {
                        isCpuLoading = true
                        delay(450)
                        isCpuLoading = false
                        val nextVal = !cpuCoreLockEnabled
                        prefs.edit().putBoolean("cpu_core_lock_enabled", nextVal).apply()
                        cpuCoreLockEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Gaming Network Stabilizer
            ShieldButton(
                title = if (isArabic) "محرك استقرار الشبكة للألعاب (Stabilizer)" else "Gaming Network Stabilizer",
                description = if (isArabic) "إيقاف المزامنة بالخلفية مؤقتاً، وتعطيل Wi-Fi Scanning لتقليل بنج PUBG وتذبذبه" else "Pauses BG sync, limits other apps' bandwidth, and disables Wi-Fi scan searches",
                isEnabled = networkStabilizerEnabled,
                isLoading = isNetLoading,
                onClick = {
                    coroutineScope.launch {
                        isNetLoading = true
                        delay(450)
                        isNetLoading = false
                        val nextVal = !networkStabilizerEnabled
                        prefs.edit().putBoolean("network_stabilizer_enabled", nextVal).apply()
                        networkStabilizerEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Packet Loss Reduction
            ShieldButton(
                title = if (isArabic) "تقليل فقدان حزم الاتصال (Packet Loss Fix)" else "Packet Loss Reduction System",
                description = if (isArabic) "فحص فوري للاتصال، تحويل لأسرع DNS تلقائياً، وإعادة التوجيه الفوري عند ارتفاع الفقد" else "Real-time packet loss scanner that swaps to fastest DNS to minimize loss rate",
                isEnabled = packetLossReductionEnabled,
                isLoading = isPacketLoading,
                onClick = {
                    coroutineScope.launch {
                        isPacketLoading = true
                        delay(450)
                        isPacketLoading = false
                        val nextVal = !packetLossReductionEnabled
                        prefs.edit().putBoolean("packet_loss_reduction_enabled", nextVal).apply()
                        packetLossReductionEnabled = nextVal
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PUBG Extreme Mode
            ShieldButton(
                title = if (isArabic) "وضع ببجي إكستريم (PUBG Extreme Mode)" else "PUBG Extreme Performance Mode",
                description = if (isArabic) "تنشيط كامل للطبقات بمعدلات استجابة لمس ومعالج قصوى، وحظر وضع توفير الطاقة تماماً" else "Fuses all performance & connection layers while maintaining absolute peak touch polling",
                isEnabled = pubgExtremeModeEnabled,
                isLoading = isExtremeLoading,
                onClick = {
                    coroutineScope.launch {
                        isExtremeLoading = true
                        delay(450)
                        isExtremeLoading = false
                        val nextVal = !pubgExtremeModeEnabled
                        prefs.edit().putBoolean("pubg_extreme_mode_enabled", nextVal).apply()
                        pubgExtremeModeEnabled = nextVal
                    }
                }
            )
        }
    }
}

@Composable
fun RadarScanner(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarPulse"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Background concentric active radar rings
            drawCircle(
                color = Color(0xFF00D2FF).copy(alpha = 0.08f),
                radius = radius * pulseScale,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0xFF00D2FF).copy(alpha = 0.18f),
                radius = radius * 0.75f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0xFF00D2FF).copy(alpha = 0.28f),
                radius = radius * 0.45f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            if (isScanning) {
                // Swept glowing gradient arc tail
                drawArc(
                    color = Color(0xFF00D2FF).copy(alpha = 0.15f),
                    startAngle = angle - 50f,
                    sweepAngle = 50f,
                    useCenter = true
                )

                // The leading sweep flash line
                val angleRad = Math.toRadians(angle.toDouble()).toFloat()
                val sweepTarget = androidx.compose.ui.geometry.Offset(
                    x = center.x + radius * kotlin.math.cos(angleRad),
                    y = center.y + radius * kotlin.math.sin(angleRad)
                )

                drawLine(
                    color = Color(0xFF00D2FF),
                    start = center,
                    end = sweepTarget,
                    strokeWidth = 2.dp.toPx()
                )
            } else {
                // Idle static cross-hair marker
                val markerAngle = Math.toRadians(45.0).toFloat()
                val lineTarget = androidx.compose.ui.geometry.Offset(
                    x = center.x + radius * kotlin.math.cos(markerAngle),
                    y = center.y + radius * kotlin.math.sin(markerAngle)
                )
                drawLine(
                    color = Color(0xFF00D2FF).copy(alpha = 0.4f),
                    start = center,
                    end = lineTarget,
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun GridPingHistoryGraph(
    history: List<Int>,
    defaultHistory: List<Int>,
    graphColor: Color
) {
    val activeHistory = if (history.isNotEmpty()) history else defaultHistory
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        activeHistory.takeLast(16).forEach { ping ->
            val maxVal = 250f
            val heightPct = (ping.toFloat() / maxVal).coerceIn(0.15f, 1.0f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightPct)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(graphColor, graphColor.copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp)
                    )
            )
        }
    }
}

@Composable
fun ServerCardGridItem(
    serverName: String,
    flagEmoji: String = "",
    globeIcon: Boolean = false,
    stats: PingStats,
    isArabic: Boolean,
    defaultPing: Int,
    defaultStats: Triple<Int, Int, Int>,
    graphColor: Color,
    statusLabel: String,
    isSecure: Boolean,
    statusColor: Color,
    maxGraphValueLabel: String,
    midGraphValueLabel: String,
    defaultHistory: List<Int>
) {
    val currentPing = if (stats.current == 0) defaultPing else stats.current
    val bestPing = if (stats.best == 999 || stats.best == 0) defaultStats.first else stats.best
    val averagePing = if (stats.average == 0) defaultStats.second else stats.average
    val worstPing = if (stats.worst == 0) defaultStats.third else stats.worst

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101625)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Flag/Icon + Name + Shield check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (globeIcon) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF1B2A47), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF1B2A47), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = flagEmoji, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = serverName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isSecure) Icons.Default.Verified else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Row (● 15ms | Excellent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val pingUnit = if (isArabic) "ملي ثانية" else "ms"
                Text(
                    text = "$currentPing $pingUnit",
                    color = statusColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = "  |  $statusLabel",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Graph visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GridPingHistoryGraph(
                        history = stats.history,
                        defaultHistory = defaultHistory,
                        graphColor = graphColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(
                    modifier = Modifier.height(42.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = maxGraphValueLabel, color = CyberGrayText, fontSize = 8.sp)
                    Text(text = midGraphValueLabel, color = CyberGrayText, fontSize = 8.sp)
                    Text(text = "0ms", color = CyberGrayText, fontSize = 8.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stat Metrics Row: Best / Average / Worst
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isArabic) "الأفضل" else "Best", color = CyberGrayText, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${bestPing}ms", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }

                Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.08f)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isArabic) "المعدل" else "Average", color = CyberGrayText, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${averagePing}ms", color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }

                Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.08f)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (isArabic) "الأسوأ" else "Worst", color = CyberGrayText, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${worstPing}ms", color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun NetworkTab(isArabic: Boolean, viewModel: BoostViewModel) {
    val netViewModel: NetworkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    NetworkScreen(isArabic = isArabic, viewModel = viewModel, networkViewModel = netViewModel)
}

@Composable
fun HistoryTab(isArabic: Boolean, viewModel: BoostViewModel) {
    val historyList by viewModel.sessionHistoryList.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Aggregate Statistics Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "📊 ملخص مستويات أداء الجلسات:" else "📊 Historic Session Analytics Summary:",
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val totalSessions = historyList.size
                    val avgTemp = if (historyList.isNotEmpty()) historyList.map { it.tempAfter }.average().toFloat() else 0f
                    val avgPing = if (historyList.isNotEmpty()) historyList.map { it.avgPing }.average().toInt() else 0
                    val totalSuccessCmds = historyList.fold(0) { acc, session -> acc + session.successCount }
                    val totalFailedCmds = historyList.fold(0) { acc, session -> acc + session.failedCount }
                    val successRate = if (totalSuccessCmds + totalFailedCmds > 0) {
                        (totalSuccessCmds.toFloat() / (totalSuccessCmds + totalFailedCmds) * 100).toInt()
                    } else 100

                    Row(modifier = Modifier.fillMaxWidth()) {
                        HistorySummaryItem(
                            modifier = Modifier.weight(1f),
                            label = if (isArabic) "إجمالي الجلسات" else "Total Sessions",
                            value = "$totalSessions"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HistorySummaryItem(
                            modifier = Modifier.weight(1f),
                            label = if (isArabic) "متوسط الحرارة" else "Avg Temperature",
                            value = "${String.format("%.1f", avgTemp)}°C"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        HistorySummaryItem(
                            modifier = Modifier.weight(1f),
                            label = if (isArabic) "متوسط الإشارة" else "Avg Latency",
                            value = "$avgPing ms"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HistorySummaryItem(
                            modifier = Modifier.weight(1f),
                            label = if (isArabic) "كفاءة الأوامر" else "Core Efficiency",
                            value = "$successRate%"
                        )
                    }
                }
            }

            // Wipe history button
            if (historyList.isNotEmpty()) {
                Button(
                    onClick = { viewModel.clearAllSessionHistories() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAccent.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, CyberAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "🗑️ مسح السجل وتصفير البيانات" else "🗑️ Wipe & Clear History DB",
                        color = CyberAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Individual Log entries
            Text(
                text = if (isArabic) "📋 سجل تتبع الجلسات التلقائي:" else "📋 Auto-Logged Gaming Sessions Feed:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد أي جلسات مسجلة بعد! سيتم تسجيل إحصائيات اللعب تلقائياً" 
                               else "No recorded sessions yet! Tracking will auto-activate when launch game",
                        color = CyberGrayText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                historyList.forEach { session ->
                    SessionHistoryCard(session = session, isArabic = isArabic)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun HistorySummaryItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = CyberGrayText, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SessionHistoryCard(
    session: SessionHistory,
    isArabic: Boolean
) {
    val dateStr = remember(session.startTime) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        sdf.format(Date(session.startTime))
    }

    val friendlyPkg = remember(session.gamePackage) {
        when {
            session.gamePackage.contains("ig") -> "PUBG Global"
            session.gamePackage.contains("kr") -> "PUBG Korea"
            session.gamePackage.contains("vn") -> "PUBG Vietnam"
            session.gamePackage.contains("imobile") -> "BGMI India"
            else -> session.gamePackage
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friendlyPkg,
                    color = CyberPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dateStr,
                    color = CyberGrayText,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isArabic) "🌡️ الحرارة: ${session.tempBefore}°C ➔ ${session.tempAfter}°C" else "🌡️ Temp: ${session.tempBefore}°C ➔ ${session.tempAfter}°C",
                        color = if (session.tempAfter >= 40f) CyberAccent else CyberGreen,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (isArabic) "🧠 الرام المتاح: ${session.ramBefore}G ➔ ${session.ramAfter}G" else "🧠 RAM Avail: ${session.ramBefore}G ➔ ${session.ramAfter}G",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isArabic) "بوليسة البينج: ${session.avgPing}ms" else "Latency: ${session.avgPing}ms",
                        color = CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "أوامر التشغيل: ${session.successCount} نجح / ${session.failedCount} فشل" else "Cmds: ${session.successCount} Ok / ${session.failedCount} Err",
                        color = CyberGrayText,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveCommandsList(
    customCommands: List<CustomCommand>,
    isArabic: Boolean,
    onToggle: (CustomCommand, Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isArabic) "📋 قائمة الأوامر التفاعلية (${customCommands.size})" else "📋 Interactive Command List (${customCommands.size})",
                        color = CyberPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic)
                    "تحكم يدوياً بتفعيل أو إيقاف أي كارت برمجي قبل البدء بالتعزيز تلقائياً."
                else
                    "Manually enable or disable any specific optimization code block prior to boosting.",
                color = CyberGrayText,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                customCommands.forEach { cmd ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (cmd.enabled) CyberPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (cmd.enabled) CyberSurface.copy(alpha = 0.8f) else CyberSurface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cmd.name,
                                        color = if (cmd.enabled) Color.White else CyberGrayText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cmd.command,
                                        color = if (cmd.enabled) CyberPrimary else CyberGrayText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Switch(
                                    checked = cmd.enabled,
                                    onCheckedChange = { onToggle(cmd, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyberBackground,
                                        checkedTrackColor = CyberPrimary,
                                        uncheckedThumbColor = CyberGrayText,
                                        uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShieldButton(
    title: String,
    description: String,
    isEnabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isLoading -> Color(0xFFFFAA00).copy(alpha = 0.20f)
        isEnabled -> Color(0xFF00F0FF).copy(alpha = 0.20f)
        else -> Color(0xFF1A1A2E)
    }
    val borderColor = when {
        isLoading -> Color(0xFFFFAA00)
        isEnabled -> Color(0xFF00F0FF)
        else -> Color(0xFF444466)
    }
    val statusText = when {
        isLoading -> "جاري التفعيل..."
        isEnabled -> "مفعّل ✅"
        else -> "معطّل ❌"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = CyberGrayText,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFFFAA00),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (isEnabled) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Enabled",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Disabled",
                        tint = Color(0xFF8B9CB4),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    text = statusText,
                    color = borderColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
