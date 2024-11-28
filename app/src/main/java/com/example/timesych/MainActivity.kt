package com.example.timesych

import android.os.Bundle
import android.view.KeyEvent
import android.media.AudioManager
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
            // Запускаем таймер при нажатии кнопки увеличения громкости
            if (!timerViewModel.isTimerRunning.value) {
                timerViewModel.startTimer()
                return true
            }
            if (timerViewModel.isTimerRunning.value) {
                timerViewModel.cutOffTimer()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
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


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Текст с временем
        Text(
            text = currentTime,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
        )

        // Отображаем все времена отсечек
        if (cutOffTimes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp)) // Добавить пространство между временем и отсечками
            cutOffTimes.forEach { time ->
                Text(
                    text = time,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp) // Добавить отступы между текстами
                )
            }
        }

        // Кнопка "Старт"
        Button(
            onClick = onStartClick,
        ) {
            Text(text = if (isTimerRunning) "Отсечка" else "Старт")
        }

        // Кнопка "Отсечка" (активна только если таймер запущен)
        Button(
            onClick = onCutOffClick,
            enabled = isTimerRunning
        ) {
            Text(text = "Отсечка")
        }

        // Кнопка "Стоп"
        Button(
            onClick = onStopClick,
            enabled = isTimerRunning  // Кнопка "Стоп" активна только если таймер запущен
        ) {
            Text(text = "Стоп")
        }

        // Кнопка "Пауза" (активна только если таймер запущен и не на паузе)
        Button(
            onClick = onPauseClick,
            enabled = isTimerRunning && !isPaused
        ) {
            Text(text = "Пауза")
        }

        // Кнопка "Продолжить" (активна только если на паузе)
        Button(
            onClick = onStartClick,
            enabled = isPaused
        ) {
            Text(text = "Продолжить")
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
