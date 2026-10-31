package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.UserStatsEntity

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: UserStatsEntity)

    @Update
    suspend fun update(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE userId = 'default_user'")
    suspend fun getStats(): UserStatsEntity?

    @Delete
    suspend fun delete(stats: UserStatsEntity)
}
