package com.aistudio.pubgbooster.fxghqz.data

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.aistudio.pubgbooster.fxghqz.util.AdbCommandRunner
import com.aistudio.pubgbooster.fxghqz.util.PingMonitor
import com.aistudio.pubgbooster.fxghqz.util.PingStats
import com.aistudio.pubgbooster.fxghqz.util.ShizukuCommandRunner

interface NetworkRepository {
    fun detectCurrentBand(context: Context): String
    fun scanBandCongestion(context: Context, bandPreference: String): Int
    suspend fun verifySettingApplied(settingKey: String, expectedValue: String): Boolean
    suspend fun isAdbAvailable(): Boolean
    suspend fun runAdbCommand(command: String): ShizukuCommandRunner.CommandResult
    fun getJitterMetric(): Int
    fun getPreflightWarnings(): List<String>
    fun isXiaomiDevice(): Boolean
}

class NetworkRepositoryImpl : NetworkRepository {

    override fun detectCurrentBand(context: Context): String {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val frequency = wifiManager.connectionInfo?.frequency ?: 0
            when {
                frequency in 2400..2500 -> "2.4GHz"
                frequency in 4900..5900 -> "5GHz"
                frequency in 5925..7125 -> "6GHz"
                else -> "Unknown"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    override fun scanBandCongestion(context: Context, bandPreference: String): Int {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val scanResults = wifiManager.scanResults ?: emptyList()
            when (bandPreference) {
                "BAND_24_ONLY" -> scanResults.count { it.frequency in 2400..2500 }
                "BAND_5_ONLY" -> scanResults.count { it.frequency in 4900..5900 }
                else -> scanResults.size
            }
        } catch (_: Exception) {
            -1
        }
    }

    override suspend fun verifySettingApplied(settingKey: String, expectedValue: String): Boolean {
        val result = AdbCommandRunner.runDetailed("settings get global $settingKey")
        return result.success && result.output.trim() == expectedValue
    }

    override suspend fun isAdbAvailable(): Boolean {
        return AdbCommandRunner.isAvailable()
    }

    override suspend fun runAdbCommand(command: String): ShizukuCommandRunner.CommandResult {
        return AdbCommandRunner.runDetailed(command)
    }

    override fun getJitterMetric(): Int {
        return try {
            val pingList = PingMonitor.middleEastStats.value.history
            if (pingList.size > 1) {
                val diffs = pingList.zipWithNext { a, b -> Math.abs(a - b) }
                diffs.average().toInt()
            } else {
                12 // Default
            }
        } catch (e: Exception) {
            12
        }
    }

    override fun isXiaomiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                brand.contains("redmi") || brand.contains("poco")
    }

    override fun getPreflightWarnings(): List<String> {
        val warnings = mutableListOf<String>()
        if (isXiaomiDevice()) {
            warnings.add("📱 Xiaomi/Redmi/POCO (MIUI/HyperOS) detected.")
            warnings.add("⚠️ Enable Autostart for this app in Settings > Apps > Permissions.")
            warnings.add("⚠️ Set Battery Saver to \"No restrictions\" for this app to prevent background services being killed.")
        }
        return warnings
    }
}
