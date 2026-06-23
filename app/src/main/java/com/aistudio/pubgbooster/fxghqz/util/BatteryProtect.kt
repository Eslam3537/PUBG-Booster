package com.aistudio.pubgbooster.fxghqz.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BatteryProtect {
    private var job: Job? = null

    fun startMonitoring(context: Context, scope: CoroutineScope) {
        if (job != null) return
        job = scope.launch {
            while (isActive) {
                delay(5000)
            }
        }
    }

    fun stopMonitoring(context: Context) {
        job?.cancel()
        job = null
    }
}
