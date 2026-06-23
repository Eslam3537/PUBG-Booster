package com.aistudio.pubgbooster.fxghqz.util

data class AdbCommandInfo(
    val id: String,
    val nameEn: String,
    val command: String,
    val restore: String
)

object AdbCommands {
    val list = listOf(
        AdbCommandInfo(
            id = "limit_concurrent_radio_load",
            nameEn = "Limit Concurrent Radio Load",
            command = "settings put global radio_bug_reporting_enabled 0",
            restore = "settings put global radio_bug_reporting_enabled 1"
        ),
        AdbCommandInfo(
            id = "tcp_retransmit_reduce",
            nameEn = "Lower TCP Retransmissions Delay",
            command = "settings put global tcp_default_init_rwnd 60",
            restore = "settings put global tcp_default_init_rwnd 10"
        ),
        AdbCommandInfo(
            id = "mobile_data_priority",
            nameEn = "Raise Cellular Link Scheduler Priority",
            command = "settings put global mobile_data_always_on 1",
            restore = "settings put global mobile_data_always_on 0"
        ),
        AdbCommandInfo(
            id = "reduce_emi_bt_off",
            nameEn = "Mute Bluetooth Coexistence EMI Scan",
            command = "settings put global ble_scan_always_enabled 0",
            restore = "settings put global ble_scan_always_enabled 1"
        ),
        AdbCommandInfo(
            id = "apn_keepalive",
            nameEn = "Optimize LTE Keepalive Packet Polling",
            command = "settings put global gprs_connection_setting 2",
            restore = "settings put global gprs_connection_setting 0"
        ),
        AdbCommandInfo(
            id = "dns_fast_resolver",
            nameEn = "Enable High Speed DNS Cache Resolvers",
            command = "settings put global private_dns_mode hostname",
            restore = "settings put global private_dns_mode automatic"
        ),
        AdbCommandInfo(
            id = "stop_wifi_scan",
            nameEn = "Hush WiFi Scan Probe Flooding",
            command = "settings put global wifi_scan_throttle_enabled 1",
            restore = "settings put global wifi_scan_throttle_enabled 0"
        )
    )

    fun byId(id: String): AdbCommandInfo? = list.find { it.id == id }
}
