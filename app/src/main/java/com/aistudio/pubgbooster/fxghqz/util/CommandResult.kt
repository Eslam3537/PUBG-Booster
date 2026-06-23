package com.aistudio.pubgbooster.fxghqz.util

data class CommandResult(
    val command: String,
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val timeMs: Long = 0L,
    val deviceCompatible: Boolean = true
)
