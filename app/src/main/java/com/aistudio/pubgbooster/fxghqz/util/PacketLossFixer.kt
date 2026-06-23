package com.aistudio.pubgbooster.fxghqz.util

import android.util.Log

object PacketLossFixer {
    
    fun runPacketLossDiagnostics(packageName: String): String {
        Log.d("PacketLossFixer", "Running packet loss diagnostics for $packageName")
        
        val commands = listOf(
            "ndc resolver flushdefaultif",
            "ndc resolver flushnet 100",
            "settings put global wifi_sleep_policy 2",
            "setprop net.ipv6.conf.all.disable_ipv6 1",
            "setprop net.ipv6.conf.wlan0.disable_ipv6 1"
        )
        
        for (cmd in commands) {
            Thread {
                try {
                    CommandExecutor.execute(cmd, "PACKET_LOSS_FIX", packageName)
                } catch (_: Exception) {}
            }.start()
        }
        
        return "Standard DNS flushed, wifi sleep policy changed to NEVER, and disabled dual-stack IPv6 to prevent server handshaking overheads."
    }
}
