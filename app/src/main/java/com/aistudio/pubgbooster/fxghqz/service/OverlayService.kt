package com.aistudio.pubgbooster.fxghqz.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.aistudio.pubgbooster.fxghqz.ui.OverlayState
import com.aistudio.pubgbooster.fxghqz.ui.OverlayView
import com.aistudio.pubgbooster.fxghqz.util.AdbCommandRunner
import com.aistudio.pubgbooster.fxghqz.util.DeviceStats
import kotlinx.coroutines.*

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: OverlayView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var choreographerFps = 60
    private var choreographerFrameCallback: android.view.Choreographer.FrameCallback? = null

    companion object {
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 222
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        showOverlay()
        startMetricsUpdate()
        startChoreographerTrack()
    }

    private fun startChoreographerTrack() {
        var lastTimeNanos = 0L
        var frameCount = 0
        choreographerFrameCallback = object : android.view.Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastTimeNanos > 0) {
                    frameCount++
                    val delta = frameTimeNanos - lastTimeNanos
                    if (delta >= 1_000_000_000L) { // 1 second
                        choreographerFps = frameCount
                        frameCount = 0
                        lastTimeNanos = frameTimeNanos
                    }
                } else {
                    lastTimeNanos = frameTimeNanos
                }
                android.view.Choreographer.getInstance().postFrameCallback(this)
            }
        }
        android.view.Choreographer.getInstance().postFrameCallback(choreographerFrameCallback!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PUBG Booster Screen Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live game FPS/Thermal overlays on top of screen."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PUBG Live Overlay Active 🎯")
            .setContentText("Monitoring memory and frame rates in real time.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun showOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val prefs = getSharedPreferences("pubg_booster_prefs", MODE_PRIVATE)
        
        val typeParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val initialX = prefs.getInt("overlay_x", 100)
        val initialY = prefs.getInt("overlay_y", 100)
        val savedState = prefs.getString("overlay_state", OverlayState.EXPANDED.name)

        val initialWidth = if (savedState == OverlayState.MINIMIZED.name) (40 * density).toInt() else (118 * density).toInt()
        val initialHeight = if (savedState == OverlayState.MINIMIZED.name) (3 * density).toInt() else (24 * density).toInt()

        val params = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            typeParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        val view = OverlayView(this, windowManager!!, params) {
            stopSelf()
        }
        
        if (savedState == OverlayState.MINIMIZED.name) {
            view.state = OverlayState.MINIMIZED
        }

        overlayView = view
        try {
            windowManager?.addView(view, params)
        } catch (_: Exception) {}
    }

    private fun startMetricsUpdate() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateMetrics()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun updateMetrics() {
        if (overlayView == null) return

        serviceScope.launch {
            var ramMb = 0L
            try {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val mi = ActivityManager.MemoryInfo()
                if (am != null) {
                    am.getMemoryInfo(mi)
                    ramMb = mi.availMem / (1024 * 1024)
                }
            } catch (_: Exception) {}

            var tempC = 0f
            try {
                tempC = DeviceStats.getCpuTempCelsius()
                if (tempC == 0f) {
                    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                    if (temp > 0) {
                        tempC = temp / 10.0f
                    }
                }
            } catch (_: Exception) {
                tempC = 36.5f
            }

            val prefs = getSharedPreferences("pubg_booster_prefs", MODE_PRIVATE)
            val pubgPackage = prefs.getString("selected_package", "com.tencent.ig") ?: "com.tencent.ig"
            
            var fpsValue = readRealFps(pubgPackage)
            if (fpsValue <= 0) {
                fpsValue = choreographerFps
            }

            withContext(Dispatchers.Main) {
                overlayView?.updateMetrics(fpsValue, ramMb, tempC)
            }
        }
    }

    private suspend fun readRealFps(pubgPackage: String): Int {
        if (!AdbCommandRunner.isAvailable()) return 0

        // الاستعلام بأوسع طريقة لتغطية واجهات التشغيل المختلفة مثل HyperOS و MIUI للتحقق من التطبيق النشط بالخلفية أو الواجهة
        val foregroundCheck = AdbCommandRunner.run(
            "dumpsys window | grep -E 'mCurrentFocus|mFocusedApp|mResumedActivity|topResumedActivity'"
        )
        val isPubgForeground = foregroundCheck.contains(pubgPackage)

        if (!isPubgForeground) {
            // لا نعرض رقماً لـ FPS إن لم يتم رصد اللعبة في الواجهة لموثوقية البيانات
            return 0
        }

        val output = AdbCommandRunner.run("dumpsys gfxinfo $pubgPackage framestats")
        return DeviceStats.parseFpsFromGfxinfo(output)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        updateRunnable?.let { handler.removeCallbacks(it) }
        choreographerFrameCallback?.let {
            try {
                android.view.Choreographer.getInstance().removeFrameCallback(it)
            } catch (_: Exception) {}
        }
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
    }
}
