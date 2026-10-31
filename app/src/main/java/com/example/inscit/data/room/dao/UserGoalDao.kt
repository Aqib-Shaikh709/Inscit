package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.UserGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: UserGoalEntity)

    @Update
    suspend fun update(goal: UserGoalEntity)

    @Delete
    suspend fun delete(goal: UserGoalEntity)

    @Query("SELECT * FROM user_goals WHERE userId = 'default_user'")
    fun getGoals(): Flow<List<UserGoalEntity>>

    @Query("SELECT * FROM user_goals WHERE userId = 'default_user' AND id = :goalId")
    suspend fun getGoalById(goalId: String): UserGoalEntity?

    @Query("DELETE FROM user_goals WHERE userId = 'default_user' AND isCompleted = 1 AND completedAt < :cutoff")
    suspend fun deleteCompletedOlderThan(cutoff: Long)
}
