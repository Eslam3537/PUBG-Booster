package com.aistudio.pubgbooster.fxghqz.util

import android.content.Context

object CommandExecutor {
    private var context: Context? = null
    private val executedCommands = mutableListOf<String>()

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun clearExecutedCommands() {
        executedCommands.clear()
    }

    fun getExecutedCommands(): List<String> {
        return executedCommands
    }

    fun execute(command: String, phase: String, pkgName: String, timeoutMs: Long = 10000L): CommandResult {
        executedCommands.add(command)
        val startTime = System.currentTimeMillis()
        val result = ShizukuCommandRunner.runCommand(command)
        val endTime = System.currentTimeMillis()
        return CommandResult(
            command = command,
            success = result.success,
            output = result.output,
            error = result.error,
            timeMs = endTime - startTime,
            deviceCompatible = true
        )
    }

    fun execute(command: String, pkgName: String): CommandResult {
        return execute(command, "Execution", pkgName)
    }
}
