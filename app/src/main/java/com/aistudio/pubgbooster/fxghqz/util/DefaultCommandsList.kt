package com.aistudio.pubgbooster.fxghqz.util

import com.aistudio.pubgbooster.fxghqz.data.CustomCommand

object DefaultCommandsList {
    val commands = listOf(
        CustomCommand(
            name = "Window animation speedup",
            command = "settings put global window_animation_scale 0.0",
            description = "Turns off window animation scaling completely to bypass window transition lags.",
            restoreCommand = "settings put global window_animation_scale 1.0"
        ),
        CustomCommand(
            name = "Transition animation speedup",
            command = "settings put global transition_animation_scale 0.0",
            description = "Turns off transition anim scale.",
            restoreCommand = "settings put global transition_animation_scale 1.0"
        ),
        CustomCommand(
            name = "Animator duration scale",
            command = "settings put global animator_duration_scale 0.0",
            description = "Turns off animator duration scale.",
            restoreCommand = "settings put global animator_duration_scale 1.0"
        ),
        CustomCommand(
            name = "Low power disable",
            command = "settings put global low_power 0",
            description = "Prevents general power saver from throttling CPUs.",
            restoreCommand = "settings put global low_power 1"
        ),
        CustomCommand(
            name = "App standby disable",
            command = "settings put global app_standby_enabled 0",
            description = "Disables general app standby restrictions so background services are preserved.",
            restoreCommand = "settings put global app_standby_enabled 1"
        ),
        CustomCommand(
            name = "Device Idle suspend",
            command = "dumpsys deviceidle disable",
            description = "Suspends doze mode.",
            restoreCommand = "dumpsys deviceidle enable"
        ),
        CustomCommand(
            name = "Max peak refresh rate",
            command = "settings put system peak_refresh_rate 120.0",
            description = "Forces the screen peak refresh rate to 120Hz.",
            restoreCommand = "settings put system peak_refresh_rate 60.0"
        ),
        CustomCommand(
            name = "Min screen refresh rate",
            command = "settings put system min_refresh_rate 120.0",
            description = "Forces the screen min refresh rate to 120Hz.",
            restoreCommand = "settings put system min_refresh_rate 60.0"
        ),
        CustomCommand(
            name = "DNS resolver mode",
            command = "settings put global private_dns_mode hostname",
            description = "Switches DNS resolver mode to hostname.",
            restoreCommand = "settings put global private_dns_mode automatic"
        ),
        CustomCommand(
            name = "DNS Cloudflare",
            command = "settings put global private_dns_specifier one.one.one.one",
            description = "Configures 1.1.1.1 secure Cloudflare DNS resolving.",
            restoreCommand = "settings put global private_dns_specifier \"\""
        ),
        CustomCommand(
            name = "WiFi scan throttle disable",
            command = "settings put global wifi_scan_throttle_enabled 0",
            description = "Disables WiFi scan throttling.",
            restoreCommand = "settings put global wifi_scan_throttle_enabled 1"
        ),
        CustomCommand(
            name = "Game powersave disable",
            command = "settings put global game_powersave_mode 0",
            description = "Disables aggressive power reductions for games.",
            restoreCommand = "settings put global game_powersave_mode 1"
        ),
        CustomCommand(
            name = "TCP high buffer rx",
            command = "settings put global tcp_default_init_rwnd 60",
            description = "Raises TCP default receiving window buffers.",
            restoreCommand = "settings put global tcp_default_init_rwnd 10"
        ),
        CustomCommand(
            name = "Auto Sync disable",
            command = "settings put global auto_sync_enabled 0",
            description = "Halts account sync tasks in background.",
            restoreCommand = "settings put global auto_sync_enabled 1"
        ),
        CustomCommand(
            name = "WiFi sleep policy",
            command = "settings put global wifi_sleep_policy 2",
            description = "Keeps WiFi active and awake always.",
            restoreCommand = "settings put global wifi_sleep_policy 0"
        ),
        CustomCommand(
            name = "WiFi auto join",
            command = "settings put global wifi_enhanced_auto_join 1",
            description = "Allows smart WiFi auto join helper.",
            restoreCommand = "settings put global wifi_enhanced_auto_join 0"
        ),
        CustomCommand(
            name = "Screen off timeout",
            command = "settings put system screen_off_timeout 2147483647",
            description = "Holds screen awake indefinitely while gaming.",
            restoreCommand = "settings put system screen_off_timeout 60000"
        ),
        CustomCommand(
            name = "Pointer speed maximum",
            command = "settings put system pointer_speed 7",
            description = "Increases pointer movement velocity.",
            restoreCommand = "settings put system pointer_speed 0"
        ),
        CustomCommand(
            name = "Touch high sensitivity",
            command = "settings put system touch_sensitivity 1",
            description = "Forces touch panel to capture actions at maximum frequency.",
            restoreCommand = "settings put system touch_sensitivity 0"
        ),
        CustomCommand(
            name = "Force compact-all RAM",
            command = "am compact {package} full",
            description = "Compacts background cached processes memory heaps.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Purge Cache Caches",
            command = "pm trim-caches 2147483647",
            description = "Purges cache indexes to lower memory limits.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Thermal override limit",
            command = "settings put global restricted_device_performance 0",
            description = "Forces device performance override.",
            restoreCommand = "settings put global restricted_device_performance 1"
        ),
        CustomCommand(
            name = "Ambient thermal peak cutoff",
            command = "settings put global ambient_thermal_limit 99",
            description = "Moves ambient thermal cutoff to highest hardware parameters.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Hardware UI acceleration",
            command = "settings put global hw_quickpower 1",
            description = "Turns on general hardware visual engines acceleration.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "CPU performance governor",
            command = "settings put system sys_use_fifo_ui 1",
            description = "Allows high-priority scheduling loops for graphics rendering UI processes.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Bypass game throttle list",
            command = "settings put global restricted_device_performance 0",
            description = "Disables background throttling restrictions.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "GPRS LTE speedup link",
            command = "settings put global gprs_connection_setting 2",
            description = "Adjusts LTE/GPRS parameters for speed optimization.",
            restoreCommand = "settings put global gprs_connection_setting 0"
        ),
        CustomCommand(
            name = "Hush bluetooth scanning",
            command = "settings put global ble_scan_always_enabled 0",
            description = "Stops constant Bluetooth Low Energy scanning background loops.",
            restoreCommand = "settings put global ble_scan_always_enabled 1"
        ),
        CustomCommand(
            name = "Network speedup queues",
            command = "settings put global wifi_tcp_buffer_sizes 4096,87380,1048576,4096,16384,262144",
            description = "Optimizes TCP window buffer limits for premium speed.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Lock task high memory limits",
            command = "device_config put activity_manager max_cached_processes 32",
            description = "Configures optimized limits for bg memory apps.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Extreme rendering speed engine",
            command = "setprop debug.sf.showfps 0",
            description = "Ensures correct SurfaceFlinger parameters.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Low Jitter network packets",
            command = "settings put global private_dns_mode hostname",
            description = "Sets default fallback DNS parameters.",
            restoreCommand = ""
        ),
        CustomCommand(
            name = "Purge and kill all tasks",
            command = "am kill-all",
            description = "Terminates unnecessary back applications.",
            restoreCommand = ""
        )
    )
}
