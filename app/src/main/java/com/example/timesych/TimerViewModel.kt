package com.example.timesych

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class TimerViewModel : ViewModel() {

    // Состояние для отслеживания времени
    var currentTime = mutableStateOf("00:00:00:00")
    var cutOffTimer = mutableStateOf("00:00:00:00") // Время на момент отсечки
    var cutOffTimes = mutableStateListOf<String>() // Список времен при отсечке
    var pausedTime = mutableStateOf("00:00:00:00") // Время на момент паузы
    var elapsedTime = mutableStateOf(0L) // Время в миллисекундах
    var currentInputTextTimer = mutableStateOf("") // Текст, который вводится в поле
    var cutOffListTextsTimer  = mutableStateListOf<String>() // Массив текста, который вводится в поле
    var stateTimer = mutableStateOf(StateTimer.RESET)
    enum class StateTimer {
        RESET,   // Состояние, когда таймер сброшен
        RUNNING, // Состояние, когда таймер выполняется
        PAUSED   // Состояние, когда таймер на паузе
        //CUTOFF
    }
    fun startTimer() {
        if (stateTimer.value != StateTimer.RUNNING) {
            stateTimer.value = StateTimer.RUNNING
        }
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
        cutOffTimes.add(currentTime.value) // Добавляем текущее время в список отсечек
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
            cutOffTimes.clear()
            cutOffTimer.value = "00:00:00:00"
        }

    }

    // Сохранить и Сбросить таймер
    fun saveResetTimer() {
        if (stateTimer.value == StateTimer.PAUSED) {
            //DatabaseHelper.saveCutOffTime(applicationContext, cutOffTimes, cutOffListTextsTimer)
            stateTimer.value = StateTimer.RESET
            currentTime.value = "00:00:00:00"
            elapsedTime.value = 0L
            cutOffListTextsTimer.clear()
            currentInputTextTimer.value = ""
            cutOffTimes.clear()
            cutOffTimer.value = "00:00:00:00"
        }

    }

    // Пауза таймера
    fun pauseTimer() {
        if (stateTimer.value == StateTimer.RUNNING) {
            stateTimer.value = StateTimer.PAUSED
            pausedTime.value = currentTime.value // Сохраняем текущее время при паузе

        }
    }

    // Продолжить таймер
    fun resumeTimer() {
        if (stateTimer.value == StateTimer.PAUSED)
            stateTimer.value = StateTimer.RUNNING
    }

    /*// Метод для добавления времени отсечки
    fun saveCutOffTime(context: Context, time: String) {
        viewModelScope.launch {
            DatabaseHelper.saveCutOffTime(context, time)
        }
    }

    // Метод для добавления текста отсечки
    fun saveCutOffText(context: Context, text: String) {
        viewModelScope.launch {
            DatabaseHelper.saveCutOffText(context, text)
        }
    }
*/
    // Метод для получения всех времен отсечек
    /*fun getCutOffTimes(context: Context): List<CutOffTimeEntity> {
        var cutOffTimes = listOf<CutOffTimeEntity>()
        viewModelScope.launch {
            cutOffTimes = DatabaseHelper.getCutOffTimes(context)
        }
        return cutOffTimes
    }
*/
   /* // Для записи данных в базу
    fun saveCutOffData(cutOffTimes: List<String>, cutOffListTextsTimer: List<String>) {
        // Создаем объект данных для записи в базу
        val cutOffData = CutOffTimeEntity(cutOffTimes = cutOffTimes, cutOffTextsTimer = cutOffListTextsTimer)

        // Запуск асинхронной операции записи в базу данных
        viewModelScope.launch(Dispatchers.IO) {
            try {
                CutOffDao.insert(cutOffData) // Вставка данных в базу
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/
}
