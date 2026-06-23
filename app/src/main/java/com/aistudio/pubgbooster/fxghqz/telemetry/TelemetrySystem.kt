package com.aistudio.pubgbooster.fxghqz.telemetry

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import com.aistudio.pubgbooster.fxghqz.util.DeviceStats
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

object TelemetrySystem {
    private const val TAG = "TelemetrySystem"
    private const val PREFS_NAME = "telemetry_prefs"
    private const val KEY_BOT_TOKEN = "telegram_bot_token"
    private const val KEY_CHAT_ID = "telegram_chat_id"
    private const val QUEUE_FILENAME = "telemetry_queue.json"
    
    // Default values
    private const val DEFAULT_BOT_TOKEN = "8877837223:AAFJ9fh6AddHbUESN85stjS2Y33ycK5bJJU"
    
    private lateinit var appContext: Context
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Live Diagnostics details for Developer telemetry screen
    private val _totalErrorsCount = MutableStateFlow(0)
    val totalErrorsCount = _totalErrorsCount.asStateFlow()
    
    private val _totalCrashesCount = MutableStateFlow(0)
    val totalCrashesCount = _totalCrashesCount.asStateFlow()
    
    private val _telegramStatus = MutableStateFlow("UNKNOWN")
    val telegramStatus = _telegramStatus.asStateFlow()
    
    private val _lastSentReportJson = MutableStateFlow("")
    val lastSentReportJson = _lastSentReportJson.asStateFlow()
    
    private val _queuedReportsCount = MutableStateFlow(0)
    val queuedReportsCount = _queuedReportsCount.asStateFlow()
    
    // Live tracking memory list of events triggered during this run
    private val _liveTelemetryHistory = MutableStateFlow<List<JSONObject>>(emptyList())
    val liveTelemetryHistory = _liveTelemetryHistory.asStateFlow()
    
    // Queue of reports that need transmission
    private val unsentReportsQueue = java.util.concurrent.ConcurrentLinkedQueue<JSONObject>()

    @Volatile
    var currentLiveFps: Int = 60
    
    // De-duplication set to avoid spamming the same error repeatedly
    private val sentLogsDeduplicator = HashSet<String>()
    
    private var isNetworkAvailable = false
    private var batchJobsActive = false

    fun init(context: Context) {
        appContext = context.applicationContext
        
        // Load counters from SharedPreferences to keep state across close/open
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _totalErrorsCount.value = sp.getInt("err_count", 0)
        _totalCrashesCount.value = sp.getInt("crash_count", 0)
        
        // Load fallback token if not customized
        if (!sp.contains(KEY_BOT_TOKEN)) {
            sp.edit().putString(KEY_BOT_TOKEN, DEFAULT_BOT_TOKEN).apply()
        }
        
        // Set Default Crash Handler
        setupGlobalCrashHandler()
        
        // Restore local queue files from storage
        loadQueueFromFile()
        
        // Listen to internet changes
        setupNetworkCallback()
        
        // Spin background sender loop
        startBackgroundTelemetryQueueProcessor()
        
        Log.d(TAG, "Telemetry & Diagnostics system fully initialized.")
    }

