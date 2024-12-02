package com.example.timesych

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.compose.runtime.State
import kotlinx.coroutines.launch


class TimerViewModel(private val application: Application) : AndroidViewModel(application)  {
    private val context = getApplication<Application>().applicationContext
    private val appContext = application.applicationContext
    private val _cutOffTimes = mutableListOf<CutOffTime>()
    val cutOffTimes: List<CutOffTime> get() = _cutOffTimes

    // Состояние для отслеживания времени
    var currentTime = mutableStateOf("00:00:00:00")
    var cutOffTimer = mutableStateOf("00:00:00:00") // Время на момент отсечки
    var cutOffTimesList = mutableStateListOf<String>() // Список времен при отсечке
    var pausedTime = mutableStateOf("00:00:00:00") // Время на момент паузы
    var elapsedTime = mutableStateOf(0L) // Время в миллисекундах
    var currentInputTextTimer = mutableStateOf("") // Текст, который вводится в поле
    var cutOffListTextsTimer  = mutableStateListOf<String>() // Массив текста, который вводится в поле
    var stateTimer = mutableStateOf(StateTimer.RESET)
    enum class StateTimer {
        RESET,
        RUNNING,
        PAUSED
    }
    private var db: AppDatabase? = null
    private var cutOffDao: CutOffDao? = null

    private suspend fun initializeDatabase() {
        if (db == null) {
            db = Room.databaseBuilder(
                getApplication(),
                AppDatabase::class.java,
                "cutofftimes.db"
            ).build()
            cutOffDao = db?.cutOffDao()
        }
    }

    suspend fun initDBStart() {
        initializeDatabase()
    }

    private suspend fun addCutOffTimesToDatabase() {
        if (cutOffDao == null) {
            Log.e("Database", "DAO is not initialized")
            return
        }
        cutOffTimesList.forEachIndexed { index, time ->
            val cutOffText = cutOffListTextsTimer.getOrNull(index) ?: ""

            // Создаем объект CutOffTime для каждого элемента
            val cutOffTime = CutOffTime(
                id = 0,  // id будет сгенерирован автоматически
                listNumber = index + 1,  // Порядковый номер списка
                cutOffTime = time,
                cutOffText = cutOffText
            )

            cutOffDao?.insert(cutOffTime) ?: Log.e("Database", "cutOffDao is null")
        }
        fetchCutOffTimes()
    }

    suspend fun fetchCutOffTimes() {
        _cutOffTimes.clear()  // Очищаем старые данные
        val cutOffList = cutOffDao?.getAll()

        // Добавляем данные в список, если они не null
        if (cutOffList != null) {
            _cutOffTimes.addAll(cutOffList)  // Добавляем в список, если данные не null
        } else {
            Log.e("TimerViewModel", "Failed to fetch cut-off times: data is null")
        }
    }

    suspend fun startTimer() {
        if (stateTimer.value != StateTimer.RUNNING) {
            stateTimer.value = StateTimer.RUNNING
        }
        //initializeDatabase()
    }

    // Обновить текст для определенной отсечки по индексу
    fun updateCutOffText(index: Int, newText: String) {
        if (index >= 0 && index < cutOffListTextsTimer.size) {
            cutOffListTextsTimer[index] = newText
        }
    }

    // Отсечка
    fun cutOffTimer() {
        cutOffTimer.value = currentTime.value // Сохраняем текущее время
        cutOffTimesList.add(currentTime.value) // Добавляем текущее время в список отсечек
        cutOffListTextsTimer.add(currentInputTextTimer.value)
        currentInputTextTimer.value = ""
    }

    // Сброс таймера
    fun resetTimer() {
        if (stateTimer.value == StateTimer.PAUSED) {
            stateTimer.value = StateTimer.RESET
            currentTime.value = "00:00:00:00"
            elapsedTime.value = 0L
            cutOffListTextsTimer.clear()
            currentInputTextTimer.value = ""
            cutOffTimesList.clear()
            cutOffTimer.value = "00:00:00:00"
        }

    }

    // Сбросить таймер
    fun saveResetTimer() {
        if (stateTimer.value == StateTimer.PAUSED) {
            stateTimer.value = StateTimer.RESET
            currentTime.value = "00:00:00:00"
            elapsedTime.value = 0L
            cutOffListTextsTimer.clear()
            currentInputTextTimer.value = ""
            cutOffTimesList.clear()
            cutOffTimer.value = "00:00:00:00"
        }

    }

    // Сохранить таймер
    suspend fun saveTime() {
        if (stateTimer.value != StateTimer.RESET) {
            viewModelScope.launch {
                addCutOffTimesToDatabase()
            }
        }

    }

    // Пауза таймера
    fun pauseTimer() {
        if (stateTimer.value == StateTimer.RUNNING) {
            stateTimer.value = StateTimer.PAUSED
            pausedTime.value = currentTime.value

        }
    }

    // Продолжить таймер
    fun resumeTimer() {
        if (stateTimer.value == StateTimer.PAUSED)
            stateTimer.value = StateTimer.RUNNING
    }
}
