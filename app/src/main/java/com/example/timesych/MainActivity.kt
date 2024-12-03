package com.example.timesych

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale

enum class VibrationType {
    HIGH, LOW
}

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
                if (timerViewModel.isDBNotNull) timerViewModel.fetchCutOffTimes()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (timerViewModel.stateTimer.value != TimerViewModel.StateTimer.RUNNING) {
                    lifecycleScope.launch {
                        try {
                            timerViewModel.startStopwatch()
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error starting timer: ${e.message}")
                        }
                    }
                    triggerVibration(VibrationType.HIGH)
                    return true
                } else {
                    timerViewModel.cutOffStopwatch()
                    triggerVibration(VibrationType.LOW)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun triggerVibration(type: VibrationType) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val vibrationDuration = when (type) {
            VibrationType.HIGH -> 200
            VibrationType.LOW -> 100
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(vibrationDuration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(vibrationDuration.toLong())
        }
    }
}

@Composable
fun SwipeableTabs(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {
    val pagerState = rememberPagerState()

    HorizontalPager(
        count = 2,
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> MainScreen(timerViewModel = timerViewModel)
            1 -> SecondTab(timerViewModel = timerViewModel)
        }
    }

    HorizontalPagerIndicator(
        pagerState = pagerState,
        modifier = Modifier
            .padding(16.dp),
        activeColor = MaterialTheme.colorScheme.primary,
        inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, timerViewModel: TimerViewModel) {
    val state                   by remember { timerViewModel.stateTimer }
    val cutOffListTimes             = timerViewModel.cutOffTimesList
    val elapsedTime             by timerViewModel.elapsedTime.observeAsState(0L)

    val cutOffListTexts         = timerViewModel.cutOffListTextsTimer
    val coroutineScope          = rememberCoroutineScope()
    val lazyListState           = rememberLazyListState()
    val mainStopwatchTime                = remember { mutableStateOf("00:00:00:00") }

    val onCutOffClick: ()       -> Unit = { timerViewModel.cutOffStopwatch() }
    val onResetClick: ()        -> Unit = { timerViewModel.resetStopwatch() }
    val onPauseClick: ()        -> Unit = { timerViewModel.pauseStopwatch() }
    val onStartClick: ()        -> Unit = {
        coroutineScope.launch {
            try {
                timerViewModel.startStopwatch()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error starting timer: ${e.message}")
            }
        }
    }
    val onSaveTimeClick: () -> Unit = {
        coroutineScope.launch {
            try {
                timerViewModel.saveTime()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error starting timer: ${e.message}")
            }
        }
    }

    LaunchedEffect(state) {
        while (state == TimerViewModel.StateTimer.RUNNING) {
            delay(10)
            val currentMilliseconds = (elapsedTime % 1000) / 10
            val currentSeconds = ((elapsedTime / 1000) % 60)
            val currentMinutes = (elapsedTime / 60000) % 60
            val currentHours = (elapsedTime / 3600000) % 24

            mainStopwatchTime.value =
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d:%02d",
                    currentHours,
                    currentMinutes,
                    currentSeconds,
                    currentMilliseconds
                )
        }
        if (state == TimerViewModel.StateTimer.RESET)
            mainStopwatchTime.value = "00:00:00:00"
    }

    LaunchedEffect(cutOffListTimes.size) {
        if (cutOffListTimes.isNotEmpty()) {
            lazyListState.animateScrollToItem(cutOffListTimes.size - 1)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = mainStopwatchTime.value,
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 160.dp)
        ) {
            if (cutOffListTimes.isNotEmpty()) {
                itemsIndexed(cutOffListTimes) { index, time ->
                    var localInputText by remember {
                        mutableStateOf(cutOffListTexts.getOrNull(index) ?: "")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)

                    ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(end = 8.dp)
                    )
                    TextField(
                        value = localInputText,
                        onValueChange = {
                            localInputText = it
                            timerViewModel.updateCutOffText(index, localInputText)},
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    timerViewModel.updateCutOffText(index, localInputText)
                                }
                            }
                    )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)

            ) {
                Button(
                    onClick = {
                        when (state) {
                            TimerViewModel.StateTimer.RUNNING   -> onCutOffClick()
                            TimerViewModel.StateTimer.RESET     -> onStartClick()
                            TimerViewModel.StateTimer.PAUSED    -> onResetClick()
                            else                                -> {}
                        }
                    }
                ) {
                    Text(
                        text = when (state) {
                            TimerViewModel.StateTimer.RUNNING   -> "Отсечка"
                            TimerViewModel.StateTimer.RESET     -> "Старт"
                            TimerViewModel.StateTimer.PAUSED    -> "Продолжить"
                            else                                -> ""
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        when (state) {
                            TimerViewModel.StateTimer.RUNNING   -> onPauseClick()
                            TimerViewModel.StateTimer.PAUSED    -> onResetClick()
                            else                                -> {}
                        }
                    },
                    enabled = (state != TimerViewModel.StateTimer.RESET)
                ) {
                    Text(
                        text = when (state) {
                            TimerViewModel.StateTimer.RUNNING   -> "Пауза"
                            TimerViewModel.StateTimer.RESET     -> "Сброс"
                            TimerViewModel.StateTimer.PAUSED    -> "Сброс"
                            else                                -> ""
                        }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                
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
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val cutOffTextList = timerViewModel.cutOffTimes
    val isDBNotEmpty by remember { derivedStateOf { timerViewModel.isDBNotEmpty } }

    val onDeleteAllClick: () -> Unit = {
        coroutineScope.launch {
            try {
                timerViewModel.deleteAll()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error starting timer: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                timerViewModel.fetchCutOffTimes()
            } catch (e: Exception) {
                Log.e("SecondTab", "Error fetching cut off times: ${e.message}")
            }
        }
    }

    LaunchedEffect(isDBNotEmpty) {
        coroutineScope.launch {
            try {
                timerViewModel.fetchCutOffTimes()
            } catch (e: Exception) {
                Log.e("SecondTab", "Error fetching cut off times: ${e.message}")
            }
        }
    }

    LaunchedEffect(cutOffTextList.size) {
        if (cutOffTextList.isNotEmpty()) {
            lazyListState.animateScrollToItem(cutOffTextList.size - 1)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            if (cutOffTextList.isNotEmpty()) {
                items(cutOffTextList) { cutOffTimeItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "№${cutOffTimeItem.listNumber} - ${cutOffTimeItem.cutOffTime ?: "Нет времени"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cutOffTimeItem.cutOffText ?: "Нет текста",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)

            ) {
                Button(
                    onClick = { onDeleteAllClick() },
                    enabled = isDBNotEmpty
                ) {
                    Text(text = "Удалить всё")
                }
            }
        }
    }
}