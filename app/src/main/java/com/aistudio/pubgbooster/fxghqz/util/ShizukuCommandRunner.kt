package com.aistudio.pubgbooster.fxghqz.util

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuCommandRunner {

    data class CommandResult(
        val success: Boolean,
        val output: String,
        val error: String? = null,
        val exitCode: Int = 0
    )

    private val listeners = mutableListOf<() -> Unit>()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        notifyListeners()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        notifyListeners()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        notifyListeners()
    }

    fun registerListeners(listener: () -> Unit) {
        listeners.add(listener)
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (_: Exception) {}
    }

    fun unregisterListeners() {
        listeners.clear()
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Exception) {}
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    fun isShizukuInstalled(context: Context): Boolean {
        if (isShizukuAvailable()) return true
        val pm = context.packageManager
        for (pkg in arrayOf("moe.shizuku.privileged.api", "dev.rikka.shizuku")) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {}
        }
        return false
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            if (Shizuku.isPreV11()) {
                true
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Exception) {
            false
        }
    }

    fun requestPermission() {
        try {
            if (isShizukuAvailable() && !hasPermission()) {
                Shizuku.requestPermission(1337)
            }
        } catch (_: Exception) {}
    }

    fun runCommand(command: String): CommandResult {
        val result = if (!isShizukuAvailable() || !hasPermission()) {
            executeLocal(command)
        } else {
            try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                newProcessMethod.isAccessible = true
                val process = newProcessMethod.invoke(null, arrayOf("/system/bin/sh", "-c", command), null, null) as java.lang.Process
                val outputReader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                
                val output = outputReader.use { it.readText() }
                val error = errorReader.use { it.readText() }
                val exitCode = process.waitFor()
                
                CommandResult(
                    success = exitCode == 0,
                    output = output,
                    error = error.ifBlank { null },
                    exitCode = exitCode
                )
            } catch (e: Exception) {
                executeLocal(command)
            }
        }

        // Log execution stats to the Telemetry Engine
        if (result.success) {
            com.aistudio.pubgbooster.fxghqz.telemetry.TelemetrySystem.logEvent(
                level = "INFO",
                feature = "ShizukuCommand",
                command = command,
                result = result.output
            )
        } else {
            com.aistudio.pubgbooster.fxghqz.telemetry.TelemetrySystem.logEvent(
                level = "ERROR",
                feature = "ShizukuCommand",
                command = command,
                result = result.output,
                error = result.error ?: "Command execution failed"
            )
        }

        return result
    }

    private fun executeLocal(command: String): CommandResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val error = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            CommandResult(
                success = exitCode == 0,
                output = output,
                error = error.ifBlank { null },
                exitCode = exitCode
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "",
                error = e.localizedMessage,
                exitCode = -1
            )
        }
    }
}
