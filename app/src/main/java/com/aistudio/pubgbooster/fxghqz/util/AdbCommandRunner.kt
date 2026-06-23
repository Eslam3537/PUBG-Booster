package com.aistudio.pubgbooster.fxghqz.util

object AdbCommandRunner {
    fun isAvailable(): Boolean {
        return ShizukuCommandRunner.isShizukuAvailable() && ShizukuCommandRunner.hasPermission()
    }

    fun run(command: String): String {
        return runDetailed(command).output
    }

    fun runDetailed(command: String): ShizukuCommandRunner.CommandResult {
        return ShizukuCommandRunner.runCommand(command)
    }
}
