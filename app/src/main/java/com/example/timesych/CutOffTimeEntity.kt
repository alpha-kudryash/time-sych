package com.example.timesych

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cutoff_times")
data class CutOffTime(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "number") val listNumber: Int, // номер списка
    @ColumnInfo(name = "time") val cutOffTime: String?, // Время отсечек
    @ColumnInfo(name = "text") val cutOffText: String?, // Тексты для отсечек
)