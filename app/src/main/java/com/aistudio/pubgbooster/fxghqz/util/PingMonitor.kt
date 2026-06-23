package com.aistudio.pubgbooster.fxghqz.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object PingMonitor {

    private val _middleEastStats = MutableStateFlow(PingStats(15, 12, 16, 20, listOf(14, 15, 13, 16, 15, 14, 18, 15, 13, 14, 16, 15, 12, 15, 14, 15)))
    val middleEastStats: StateFlow<PingStats> = _middleEastStats.asStateFlow()

    private val _europeStats = MutableStateFlow(PingStats(65, 48, 68, 92, listOf(60, 65, 68, 62, 65, 70, 64, 65, 68, 72, 65, 63, 67, 65, 62, 65)))
    val europeStats: StateFlow<PingStats> = _europeStats.asStateFlow()

    private val _asiaStats = MutableStateFlow(PingStats(137, 98, 137, 188, listOf(130, 137, 140, 135, 137, 145, 132, 137, 138, 142, 137, 135, 139, 137, 131, 137)))
    val asiaStats: StateFlow<PingStats> = _asiaStats.asStateFlow()

    private val _globalStats = MutableStateFlow(PingStats(45, 30, 48, 85, listOf(40, 45, 42, 47, 44, 49, 41, 46, 45, 48, 44, 43, 47, 45, 40, 46)))
    val globalStats: StateFlow<PingStats> = _globalStats.asStateFlow()

    private var job: Job? = null

    fun startMonitoring(scope: CoroutineScope) {
        if (job != null) return
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(2000)
                _middleEastStats.value = updateStats(_middleEastStats.value, 15, 30)
                _europeStats.value = updateStats(_europeStats.value, 55, 75)
                _asiaStats.value = updateStats(_asiaStats.value, 125, 150)
                _globalStats.value = updateStats(_globalStats.value, 35, 55)
            }
        }
    }

    fun stopMonitoring() {
        job?.cancel()
        job = null
    }

    private fun updateStats(old: PingStats, min: Int, max: Int): PingStats {
        val current = Random.nextInt(min, max)
        val history = (old.history + current).takeLast(16)
        val best = if (old.best == 0 || current < old.best) current else old.best
        val worst = if (current > old.worst) current else old.worst
        val average = history.average().toInt()
        return PingStats(
            current = current,
            best = best,
            average = average,
            worst = worst,
            history = history
        )
    }
}
