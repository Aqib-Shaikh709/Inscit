package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.UserProfileEntity

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE userId = 'default_user'")
    suspend fun getProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE userId = 'default_user' LIMIT 1")
    suspend fun getProfileOrCreate(): UserProfileEntity

    @Delete
    suspend fun delete(profile: UserProfileEntity)
}
