package com.aistudio.pubgbooster.fxghqz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionHistoryDao {

    @Query("SELECT * FROM session_history ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<SessionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionHistory): Long

    @Query("DELETE FROM session_history")
    suspend fun clearAllSessions()

    @Query("SELECT * FROM session_history")
    suspend fun getAllSessions(): List<SessionHistory>
}
