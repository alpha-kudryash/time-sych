package com.example.timesych

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    // Состояние для отслеживания времени
    var currentTime = mutableStateOf("00:00:00:00")
    var isTimerRunning = mutableStateOf(false)
    var isPaused = mutableStateOf(false)
    var isCutOff = mutableStateOf(false) // Отсечка
    var cutOffTimer = mutableStateOf("00:00:00:00") // Время на момент отсечки
    var cutOffTimes = mutableStateListOf<String>() // Список времен при отсечке
    var pausedTime = mutableStateOf("00:00:00:00") // Время на момент паузы
    var elapsedTime = mutableStateOf(0L) // Время в миллисекундах
    var currentInputTextTimer = mutableStateOf("") // Текст, который вводится в поле
    var cutOffListTextsTimer  = mutableStateListOf<String>() // Массив текста, который вводится в поле

    // Запуск таймера
    fun startTimer() {
        if (!isTimerRunning.value) {
            isTimerRunning.value = true
            isPaused.value = false
                //isCutOffTimer.value = false
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
        isCutOff.value = true
        cutOffTimer.value = currentTime.value // Сохраняем текущее время при паузе
        cutOffTimes.add(currentTime.value) // Добавляем текущее время в список отсечек
        cutOffListTextsTimer.add(currentInputTextTimer.value)
        currentInputTextTimer.value = ""
    }

    // Остановка таймера
    fun stopTimer() {
        isTimerRunning.value = false
        isPaused.value = false
        currentTime.value = "00:00:00:00"
        elapsedTime.value = 0L
    }

    // Пауза таймера
    fun pauseTimer() {
        isPaused.value = true
        isTimerRunning.value = false
        pausedTime.value = currentTime.value // Сохраняем текущее время при паузе
    }

    // Продолжить таймер
    fun resumeTimer() {
        isPaused.value = false
        isTimerRunning.value = true
    }
}
