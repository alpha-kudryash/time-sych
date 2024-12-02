package com.example.timesych

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.util.Locale


class TimerViewModel(application: Application) : AndroidViewModel(application)  {
    private val _cutOffTimes = mutableListOf<CutOffTime>()
    val cutOffTimes: List<CutOffTime> get() = _cutOffTimes

    private var cutOffTimer = mutableStateOf("00:00:00:00") // Время на момент отсечки
    var cutOffTimesList = mutableStateListOf<String>() // Список времен при отсечке
    private var currentInputTextTimer = mutableStateOf("") // Текст, который вводится в поле
    var cutOffListTextsTimer  = mutableStateListOf<String>() // Массив текста, который вводится в поле
    var stateTimer = mutableStateOf(StateTimer.RESET)
    var isDBNotNull = false
    var isDBNotEmpty = false
    private var _currentListNumber: MutableState<Int> = mutableIntStateOf(1)
    private val currentListNumber: State<Int> get() = _currentListNumber

    enum class StateTimer {
        RESET,
        RUNNING,
        PAUSED
    }

    private val _elapsedTime = MutableLiveData(0L)
    val elapsedTime: LiveData<Long> = _elapsedTime
    private var startTime = 0L
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

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
            isDBNotNull = true
        }
        _currentListNumber.value = cutOffDao?.getMaxListNumber()?.plus(1) ?: 1
        isDBNotEmpty = currentListNumber.value != 1
    }

    suspend fun initDBStart() {
        initializeDatabase()
    }

    private suspend fun addAllCutOffTimesToDB() {
        if (isDBNotNull) {
            val nextListNumber = currentListNumber.value
            if (cutOffDao?.getMaxListNumber() != nextListNumber) {
                cutOffTimesList.forEachIndexed { index, time ->
                    val cutOffText = cutOffListTextsTimer.getOrNull(index) ?: ""
                    val cutOffTime = CutOffTime(
                        id = 0,  // id будет сгенерирован автоматически
                        listNumber = nextListNumber,
                        cutOffTime = time,
                        cutOffText = cutOffText
                    )

                    cutOffDao?.insert(cutOffTime) ?: Log.e("Database", "cutOffDao is null")
                }
            } else {
                cutOffDao?.deleteByListNumber(nextListNumber)
                cutOffTimesList.forEachIndexed { index, time ->
                    val cutOffText = cutOffListTextsTimer.getOrNull(index) ?: ""
                    val cutOffTime = CutOffTime(
                        id = 0,
                        listNumber = nextListNumber,
                        cutOffTime = time,
                        cutOffText = cutOffText
                    )

                    cutOffDao?.insert(cutOffTime) ?: Log.e("Database", "cutOffDao is null")
                }
            }
            isDBNotEmpty = true
            fetchCutOffTimes()
        }
    }

    private suspend fun deleteAllInDB() {
        if (isDBNotNull) {
            cutOffDao?.deleteAll()
            _currentListNumber.value = cutOffDao?.getMaxListNumber()?.plus(1) ?: 1
            isDBNotEmpty = false
            fetchCutOffTimes()
        }
    }

    suspend fun fetchCutOffTimes() {
        _cutOffTimes.clear()  // Очищаем старые данные
        if (isDBNotNull) {
            val cutOffList = cutOffDao?.getAll() ?: emptyList()
            _cutOffTimes.addAll(cutOffList)  // Добавляем в список, если данные не null
            }

    }

    suspend fun deleteAll() {
        if (isDBNotNull) deleteAllInDB()
    }

    fun startStopwatch() {
        startTime = if (stateTimer.value == StateTimer.RESET) {
            SystemClock.elapsedRealtime() // получаем время, прошедшее с начала работы устройства
        } else {
            SystemClock.elapsedRealtime() - (_elapsedTime.value ?: 0L)
        }
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                _elapsedTime.postValue(elapsedMillis)
                handler.postDelayed(this, 10) // Обновляем каждую 1/10 секунды
            }
        }
        handler.post(runnable!!) // Запускаем обновление времени
        stateTimer.value = StateTimer.RUNNING
    }

    fun pauseStopwatch() {
        handler.removeCallbacks(runnable!!) // Останавливаем обновление
        if (stateTimer.value == StateTimer.RUNNING) {
            stateTimer.value = StateTimer.PAUSED
        }

    }

    fun resetStopwatch() {
        if (stateTimer.value == StateTimer.PAUSED) {
            startTime = SystemClock.elapsedRealtime()
            stateTimer.value = StateTimer.RESET
            cutOffListTextsTimer.clear()
            currentInputTextTimer.value = ""
            cutOffTimesList.clear()
            _elapsedTime.postValue(0L)
            cutOffTimer.value = "00:00:00:00"
            _currentListNumber.value += 1
        }
    }

    fun cutOffStopwatch() {
        val currentCutOffTime = _elapsedTime.value ?: 0L
        val formattedTime = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d:%02d",
            (currentCutOffTime / 3600000) % 24,
            (currentCutOffTime / 60000) % 60,
            (currentCutOffTime / 1000) % 60,
            currentCutOffTime % 1000 / 10
        )
        cutOffTimer.value = formattedTime
        cutOffTimesList.add(formattedTime)
        cutOffListTextsTimer.add(currentInputTextTimer.value)
        currentInputTextTimer.value = ""
    }


    fun updateCutOffText(index: Int, newText: String) {
        if (index >= 0 && index < cutOffListTextsTimer.size) {
            cutOffListTextsTimer[index] = newText
        }
    }

    // Сохранить таймер
    fun saveTime() {
        if (stateTimer.value != StateTimer.RESET) {
            viewModelScope.launch {
                addAllCutOffTimesToDB()
            }
        }
    }
}
