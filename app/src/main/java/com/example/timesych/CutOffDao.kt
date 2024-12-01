package com.example.timesych

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CutOffDao {

    // Вставка данных в базу
    @Insert
    suspend fun insert(cutOffData: CutOffTimeEntity)

    // Получение последней записи (для примера)
    @Query("SELECT * FROM cutoff_times ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): CutOffTimeEntity

    // Вставить новое время отсечки
    @Insert
    suspend fun insertCutOffTime(cutOffTimeEntity: CutOffTimeEntity)

    // Вставить новый текст для отсечки
    @Insert
    suspend fun insertCutOffText(cutOffTextEntity: CutOffTextEntity)

    // Получить все времена отсечек
    @Query("SELECT * FROM cutoff_times")
    suspend fun getAllCutOffTimes(): List<CutOffTimeEntity>

    // Получить все тексты отсечек
    @Query("SELECT * FROM cutoff_texts")
    suspend fun getAllCutOffTexts(): List<CutOffTextEntity>

    // Обновить текст для конкретной отсечки
    @Update
    suspend fun updateCutOffText(cutOffTextEntity: CutOffTextEntity)
}
