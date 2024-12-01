package com.example.timesych

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [CutOffTimeEntity::class, CutOffTextEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cutOffDao(): CutOffDao

    companion object {
        // Используется для синхронизации базы данных
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Метод для получения базы данных
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cutoff_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