    // Dynamic Getters/Setters for Telegram integration
    fun getBotToken(): String {
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_BOT_TOKEN, DEFAULT_BOT_TOKEN) ?: DEFAULT_BOT_TOKEN
    }

    fun setBotToken(token: String) {
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_BOT_TOKEN, token).apply()
    }

    fun getChatId(): String {
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_CHAT_ID, "") ?: ""
    }

    fun setChatId(chatId: String) {
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_CHAT_ID, chatId).apply()
    }

    private fun setupGlobalCrashHandler() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "CRITICAL: Uncaught crash captured inside Telemetry Engine", throwable)
            
            // Synchronously construct and queue the critical crash dump
            val crashReport = generateReportJson(
                level = "CRITICAL",
                feature = "GlobalCrashHandler",
                command = "app_crash",
                result = "FATAL_EXCEPTION",
                error = throwable.message ?: "Null pointer or memory crash",
                throwable = throwable
            )
            
            incrementCrashCounter()
            queueReport(crashReport, immediate = true)
            
            // Allow thread to process cleanly
            if (oldHandler != null) {
                oldHandler.uncaughtException(thread, throwable)
            } else {
                System.exit(1)
            }
        }
    }

    // Increments persistent error counter
    fun incrementErrorCounter() {
        _totalErrorsCount.value++
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putInt("err_count", _totalErrorsCount.value).apply()
    }

    // Increments persistent crash counter
    private fun incrementCrashCounter() {
        _totalCrashesCount.value++
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putInt("crash_count", _totalCrashesCount.value).apply()
    }

    fun clearStats() {
        _totalErrorsCount.value = 0
        _totalCrashesCount.value = 0
        val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putInt("err_count", 0).putInt("crash_count", 0).apply()
        unsentReportsQueue.clear()
        saveQueueToFile()
        _liveTelemetryHistory.value = emptyList()
        sentLogsDeduplicator.clear()
    }

    // Low-overhead background queue processor
    private fun startBackgroundTelemetryQueueProcessor() {
        if (batchJobsActive) return
        batchJobsActive = true
        
        coroutineScope.launch {
            while (isActive) {
                // Periodically check queue or wait for items
                if (isNetworkAvailable && unsentReportsQueue.isNotEmpty()) {
                    val report = unsentReportsQueue.peek()
                    if (report != null) {
                        val success = dispatchReportToTelegramSync(report)
                        if (success) {
                            unsentReportsQueue.poll() // Remove from queue
                            saveQueueToFile() // Update disk file
                        } else {
                            // If transmission failed (e.g. rate limit / network error / token bad), we retry after a pause
                            delay(5000)
                        }
                    }
                }
                delay(1500) // Heartbeat delay limits execution resources and saves battery
            }
        }
    }

    // Triggered publicly to trace execution logs
    fun logEvent(
        level: String,
        feature: String,
        command: String = "",
        result: String = "",
        error: String = "",
        throwable: Throwable? = null
    ) {
        // Prevent tracing spammy commands (Deduplication Layer)
        val traceKey = "$feature|$command|$error"
        if (sentLogsDeduplicator.contains(traceKey)) return
        if (sentLogsDeduplicator.size > 200) {
            sentLogsDeduplicator.clear() // Prevent memory bloat
        }
        sentLogsDeduplicator.add(traceKey)

        if (level == "ERROR" || level == "CRITICAL") {
            incrementErrorCounter()
        }

        coroutineScope.launch {
            val json = generateReportJson(level, feature, command, result, error, throwable)
            queueReport(json, immediate = (level == "CRITICAL" || level == "ERROR"))
        }
    }

    private fun queueReport(report: JSONObject, immediate: Boolean) {
        // Enqueue memory object
        unsentReportsQueue.add(report)
        
        // Save current history lists
        val currentLive = _liveTelemetryHistory.value.toMutableList()
        currentLive.add(0, report)
        if (currentLive.size > 100) currentLive.removeAt(currentLive.size - 1)
        _liveTelemetryHistory.value = currentLive
        
        saveQueueToFile()
        
        // Execute immediately if requested and network is online
        if (immediate && isNetworkAvailable) {
            coroutineScope.launch {
                val head = unsentReportsQueue.peek()
                if (head != null) {
                    val success = dispatchReportToTelegramSync(head)
                    if (success) {
                        unsentReportsQueue.poll()
                        saveQueueToFile()
                    }
                }
            }
        }
    }

    // Sync Transmission to avoid threading conflicts with HttpClient
    private fun dispatchReportToTelegramSync(report: JSONObject): Boolean {
        val botToken = getBotToken()
        val chatId = getChatId()
        
        if (botToken.isBlank() || chatId.isBlank()) {
            _telegramStatus.value = "MISSING_CONFIG"
            return false
        }
        
        var attempts = 3
        while (attempts > 0) {
            try {
                // Build a beautiful formatted message for Telegram
                val formattedText = formatTelegramMessage(report)
                
                val urlUrl = URL("https://api.telegram.org/bot$botToken/sendMessage")
                val conn = urlUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                
                val paramsBody = JSONObject().apply {
                    put("chat_id", chatId)
                    put("text", formattedText)
                    put("parse_mode", "Markdown")
                    put("disable_web_page_preview", true)
                }
                
                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(paramsBody.toString())
                    writer.flush()
                }
                
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    _telegramStatus.value = "CONNECTED_OK"
                    _lastSentReportJson.value = report.toString(2)
                    return true
                } else {
                    val errStream = conn.errorStream
                    val responseMsg = if (errStream != null) {
                        BufferedReader(InputStreamReader(errStream)).use { it.readText() }
                    } else "HTTP $responseCode"
                    Log.e(TAG, "Telegram error: $responseMsg")
                    _telegramStatus.value = "SERVER_ERROR ($responseCode)"
                    
                    // Specific handle for invalid token or deactivated bot: don't loop/retry
                    if (responseCode == 401 || responseCode == 404 || responseCode == 400) {
                        return false // Immediately abandon invalid configs to save cycles
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed to Telegram, retrying...", e)
                _telegramStatus.value = "CONNECT_FAILED: ${e.message}"
            }
            attempts--
            if (attempts > 0) {
                Thread.sleep(1000) // Backoff delay before retry
            }
        }
        return false
    }

    fun triggerManualHeartbeat() {
        logEvent("INFO", "ManualHeartbeat", "telemetry_test", "ONLINE_PONG", "")
    }

    // Formats JSON structure into highly professional and readable Markdown report
    private fun formatTelegramMessage(obj: JSONObject): String {
        val level = obj.optString("level")
        val timestamp = obj.optString("timestamp")
        val feature = obj.optString("feature")
        val cmd = obj.optString("command")
        val res = obj.optString("result")
        val err = obj.optString("error")
        val stack = obj.optString("stackTrace")
        
        val device = obj.optJSONObject("device")
        val deviceStr = if (device != null) {
            "${device.optString("brand")} ${device.optString("model")} (${obj.optString("uiLayer")})"
        } else "Unknown"
        
        val perf = obj.optJSONObject("performance")
        val perfStr = if (perf != null) {
            "FPS: ${perf.optString("fps")} | RAM: ${perf.optString("ram")} | Temp: ${perf.optString("temperature")}"
        } else "N/A"

        val levelEmoji = when (level) {
            "CRITICAL" -> "🚨 *[CRITICAL BOOSTER CRASH]*"
            "ERROR" -> "❌ *[BOOSTER ERROR]*"
            "WARNING" -> "⚠️ *[BOOSTER WARNING]*"
            else -> "ℹ️ *[BOOSTER INFO]*"
        }

        val sb = StringBuilder()
        sb.append("$levelEmoji\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📱 *Device:* `$deviceStr`\n")
        sb.append("⏰ *Time:* `$timestamp`\n")
        sb.append("🧩 *Comp:* `$feature`\n")
        sb.append("📊 *Status:* $perfStr\n")
        
        if (cmd.isNotBlank()) {
            sb.append("\n⚙️ *Command:* `$cmd`\n")
        }
        if (res.isNotBlank()) {
            sb.append("📥 *Result:* `${res.take(300)}`\n")
        }
        if (err.isNotBlank()) {
            sb.append("⚠️ *Error:* `$err`\n")
        }
        if (stack.isNotBlank()) {
            sb.append("\n📚 *Stacktrace:*\n```\n${stack.take(500)}\n```\n")
        }
        
        return sb.toString()
    }

    // JSON Schema Factory
    private fun generateReportJson(
        level: String,
        feature: String,
        command: String,
        result: String,
        error: String,
        throwable: Throwable?
    ): JSONObject {
        val root = JSONObject()
        root.put("level", level)
        
        // Device Node
        val deviceNode = JSONObject()
        deviceNode.put("manufacturer", Build.MANUFACTURER)
        deviceNode.put("model", Build.MODEL)
        deviceNode.put("brand", Build.BRAND)
        root.put("device", deviceNode)
        
        // Android Node
        val androidNode = JSONObject()
        androidNode.put("version", Build.VERSION.RELEASE)
        androidNode.put("apiLevel", Build.VERSION.SDK_INT.toString())
        androidNode.put("buildFingerprint", Build.FINGERPRINT)
        root.put("android", androidNode)
        
        root.put("uiLayer", getUiLayerName())
        
        // App Node
        val appNode = JSONObject()
        val pkgInfo = try {
            appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        } catch (_: Exception) {
            null
        }
        appNode.put("version", pkgInfo?.versionName ?: "1.0.0")
        appNode.put("buildCode", pkgInfo?.versionCode?.toString() ?: "1")
        root.put("app", appNode)
        
        // Performance Node
        val perfNode = JSONObject()
        perfNode.put("fps", currentLiveFps.toString())
        val (totalRam, usedRam, availRam) = DeviceStats.getRamInfoMb(appContext)
        perfNode.put("ram", "${availRam}MB Free / ${totalRam}MB")
        perfNode.put("cpu", "N/A")
        val cpuTemp = DeviceStats.getCpuTempCelsius()
        perfNode.put("temperature", if (cpuTemp > 0) String.format(Locale.US, "%.1f°C", cpuTemp) else "N/A")
        root.put("performance", perfNode)
        
        root.put("feature", feature)
        root.put("command", command)
        root.put("result", result)
        root.put("error", error)
        
        if (throwable != null) {
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            throwable.printStackTrace(pw)
            root.put("stackTrace", sw.toString())
        } else {
            root.put("stackTrace", "")
        }
        
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        df.timeZone = TimeZone.getDefault()
        root.put("timestamp", df.format(Date()))
        
        return root
    }

    private fun getUiLayerName(): String {
        return try {
            val brand = Build.BRAND.lowercase()
            val manufacturer = Build.MANUFACTURER.lowercase()
            when {
                brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> {
                    // HyperOS vs MIUI detection
                    val hasMiuiVer = System.getProperty("ro.miui.ui.version.name")?.lowercase()
                    if (hasMiuiVer != null && (hasMiuiVer.contains("v8") || hasMiuiVer.contains("ux"))) {
                        "HyperOS"
                    } else {
                        "MIUI"
                    }
                }
                brand.contains("samsung") || manufacturer.contains("samsung") -> "OneUI"
                brand.contains("oneplus") || brand.contains("oppo") -> "ColorOS"
                brand.contains("realme") -> "realme UI"
                brand.contains("vivo") -> "Funtouch OS"
                brand.contains("huawei") || brand.contains("honor") -> "MagicOS"
                else -> "Stock UI"
            }
        } catch (_: Exception) {
            "Stock Android"
        }
    }

    // Disk offline storage serialization
    private fun saveQueueToFile() {
        synchronized(this) {
            try {
                val array = JSONArray()
                for (item in unsentReportsQueue) {
                    array.put(item)
                }
                val file = File(appContext.filesDir, QUEUE_FILENAME)
                file.writeText(array.toString())
                _queuedReportsCount.value = unsentReportsQueue.size
            } catch (e: Exception) {
                Log.e(TAG, "Failed serialization to disk queue file", e)
            }
        }
    }

    private fun loadQueueFromFile() {
        synchronized(this) {
            try {
                val file = File(appContext.filesDir, QUEUE_FILENAME)
                if (file.exists()) {
                    val raw = file.readText()
                    if (raw.isNotBlank()) {
                        val array = JSONArray(raw)
                        unsentReportsQueue.clear()
                        for (i in 0 until array.length()) {
                            unsentReportsQueue.add(array.getJSONObject(i))
                        }
                        _queuedReportsCount.value = unsentReportsQueue.size
                        Log.d(TAG, "Loaded ${_queuedReportsCount.value} pending reports from local queue file.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading disk queue, rebuilding", e)
            }
        }
    }

    private fun setupNetworkCallback() {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Read initial state
        val info = cm.activeNetworkInfo
        isNetworkAvailable = info != null && info.isConnected
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
                Log.d(TAG, "Network restored: active transmission queue resumed")
                // Re-process queue immediately
                coroutineScope.launch {
                    if (unsentReportsQueue.isNotEmpty()) {
                        val head = unsentReportsQueue.peek()
                        if (head != null) {
                            val success = dispatchReportToTelegramSync(head)
                            if (success) {
                                unsentReportsQueue.poll()
                                saveQueueToFile()
                            }
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
                Log.d(TAG, "Network disconnected: batch buffers holding locally")
            }
        })
    }
}
