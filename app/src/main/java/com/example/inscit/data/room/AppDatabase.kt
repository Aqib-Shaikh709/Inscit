package com.example.inscit.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inscit.data.room.converter.Converters
import com.example.inscit.data.room.dao.DailyXpDao
import com.example.inscit.data.room.dao.UserGoalDao
import com.example.inscit.data.room.dao.UserProfileDao
import com.example.inscit.data.room.dao.UserStatsDao
import com.example.inscit.data.room.dao.QuizAttemptDao
import com.example.inscit.data.room.dao.VersusMatchDao
import com.example.inscit.data.room.entity.DailyXpEntity
import com.example.inscit.data.room.entity.UserGoalEntity
import com.example.inscit.data.room.entity.UserProfileEntity
import com.example.inscit.data.room.entity.UserStatsEntity
import com.example.inscit.data.room.entity.QuizAttemptEntity
import com.example.inscit.data.room.entity.VersusMatchEntity

@Database(
    entities = [
        UserProfileEntity::class,
        UserStatsEntity::class,
        UserGoalEntity::class,
        QuizAttemptEntity::class,
        VersusMatchEntity::class,
        DailyXpEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun versusMatchDao(): VersusMatchDao
    abstract fun dailyXpDao(): DailyXpDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inscit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
