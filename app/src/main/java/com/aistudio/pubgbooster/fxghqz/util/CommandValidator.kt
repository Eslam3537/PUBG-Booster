package com.aistudio.pubgbooster.fxghqz.util

object CommandValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val correctedCommand: String,
        val errorMessageEn: String? = null,
        val errorMessageAr: String? = null
    )

    fun validateAndCorrect(command: String): ValidationResult {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                correctedCommand = "",
                errorMessageEn = "Command cannot be empty",
                errorMessageAr = "الكود لا يمكن أن يكون فارغاً"
            )
        }

        // Clean command from "adb shell " prefix if exists
        var cleaned = trimmed
        if (cleaned.startsWith("adb shell ", ignoreCase = true)) {
            cleaned = cleaned.substring("adb shell ".length).trim()
        } else if (cleaned.startsWith("adb ", ignoreCase = true)) {
            cleaned = cleaned.substring("adb ".length).trim()
        }

        // Malicious/unsupported actions safety guard
        val lower = cleaned.lowercase()
        if (lower.contains("rm ") || lower.contains("format ") || lower.contains("; shutdown") || lower.contains("reboot")) {
            return ValidationResult(
                isValid = false,
                correctedCommand = cleaned,
                errorMessageEn = "Destructive system actions are blocked for device safety.",
                errorMessageAr = "أوامر حذف أو تهيئة النظام محظورة لسلامة جهازك."
            )
        }

        // Only allow certain prefixes
        val allowedPrefixes = listOf("settings", "dumpsys", "am", "pm", "setprop", "getprop", "cmd", "echo", "stop", "start", "mkdir")
        val words = cleaned.split("\\s+".toRegex())
        val firstWord = words.firstOrNull()?.lowercase() ?: ""

        if (firstWord !in allowedPrefixes) {
            return ValidationResult(
                isValid = false,
                correctedCommand = cleaned,
                errorMessageEn = "Only standard ADB settings commands (settings, setprop, am, pm, dumpsys) are allowed.",
                errorMessageAr = "مسموح فقط بأوامر نظام ADB القياسية (settings, setprop, am, pm, dumpsys)."
            )
        }

        return ValidationResult(
            isValid = true,
            correctedCommand = cleaned
        )
    }
}
