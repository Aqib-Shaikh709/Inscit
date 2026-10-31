package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts WHERE userId = 'default_user' ORDER BY timestamp DESC")
    suspend fun getAttempts(): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE userId = 'default_user' ORDER BY timestamp DESC LIMIT 10")
    fun getRecentAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("DELETE FROM quiz_attempts WHERE userId = 'default_user' AND timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
