package com.example.inscit.data.room.dao

import androidx.room.*
import com.example.inscit.data.room.entity.VersusMatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VersusMatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: VersusMatchEntity)

    @Update
    suspend fun update(match: VersusMatchEntity)

    @Query("SELECT * FROM versus_matches WHERE userId = 'default_user' ORDER BY timestamp DESC")
    suspend fun getMatches(): List<VersusMatchEntity>

    @Query("SELECT * FROM versus_matches WHERE userId = 'default_user' ORDER BY timestamp DESC LIMIT 10")
    fun getRecentMatches(): Flow<List<VersusMatchEntity>>

    @Query("SELECT * FROM versus_matches WHERE userId = 'default_user' AND matchId = :matchId")
    suspend fun getMatchById(matchId: String): VersusMatchEntity?

    @Query("DELETE FROM versus_matches WHERE userId = 'default_user' AND timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
