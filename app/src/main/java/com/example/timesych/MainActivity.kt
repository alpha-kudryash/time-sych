package com.example.timesych

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.timesych.ui.theme.TimeSychTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var audioManager: AudioManager

    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        lifecycleScope.launch {
            try {
                timerViewModel.initDBStart()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting timer: ${e.message}")
            }
        }

        setContent {
            TimeSychTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwipeableTabs(
                        modifier = Modifier.padding(innerPadding),
                        timerViewModel = timerViewModel
                    )
                }
            }
        }
    }

    // Обработка нажатия кнопок (увеличение громкости)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (timerViewModel.stateTimer.value != TimerViewModel.StateTimer.RUNNING) {
                lifecycleScope.launch {
                    try {
                        timerViewModel.startTimer()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting timer: ${e.message}")
                    }
                }
                triggerVibrationHigh()  // Добавляем вибрацию
                return true
            }
            if (timerViewModel.stateTimer.value == TimerViewModel.StateTimer.RUNNING) {
                timerViewModel.cutOffTimer()
                triggerVibrationLow()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun triggerVibrationHigh() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)  // Вибрация длительностью 100 миллисекунд
        } else {
            vibrator.vibrate(200)  // Для старых версий Android (до API 26)
        }
    }

    private fun triggerVibrationLow() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)  // Вибрация длительностью 100 миллисекунд
        } else {
            vibrator.vibrate(100)  // Для старых версий Android (до API 26)
        }
    }
}

