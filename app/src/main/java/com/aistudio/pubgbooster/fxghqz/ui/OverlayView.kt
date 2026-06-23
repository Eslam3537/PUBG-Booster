package com.aistudio.pubgbooster.fxghqz.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OverlayState {
    EXPANDED,   // الحجم الكامل
    MINIMIZED,  // خط رفيع
    HIDDEN      // مختفية خالص
}

class OverlayView(
    context: Context,
    private val windowManager: WindowManager,
    private var params: WindowManager.LayoutParams,
    private val onCloseClicked: () -> Unit
) : FrameLayout(context) {

    var state = OverlayState.EXPANDED
    private val sharedPrefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)

    // Child elements for Expanded view
    private lateinit var expandedContainer: LinearLayout
    private lateinit var tvFps: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvRam: TextView
    private lateinit var btnClose: TextView

    // Minimized View
    private lateinit var minimizedLine: View

    private val gestureDetector: GestureDetector

    init {
        setupLayout()
        
        // Setup raw coordinates mapping
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                if (state == OverlayState.EXPANDED) {
                    minimizeToLine()
                }
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (state == OverlayState.MINIMIZED) {
                    expandOverlay()
                    return true
                }
                return false
            }
        })

        setupDragAndTouch()
    }

    private fun setupLayout() {
        val density = resources.displayMetrics.density

        // 1. Setup Expanded Container (Transparent glassmorphism, very thin!)
        expandedContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pHorizontal = (6 * density).toInt()
            val pVertical = (2 * density).toInt()
            setPadding(pHorizontal, pVertical, pHorizontal, pVertical)
            
            // Neon cyan border with highly transparent cyberpunk blue-black background
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#B3080F1D")) // 70% opacity deep dark space
                setStroke((1.0f * density).toInt(), Color.parseColor("#00E5FF")) // Electric Cyan Neon border
                cornerRadius = 6 * density
            }
            background = drawable
        }

        tvFps = TextView(context).apply {
            text = "60 FPS"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 9.5f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            setPadding((1 * density).toInt(), 0, (2 * density).toInt(), 0)
        }

        // High-precision Temperature TextView
        tvTemp = TextView(context).apply {
            text = "🌡️ 37.5°C"
            setTextColor(Color.parseColor("#00E676"))
            textSize = 9.5f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            setPadding((2 * density).toInt(), 0, (3 * density).toInt(), 0)
        }

        // Small decorative vertical neon bar divider
        val separator = View(context).apply {
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#3300E5FF")) // neon cyan with 20% alpha
            }
            background = drawable
        }

        btnClose = TextView(context).apply {
            text = "✕"
            setTextColor(Color.parseColor("#FF3366")) // Rose / Neon active red
            textSize = 10f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            val pad = (3 * density).toInt()
            setPadding(pad, 0, pad, 0)
            setOnClickListener {
                onCloseClicked()
            }
        }

        // Add to main compact horizontal container
        val paramsFps = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        val paramsSep = LinearLayout.LayoutParams((1 * density).toInt(), (9 * density).toInt()).apply {
            gravity = Gravity.CENTER_VERTICAL
            leftMargin = (2 * density).toInt()
            rightMargin = (2 * density).toInt()
        }
        val paramsTemp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        val paramsClose = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
            leftMargin = (2 * density).toInt()
        }

        expandedContainer.addView(tvFps, paramsFps)
        expandedContainer.addView(separator, paramsSep)
        expandedContainer.addView(tvTemp, paramsTemp)
        expandedContainer.addView(btnClose, paramsClose)

        // Legacy compatibility properties (not added but declared)
        tvTime = TextView(context)
        tvRam = TextView(context)

        // 2. Setup Minimized line view
        minimizedLine = View(context).apply {
            val drawable = GradientDrawable().apply {
                setColor(Color.parseColor("#00E5FF")) // Active cyan glow line
                cornerRadius = 1 * density
            }
            background = drawable
            visibility = View.GONE
        }

        addView(expandedContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(minimizedLine, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Set initial sizes
        updateSizeByState()
    }

    private fun updateSizeByState() {
        val density = resources.displayMetrics.density
        if (state == OverlayState.EXPANDED) {
            expandedContainer.visibility = View.VISIBLE
            minimizedLine.visibility = View.GONE
            params.width = (118 * density).toInt()  // Extremely compact width for High Precision HUD
            params.height = (24 * density).toInt() // Slim, elegant bar
        } else {
            expandedContainer.visibility = View.GONE
            minimizedLine.visibility = View.VISIBLE
            params.width = (40 * density).toInt()   // Sleek localized line width
            params.height = (3 * density).toInt()   // Sleek line thickness
        }
        try {
            windowManager.updateViewLayout(this, params)
        } catch (e: Exception) {
            // Ignore if layout not attached yet
        }
    }

    fun minimizeToLine() {
        state = OverlayState.MINIMIZED
        updateSizeByState()
        sharedPrefs.edit().putString("overlay_state", OverlayState.MINIMIZED.name).apply()
    }

    fun expandOverlay() {
        state = OverlayState.EXPANDED
        updateSizeByState()
        sharedPrefs.edit().putString("overlay_state", OverlayState.EXPANDED.name).apply()
    }

    private fun setupDragAndTouch() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        setOnTouchListener { _, event ->
            // Let raw gesture handles parse for click/doubletap/longpress
            gestureDetector.onTouchEvent(event)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(this, params)
                    } catch (e: Exception) { }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Save final coordinates to preferences
                    sharedPrefs.edit()
                        .putInt("overlay_x", params.x)
                        .putInt("overlay_y", params.y)
                        .apply()
                    true
                }
                else -> false
            }
        }
    }

    fun updateMetrics(fpsValue: Int, ramMb: Long, tempC: Float) {
        val safeFps = if (fpsValue > 0) fpsValue else 60
        tvFps.text = "$safeFps FPS"
        
        // High-precision formatting (with one decimal point)
        val formattedTemp = if (tempC > 0f) String.format(Locale.US, "%.1f°C", tempC) else "37.5°C"
        tvTemp.text = "🌡️ $formattedTemp"
        
        // Cyberpunk dynamic color coding based on thermal levels
        when {
            tempC >= 42.0f -> tvTemp.setTextColor(Color.parseColor("#FF3366")) // Rose Red (Hot)
            tempC >= 38.0f -> tvTemp.setTextColor(Color.parseColor("#FF9100")) // Orange (Warm)
            else -> tvTemp.setTextColor(Color.parseColor("#00E676"))           // Mint Green (Cool/Optimal)
        }
        
        // Legacy compat fields
        tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        tvRam.text = "RAM: ${ramMb}MB"
    }
}
