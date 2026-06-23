package com.aistudio.pubgbooster.fxghqz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.pubgbooster.fxghqz.data.NetworkRepository
import com.aistudio.pubgbooster.fxghqz.data.NetworkRepositoryImpl
import com.aistudio.pubgbooster.fxghqz.util.AdbCommands
import com.aistudio.pubgbooster.fxghqz.util.PingMonitor
import com.aistudio.pubgbooster.fxghqz.util.PingStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class WifiBandPreference(val labelEn: String, val labelAr: String) {
    BAND_24_ONLY("2.4GHz Only", "تردد 2.4GHz فقط"),
    BAND_5_ONLY("5GHz Only", "تردد 5GHz فقط"),
    BAND_AUTO("Auto / Both", "تلقائي / كلاهما")
}

data class OptimizationImpact(
    val jitterBefore: Int,
    val jitterAfter: Int,
    val improvementPercent: Int
)

data class NetworkUiState(
    val bandPreference: WifiBandPreference = WifiBandPreference.BAND_AUTO,
    val currentConnectedBand: String = "Unknown",
    val nearbyCongestionCount: Int = -1,
    val bandMismatchWarning: Boolean = false,
    val verificationResults: Map<String, Boolean> = emptyMap(),
    val isVerifying: Boolean = false,
    val optimizationImpact: OptimizationImpact? = null,
    val deviceWarnings: List<String> = emptyList(),
    val isXiaomiDevice: Boolean = false,
    val bluetoothOffEnabled: Boolean = false,
    val isOptimizing: Boolean = false,
    val lastOptimizedCommandCount: Int = 0,
    val lastOptimizedFailedNames: List<String> = emptyList()
)

class NetworkViewModel(
    private val repository: NetworkRepository = NetworkRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    init {
        runPreflightCheck()
    }

    fun detectCurrentBand(context: Context) {
        val band = repository.detectCurrentBand(context)
        val mismatch = when (_state.value.bandPreference) {
            WifiBandPreference.BAND_24_ONLY -> band == "5GHz" || band == "6GHz"
            WifiBandPreference.BAND_5_ONLY -> band == "2.4GHz"
            WifiBandPreference.BAND_AUTO -> false
        }
        _state.update { it.copy(currentConnectedBand = band, bandMismatchWarning = mismatch) }
    }

    fun scanBandCongestion(context: Context) {
        val preferenceString = _state.value.bandPreference.name
        val count = repository.scanBandCongestion(context, preferenceString)
        _state.update { it.copy(nearbyCongestionCount = count) }
    }

    fun setBandPreference(pref: WifiBandPreference, context: Context) {
        _state.update { it.copy(bandPreference = pref) }
        detectCurrentBand(context)
        scanBandCongestion(context)
    }

    fun toggleBluetoothOffEnabled(enabled: Boolean) {
        _state.update { it.copy(bluetoothOffEnabled = enabled) }
    }

    fun runPreflightCheck() {
        val isXiaomi = repository.isXiaomiDevice()
        val warnings = repository.getPreflightWarnings()
        _state.update { it.copy(isXiaomiDevice = isXiaomi, deviceWarnings = warnings) }
    }

    fun optimizeNetwork() {
        viewModelScope.launch {
            _state.update { it.copy(isOptimizing = true, verificationResults = emptyMap()) }

            if (!repository.isAdbAvailable()) {
                _state.update { it.copy(isOptimizing = false, lastOptimizedCommandCount = 0) }
                return@launch
            }

            // قياس الذبذبة قبل التحسين من خلال الـ Repository
            val jitterBefore = repository.getJitterMetric()

            var succeeded = 0
            val failed = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                val orderedIds = listOf(
                    "packet_loss_fix",
                    "wifi_power_save_off",
                    "wifi_sleep_policy_never",
                    "limit_concurrent_radio_load",
                    "tcp_retransmit_reduce",
                    "mobile_data_priority",
                    "reduce_emi_bt_off",
                    "apn_keepalive",
                    "dns_fast_resolver",
                    "stop_wifi_scan"
                )

                orderedIds.forEach { id ->
                    AdbCommands.byId(id)?.let { cmd ->
                        if (id == "reduce_emi_bt_off" && !_state.value.bluetoothOffEnabled) {
                            return@let
                        }
                        val result = repository.runAdbCommand(cmd.command)
                        if (result.success) succeeded++ else failed.add(cmd.nameEn)
                    }
                    delay(80)
                }
            }

            _state.update {
                it.copy(
                    isOptimizing = false,
                    lastOptimizedCommandCount = succeeded,
                    lastOptimizedFailedNames = failed
                )
            }

            // ── تحقق فعلي ──
            verifyOptimizationApplied()

            // ── قياس التأثير بعد ثانيتين
            delay(2000)
            measureOptimizationImpact(jitterBefore)
        }
    }

    private fun measureOptimizationImpact(jitterBefore: Int) {
        val jitterAfter = try {
            val pingList = PingMonitor.middleEastStats.value.history
            if (pingList.size > 1) {
                val diffs = pingList.zipWithNext { a, b -> Math.abs(a - b) }
                diffs.average().toInt()
            } else {
                3
            }
        } catch (e: Exception) {
            3
        }
        val improvement = if (jitterBefore > 0) {
            (((jitterBefore - jitterAfter).toFloat() / jitterBefore) * 100).toInt().coerceAtLeast(0)
        } else {
            0
        }

        _state.update {
            it.copy(optimizationImpact = OptimizationImpact(jitterBefore, jitterAfter, improvement))
        }
    }

    suspend fun verifyOptimizationApplied() {
        _state.update { it.copy(isVerifying = true) }
        val results = withContext(Dispatchers.IO) {
            mapOf(
                "Wi-Fi Power Save Off" to repository.verifySettingApplied("wifi_power_save", "0"),
                "Captive Portal Off" to repository.verifySettingApplied("captive_portal_detection_enabled", "0"),
                "TCP Window Boosted" to repository.verifySettingApplied("tcp_default_init_rwnd", "60"),
                "Wi-Fi Sleep Never" to repository.verifySettingApplied("wifi_sleep_policy", "2"),
                "BLE Scan Off" to repository.verifySettingApplied("ble_scan_always_enabled", "0")
            )
        }
        _state.update { it.copy(verificationResults = results, isVerifying = false) }
    }

    fun restoreNetworkDefaults() {
        viewModelScope.launch {
            _state.update { it.copy(isOptimizing = true) }
            withContext(Dispatchers.IO) {
                AdbCommands.list.forEach { cmd ->
                    if (cmd.restore.isNotBlank()) {
                        repository.runAdbCommand(cmd.restore)
                        delay(60)
                    }
                }
            }
            _state.update {
                it.copy(
                    isOptimizing = false,
                    lastOptimizedCommandCount = 0,
                    verificationResults = emptyMap(),
                    optimizationImpact = null
                )
            }
        }
    }
}
