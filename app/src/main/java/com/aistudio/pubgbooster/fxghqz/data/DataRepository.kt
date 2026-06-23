package com.aistudio.pubgbooster.fxghqz.data

import kotlinx.coroutines.flow.Flow

class DataRepository(
    private val appDao: AppDao,
    private val sessionHistoryDao: SessionHistoryDao
) {

    // Session History API
    val allSessionsFlow: Flow<List<SessionHistory>> = sessionHistoryDao.getAllSessionsFlow()

    suspend fun insertSession(session: SessionHistory): Long {
        return sessionHistoryDao.insertSession(session)
    }

    suspend fun clearAllSessions() {
        sessionHistoryDao.clearAllSessions()
    }

    suspend fun getAllSessions(): List<SessionHistory> {
        return sessionHistoryDao.getAllSessions()
    }

    // Custom Commands API
    val allCustomCommands: Flow<List<CustomCommand>> = appDao.getAllCustomCommands()

    suspend fun getEnabledCustomCommands(): List<CustomCommand> {
        return appDao.getEnabledCustomCommands()
    }

    suspend fun insertCustomCommand(command: CustomCommand): Long {
        return appDao.insertCustomCommand(command)
    }

    suspend fun updateCustomCommand(command: CustomCommand) {
        appDao.updateCustomCommand(command)
    }

    suspend fun deleteCustomCommand(command: CustomCommand) {
        appDao.deleteCustomCommand(command)
    }

    suspend fun deleteCustomCommandById(id: Int) {
        appDao.deleteCustomCommandById(id)
    }

    suspend fun clearAllCustomCommands() {
        appDao.clearAllCustomCommands()
    }


    // System Backups API
    val allBackupsFlow: Flow<List<SystemBackup>> = appDao.getAllBackupsFlow()

    suspend fun getAllBackups(): List<SystemBackup> {
        return appDao.getAllBackups()
    }

    suspend fun getBackupByKey(key: String): SystemBackup? {
        return appDao.getBackupByKey(key)
    }

    suspend fun insertBackup(backup: SystemBackup) {
        appDao.insertBackup(backup)
    }

    suspend fun clearAllBackups() {
        appDao.clearAllBackups()
    }

    suspend fun deleteBackupByKey(key: String) {
        appDao.deleteBackupByKey(key)
    }
}
