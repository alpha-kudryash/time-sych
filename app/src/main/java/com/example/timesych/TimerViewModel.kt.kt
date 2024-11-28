package com.example.timesych

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel



class TimerViewModel : ViewModel() {
    // Состояние для отслеживания времени
    var currentTime = mutableStateOf("00:00:00:00")
    var isCutOff = mutableStateOf(false) // Отсечка
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
        isCutOff.value = true
        cutOffTimer.value = currentTime.value // Сохраняем текущее время при паузе
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
            isCutOff.value = false
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
}
