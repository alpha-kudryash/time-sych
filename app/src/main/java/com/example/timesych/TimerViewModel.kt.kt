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

    // Запуск таймера
    fun startTimer() {
        if (!isTimerRunning.value) {
            isTimerRunning.value = true
            isPaused.value = false
                //isCutOffTimer.value = false
        }
    }

    // Отсечка
    fun cutOffTimer() {
        isCutOff.value = true
        cutOffTimer.value = currentTime.value // Сохраняем текущее время при паузе
        cutOffTimes.add(currentTime.value) // Добавляем текущее время в список отсечек
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
}
