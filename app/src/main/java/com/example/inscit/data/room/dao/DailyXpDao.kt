package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.DailyXpEntity

@Dao
interface DailyXpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(xp: DailyXpEntity)

    @Update
    suspend fun update(xp: DailyXpEntity)

    @Query("SELECT * FROM daily_xp WHERE userId = 'default_user'")
    suspend fun getAll(): List<DailyXpEntity>

    @Query("SELECT xp FROM daily_xp WHERE userId = 'default_user' AND date = :date")
    suspend fun getXpForDate(date: String): Int?

    @Query("SELECT SUM(xp) FROM daily_xp WHERE userId = 'default_user'")
    suspend fun getTotalDailyXp(): Int?

    @Delete
    suspend fun delete(xp: DailyXpEntity)
}
