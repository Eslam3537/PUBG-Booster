package com.aistudio.pubgbooster.fxghqz.util

data class PingStats(
    val current: Int,
    val best: Int,
    val average: Int,
    val worst: Int,
    val history: List<Int>
)
