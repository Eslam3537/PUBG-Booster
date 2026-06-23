package com.aistudio.pubgbooster.fxghqz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_backups")
data class SystemBackup(
    @PrimaryKey val settingKey: String, // format: "namespace:setting_name" -> e.g. "global:animator_duration_scale"
    val settingNamespace: String,        // global, system, secure
    val settingName: String,             // animator_duration_scale
    val originalValue: String,           // value before boost
    val modifiedValue: String,           // value after boost
    val updatedAt: Long,                 // epoch timestamp
    val modifiedBy: String               // e.g. "BASIC_BOOST" or "CUSTOM_COMMAND: {id}"
)
