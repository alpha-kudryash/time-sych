package com.example.timesych

import android.os.Bundle
import android.view.KeyEvent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.timesych.ui.theme.TimeSychTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var audioManager: AudioManager

    // Получаем ViewModel
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализация AudioManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        setContent {
            TimeSychTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
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
            if (!timerViewModel.isTimerRunning.value) {
                timerViewModel.startTimer()
                triggerVibrationHigh()  // Добавляем вибрацию
                return true
            }
            if (timerViewModel.isTimerRunning.value) {
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
            vibrator.vibrate(100)  // Для старых версий Android (до API 26)
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

    // Обработка нажатия кнопок устройства
    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Если нажата кнопка уменьшения громкости
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Пауза таймера
            timerViewModel.pauseTimer()
            return true // Возвращаем true, чтобы не обработать событие дальше
        }
        return super.onKeyDown(keyCode, event)
    }*/
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {
    // Получаем данные из ViewModel
    val currentTime by remember { timerViewModel.currentTime }
    val isTimerRunning by remember { timerViewModel.isTimerRunning }
    val isPaused by remember { timerViewModel.isPaused }
    val pausedTime by remember { timerViewModel.pausedTime } // Время при паузе
    val cutOffTime by remember { timerViewModel.cutOffTimer } // Время при отсечке
    val isCutOff by remember { timerViewModel.isCutOff } // отсечка
    val cutOffTimes = timerViewModel.cutOffTimes // Список отсечек (не используем remember)
    val elapsedTime by remember { timerViewModel.elapsedTime }
    val inputText by remember { timerViewModel.currentInputTextTimer } // Получаем текст из ViewModel
    val cutOffListTexts = timerViewModel.cutOffListTextsTimer

    // Обработчик нажатия кнопки Старт
    val onStartClick: () -> Unit = {
        timerViewModel.startTimer()
    }

    // Обработчик нажатия кнопки Отсечка
    val onCutOffClick: () -> Unit = {
        timerViewModel.cutOffTimer()
    }

    // Обработчик нажатия кнопки Стоп
    val onStopClick: () -> Unit = {
        timerViewModel.stopTimer()
    }

    // Обработчик нажатия кнопки Пауза
    val onPauseClick: () -> Unit = {
        timerViewModel.pauseTimer()
    }

    // Обработчик нажатия кнопки Продолжить
    val onResumeClick: () -> Unit = {
        timerViewModel.resumeTimer()
    }

    // Логика отсчёта времени
    LaunchedEffect(isTimerRunning, isPaused) {
        if (isTimerRunning && !isPaused) {
            //var elapsedTime = 0L  // Время в миллисекундах
            while (isTimerRunning) {
                // Форматируем время в миллисекундах (часы:минуты:секунды:миллисекунды)
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
    }

    // Эффект, который срабатывает, когда isCutOff == true, и сбрасывает его на false после отображения
    LaunchedEffect(isCutOff) {
        if (isCutOff) {
            //delay(2000) // Задержка для отображения времени отсечки (2 секунды)
            timerViewModel.isCutOff.value = false // Сбросить отсечку
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
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
                //.weight(1f)  // Разрешить оставшееся пространство для списка отсечек
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
                            .padding(vertical = 4.dp) // Отступы между отсечками
                            .padding(end = 8.dp) // Отступ между временем и дополнительным текстом
                    )
                    // Поле для редактирования текста
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

                // Кнопка "Старт-отсечка"
                Button(
                    onClick = {
                        if (isTimerRunning) {
                            onCutOffClick()  // Если таймер запущен, вызываем функцию отсечки
                        } else {
                            onStartClick()  // Если таймер не запущен, запускаем его
                        }
                    }
                ) {
                    Text(text = if (isTimerRunning) "Отсечка" else "Старт")
                }

                // Кнопка "Стоп"
                Button(
                    onClick = onStopClick,
                    enabled = isTimerRunning  // Кнопка "Стоп" активна только если таймер запущен
                ) {
                    Text(text = "Стоп")
                }

                // Кнопка "Пауза-продолжить"
                Button(
                    onClick = {
                        if (isTimerRunning) {
                            onPauseClick()  // Если таймер запущен, вызываем функцию паузы
                        } else {
                            onStartClick()  // Если таймер на паузе, запускаем его
                        }
                    }
                ) {
                    Text(text = if (isTimerRunning) "Пауза" else "Продолжить")
                }
            }
        }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimeSychTheme {
        MainScreen(timerViewModel = TimerViewModel())
    }
}
