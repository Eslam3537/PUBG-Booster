package com.aistudio.pubgbooster.fxghqz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_history")
data class SessionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val gamePackage: String,
    val successCount: Int,
    val failedCount: Int,
    val tempBefore: Float,
    val tempAfter: Float,
    val ramBefore: Long, // in MB
    val ramAfter: Long,  // in MB
    val avgPing: Int     // in ms
)
