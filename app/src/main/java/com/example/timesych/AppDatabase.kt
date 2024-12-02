package com.example.timesych

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(entities = [CutOffTime::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cutOffDao(): CutOffDao
}
