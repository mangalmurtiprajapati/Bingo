package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserStatsEntity::class, AchievementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BingoDatabase : RoomDatabase() {
    abstract fun bingoDao(): BingoDao

    companion object {
        @Volatile
        private var INSTANCE: BingoDatabase? = null

        fun getDatabase(context: Context): BingoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BingoDatabase::class.java,
                    "lucky_bingo_offline.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
