package com.aistudio.pubgbooster.fxghqz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_commands")
data class CustomCommand(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val command: String,
    val description: String,
    val enabled: Boolean = true,
    val restoreCommand: String = ""
)
