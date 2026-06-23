package com.aistudio.pubgbooster.fxghqz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Custom Commands
    @Query("SELECT * FROM custom_commands ORDER BY id ASC")
    fun getAllCustomCommands(): Flow<List<CustomCommand>>

    @Query("SELECT * FROM custom_commands WHERE enabled = 1")
    suspend fun getEnabledCustomCommands(): List<CustomCommand>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCommand(command: CustomCommand): Long

    @Update
    suspend fun updateCustomCommand(command: CustomCommand)

    @Delete
    suspend fun deleteCustomCommand(command: CustomCommand)

    @Query("DELETE FROM custom_commands WHERE id = :id")
    suspend fun deleteCustomCommandById(id: Int)

    @Query("DELETE FROM custom_commands")
    suspend fun clearAllCustomCommands()


    // System Backups
    @Query("SELECT * FROM system_backups ORDER BY updatedAt DESC")
    fun getAllBackupsFlow(): Flow<List<SystemBackup>>

    @Query("SELECT * FROM system_backups")
    suspend fun getAllBackups(): List<SystemBackup>

    @Query("SELECT * FROM system_backups WHERE settingKey = :key LIMIT 1")
    suspend fun getBackupByKey(key: String): SystemBackup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: SystemBackup)

    @Query("DELETE FROM system_backups")
    suspend fun clearAllBackups()

    @Query("DELETE FROM system_backups WHERE settingKey = :key")
    suspend fun deleteBackupByKey(key: String)
}