@Composable
fun SwipeableTabs(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {
    val pagerState = rememberPagerState() // Состояние для контроля текущей страницы
    //val context = LocalContext.current // Получаем текущий контекст

    // HorizontalPager для создания свайпа между вкладками
    HorizontalPager(
        count = 2, // Количество страниц (вкладок)
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> MainScreen(timerViewModel = timerViewModel)
            1 -> SecondTab(timerViewModel = timerViewModel) // Вторая вкладка
        }
    }

    // Индикатор текущей страницы (точки)
    HorizontalPagerIndicator(
        pagerState = pagerState,
        modifier = Modifier
            .padding(16.dp),
        //.align(Alignment.BottomCenter),
        activeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        inactiveColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {
    // Получаем данные из ViewModel
    val currentTime by remember { timerViewModel.currentTime }
    val state by remember { timerViewModel.stateTimer }
    val pausedTime by remember { timerViewModel.pausedTime } // Время при паузе
    val cutOffTime by remember { timerViewModel.cutOffTimer } // Время при отсечке
    val cutOffTimes = timerViewModel.cutOffTimesList // Список отсечек (не используем remember)
    val elapsedTime by remember { timerViewModel.elapsedTime }
    val inputText by remember { timerViewModel.currentInputTextTimer } // Получаем текст из ViewModel
    val cutOffListTexts = timerViewModel.cutOffListTextsTimer
    val coroutineScope = rememberCoroutineScope()

    // Обработчик нажатия кнопки Старт
    val onStartClick: () -> Unit = {
        coroutineScope.launch {
            try {
                timerViewModel.startTimer()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error starting timer: ${e.message}")
            }
        }
    }

    // Обработчик нажатия кнопки Отсечка
    val onCutOffClick: () -> Unit = {
        timerViewModel.cutOffTimer()
    }

    // Обработчик нажатия кнопки резет
    val onResetClick: () -> Unit = {
        timerViewModel.resetTimer()
    }

    // Обработчик нажатия кнопки Пауза
    val onPauseClick: () -> Unit = {
        timerViewModel.pauseTimer()
    }

    // Обработчик нажатия кнопки Продолжить
    val onResumeClick: () -> Unit = {
        timerViewModel.resumeTimer()
    }

    // Обработчик нажатия кнопки Сохранить и сбросить
    val onSaveResetClick: () -> Unit = {
        //timerViewModel.viewModelScope.launch {
        //DatabaseHelper.saveCutOffTime(context, cutOffTimes, cutOffListTexts)}
        timerViewModel.saveResetTimer()
    }

    // Обработчик нажатия кнопки Сохранить и сбросить
    val onSaveTimeClick: () -> Unit = {
        coroutineScope.launch {
            try {
                timerViewModel.saveTime()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error starting timer: ${e.message}")
            }
        }
        //timerViewModel.viewModelScope.launch {
        //DatabaseHelper.saveCutOffTime(context, cutOffTimes, cutOffListTexts)}

    }

    // Логика отсчёта времени
    LaunchedEffect(state) {
        while (state == TimerViewModel.StateTimer.RUNNING) {
            val hours = ((timerViewModel.elapsedTime.value / 1000) / 3600).toInt()
            val minutes = ((timerViewModel.elapsedTime.value / 1000) / 60).toInt() // 60 000 мс = 1 минута
            val seconds = ((timerViewModel.elapsedTime.value / 1000) % 60).toInt() // 1000 мс = 1 секунда
            val milliseconds = (timerViewModel.elapsedTime.value % 1000).toInt()

            // Обновляем текущие значения времени
            timerViewModel.currentTime.value = String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliseconds / 10)

            // Задержка на 50 миллисекунд для обновления времени
            delay(50)  // Интервал обновления (50 мс = 0.05 сек)
            timerViewModel.elapsedTime.value += 50  // Увеличиваем время на 50 миллисекунд
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)  // Время будет вверху экрана
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Текст с временем
            Text(
                text = currentTime,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            )
        }
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp) // чтобы избежать наложения на другие элементы
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Добавим горизонтальные отступы
                .padding(bottom = 160.dp) // Добавим горизонтальные отступы

        ) {
            // Отображаем все времена отсечек
            if (cutOffTimes.isNotEmpty()) {
                itemsIndexed(cutOffTimes) { index, time ->  // Для каждого элемента в cutOffTimes
                    var localInputText by remember { mutableStateOf(cutOffListTexts.getOrNull(index) ?: "") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // Чтобы Row занимал всю ширину
                            .padding(vertical = 4.dp) // Отступы между отсечками

                    ) {
                    Text(
                        text = time,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(end = 8.dp)
                    )
                    TextField(
                        value = localInputText,
                        onValueChange = { localInputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    // Обновить глобальный список текста, когда фокус теряется
                                    timerViewModel.updateCutOffText(index, localInputText)
                                }
                            }
                    )
                    }
                }
            }
        }
            // Кнопки внизу
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)  // Кнопки выравниваются по центру внизу
                    .padding(16.dp)  // Добавим отступы для красивого вида
            ) {
                Row(
                    modifier = Modifier
                        //.fillMaxWidth()
                        .align(Alignment.CenterHorizontally)

                ) {
                    // Кнопка "Старт-отсечка-продолжить"
                    Button(
                        onClick = {
                            if (state == TimerViewModel.StateTimer.RUNNING) onCutOffClick()
                            if (state == TimerViewModel.StateTimer.RESET) onStartClick()
                            if (state == TimerViewModel.StateTimer.PAUSED) onStartClick()
                        }
                    ) {
                        Text(
                            text = when (state) {
                                TimerViewModel.StateTimer.RUNNING -> "Отсечка"
                                TimerViewModel.StateTimer.RESET -> "Старт"
                                TimerViewModel.StateTimer.PAUSED -> "Продолжить"
                                else -> ""
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(16.dp)  // Отступы для красивого вида
                ) {
                    // Кнопка "Пауза-Reset"
                    Button(
                        onClick = {
                            when (state) {
                                TimerViewModel.StateTimer.RUNNING -> onPauseClick()
                                TimerViewModel.StateTimer.PAUSED -> onResetClick()
                                else -> {}
                            }
                        },
                        enabled = (state != TimerViewModel.StateTimer.RESET)  // Кнопка "Reset" активна только если таймер на паузе
                    ) {
                        Text(
                            text = when (state) {
                                TimerViewModel.StateTimer.RUNNING -> "Пауза"
                                TimerViewModel.StateTimer.RESET -> "Сброс"
                                TimerViewModel.StateTimer.PAUSED -> "Сброс"
                                else -> ""
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))  // Добавление отступа между кнопками

                    // Кнопка "Сохранить"
                    Button(
                        onClick = {
                            if (state != TimerViewModel.StateTimer.RESET) onSaveTimeClick()
                        },
                        enabled = (state != TimerViewModel.StateTimer.RESET)
                    ) {
                        Text(text = "Сохранить")
                    }
                }
            }
        }
}

@Composable
fun SecondTab(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {


    // Загружаем данные из базы при старте экрана
    LaunchedEffect(Unit) {
        try {
            timerViewModel.fetchCutOffTimes()  // Запускаем функцию загрузки данных
        } catch (e: Exception) {
            Log.e("SecondTab", "Error fetching cut off times: ${e.message}")
        }
    }
    // Получаем данные из ViewModel
    val cutOffTimes = timerViewModel.cutOffTimes
    // Отображаем данные в LazyColumn
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(cutOffTimes) { cutOffTime ->  // Здесь cutOffTime - это объект типа CutOffTime
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Отображаем данные из объекта CutOffTime
                Text(
                    text = "№${cutOffTime.listNumber} - ${cutOffTime.cutOffTime ?: "Нет времени"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cutOffTime.cutOffText ?: "Нет текста",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SwipeTheme {
        SwipeableTabs()
    }
}*/