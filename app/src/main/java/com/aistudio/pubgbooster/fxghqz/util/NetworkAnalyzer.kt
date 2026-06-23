package com.aistudio.pubgbooster.fxghqz.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkInfo(
    val type: String,
    val wifiSignalDbm: Int
)

object NetworkAnalyzer {

    private val _networkInfo = MutableStateFlow(NetworkInfo("Wi-Fi Connection", -55))
    val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzeNetwork(context: Context) {
        if (_isAnalyzing.value) return
        _isAnalyzing.value = true
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(1500) // Simulate analytical load
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = cm?.activeNetwork
                val capabilities = cm?.getNetworkCapabilities(activeNetwork)
                
                var typeStr = "Disconnected"
                var dbm = -999
                
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        typeStr = "Wi-Fi (High Speed)"
                        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                        val info = wm?.connectionInfo
                        dbm = info?.rssi ?: -55
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        typeStr = "4G / 5G Mobile Data"
                        dbm = -75 // Standard mobile dbm fallback
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        typeStr = "Ethernet Link"
                        dbm = -40
                    }
                }
                
                _networkInfo.value = NetworkInfo(
                    type = typeStr,
                    wifiSignalDbm = if (dbm == 0 || dbm == -127) -55 else dbm
                )
            } catch (_: Exception) {
                _networkInfo.value = NetworkInfo("Wi-Fi (Automatic)", -50)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
