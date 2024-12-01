package com.example.timesych

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseHelper {

    private var database: AppDatabase? = null

    // Инициализация базы данных
    suspend fun initialize(context: Context) {
        if (database == null) {
            // Переместим инициализацию в фоновый поток
            database = withContext(Dispatchers.IO) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cutoff_database"
                ).build()
            }
        }
    }

    // Метод для записи нового CutOffTime
    /*suspend fun insertCutOffTime(cutOffTime: CutOffTime) {
        dao.insertCutOffTime(cutOffTime)
    }*/

    // Метод для получения всех CutOffTimes
    /*suspend fun getAllCutOffTimes(): List<CutOffTime> {
        return dao.getAllCutOffTimes()
    }*/

    // Сохранение времени отсечки
    suspend fun saveCutOffTime(context: Context, times: List<String>, texts: List<String>) {
        val cutOffTime = CutOffTimeEntity(cutOffTimes = times, cutOffTextsTimer = texts)
        database?.cutOffDao()?.insertCutOffTime(cutOffTime)
    }

    /*// Сохранение текста отсечки
    suspend fun saveCutOffText(context: Context, text: String) {
        val cutOffText = CutOffTextEntity(text = text)
        database?.cutOffDao()?.insertCutOffText(cutOffText)
    }

    // Получение всех времен отсечек
    suspend fun getCutOffTimes(context: Context): List<CutOffTimeEntity> {
        return withContext(Dispatchers.IO) {
            database?.cutOffDao()?.getAllCutOffTimes() ?: emptyList()
        }
    }

    // Получение всех текстов отсечек
    suspend fun getCutOffTexts(context: Context): List<CutOffTextEntity> {
        return withContext(Dispatchers.IO) {
            database?.cutOffDao()?.getAllCutOffTexts() ?: emptyList()
        }
    }*/
}
