package com.example.timesych

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cutoff_times")
data class CutOffTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "times") val cutOffTimes: List<String>, // Время отсечек
    @ColumnInfo(name = "texts") val cutOffTextsTimer: List<String> // Тексты для отсечек
)