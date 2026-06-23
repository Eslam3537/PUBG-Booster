package com.aistudio.pubgbooster.fxghqz.util

import android.app.ActivityManager
import android.content.Context
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

object DeviceStats {

    // ── RAM: حقيقي، بدون تغيير ──
    fun getRamInfoMb(context: Context): Triple<Long, Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalMb = mi.totalMem / (1024 * 1024)
        val availMb = mi.availMem / (1024 * 1024)
        val usedMb  = totalMb - availMb
        return Triple(totalMb, usedMb, availMb)
    }

    fun getRamUsedPercent(context: Context): Int {
        val (total, used, _) = getRamInfoMb(context)
        return if (total > 0) ((used.toFloat() / total) * 100).roundToInt() else 0
    }

    // ── حرارة المعالج والهاتف: ذكية وحيوية بدقة فائقة ──
    fun getCpuTempCelsius(): Float {
        // نقوم بالتحقق من جميع زونات الحرارة المتوفرة ديناميكياً (من 0 إلى 80) لضمان التوافق التام مع MediaTek Dimensity وSnapdragon
        for (i in 0..85) {
            val path = "/sys/class/thermal/thermal_zone$i/temp"
            try {
                val rawStr = BufferedReader(FileReader(path)).use { it.readLine() }?.trim() ?: continue
                val raw = rawStr.toFloatOrNull() ?: continue
                if (raw <= 0f) continue
                
                // معالجة قياسات الحرارة المختلفة حسب الشركة المصنعة للهاتف (Scale automatic parsing)
                val celsius = when {
                    raw > 10000f -> raw / 1000f       // مثال: 42500 -> 42.5°C
                    raw > 1000f -> raw / 100f         // مثال: 4250 -> 42.5°C
                    raw > 150f -> raw / 10f           // مثال: 425 -> 42.5°C
                    else -> raw                      // مثال: 42.5 -> 42.5°C
                }
                
                // نطاق حرارة المعالج المنطقي تحت الضغط بالألعاب هو بين 25°C و 85°C
                if (celsius in 25.0f..85.0f) {
                    return celsius
                }
            } catch (_: Exception) {
                // تخطي إلى الزون التالية
            }
        }
        return 0f
    }

    /**
     * تحليل ناتج "dumpsys gfxinfo <package> framestats" الحقيقي والموثّق رسمياً
     * من developer.android.com/tools/dumpsys — يعطي طوابع زمنية بدقة nanosecond
     * لآخر 120 فريم فعلي رسمتها اللعبة، بخلاف SurfaceFlinger --latency
     * الذي يتطلب اسم Surface دقيق وليس اسم حزمة.
     *
     * هيكل سطر FRAMESTATS الموثّق (مفصول بفواصل):
     * FRAMESTATS,flags,intendedVsync,vsync,oldestInputEvent,newestInputEvent,
     * handleInputStart,animationStart,performTraversalsStart,drawStart,
     * syncQueued,syncStart,issueDrawCommandsStart,swapBuffers,frameCompleted, ...
     *
     * نحسب FPS من الفرق بين قيم "FrameCompleted" المتتالية (آخر عمود موثوق التوقيت).
     */
    fun parseFpsFromGfxinfo(output: String): Int {
        if (output.isBlank() || output.startsWith("ERROR")) return 0
        return try {
            val frameLines = output.lines().filter { it.trim().startsWith("FRAMESTATS") }
            if (frameLines.size < 2) return 0

            // أول سطر FRAMESTATS هو سطر العناوين (header) — تجاهله
            val dataLines = frameLines.drop(1)
            if (dataLines.size < 2) return 0

            // العمود الأخير في كل سطر = FrameCompleted (نانوثانية)
            val completedTimestamps = dataLines.mapNotNull { line ->
                line.trim().split(",").lastOrNull()?.toLongOrNull()
            }.filter { it > 0 }

            if (completedTimestamps.size < 2) return 0

            val diffsNs = completedTimestamps.sorted().zipWithNext { a, b -> b - a }
                .filter { it in 1_000_000..200_000_000 } // فلترة قيم غير منطقية (1ms - 200ms بين الفريمات)

            if (diffsNs.isEmpty()) return 0

            val avgFrameNs = diffsNs.average()
            (1_000_000_000.0 / avgFrameNs).roundToInt().coerceIn(1, 144)
        } catch (_: Exception) {
            0
        }
    }

    /** يُبقي على الدالة القديمة كـ fallback فقط في حال فشلت framestats، لا تُستخدم كمصدر أساسي بعد الآن. */
    @Deprecated("استخدم parseFpsFromGfxinfo بدلاً منها — هذه أقل دقة وتتطلب اسم Surface دقيق")
    fun parseFpsFromSurfaceFlinger(output: String): Int {
        if (output.isBlank() || output.startsWith("ERROR")) return 0
        return try {
            val lines = output.trim().lines()
            if (lines.size < 3) return 0
            val timestamps = lines.drop(1).mapNotNull { line ->
                line.trim().split(Regex("\\s+")).getOrNull(1)?.toLongOrNull()
            }.filter { it > 0 }
            if (timestamps.size < 2) return 0
            val diffsNs = timestamps.zipWithNext { a, b -> b - a }
                .filter { it in 1_000_000..200_000_000 }
            if (diffsNs.isEmpty()) return 0
            val avgFrameNs = diffsNs.average()
            (1_000_000_000.0 / avgFrameNs).roundToInt().coerceIn(1, 144)
        } catch (_: Exception) {
            0
        }
    }
}
