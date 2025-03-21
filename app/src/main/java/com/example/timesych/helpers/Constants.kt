package com.example.timesych.helpers

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.pow

// shared preferences
const val SELECTED_TIME_ZONES = "selected_time_zones"
const val EDITED_TIME_ZONE_TITLES = "edited_time_zone_titles"
const val TIMER_SECONDS = "timer_seconds"
const val TIMER_VIBRATE = "timer_vibrate"
const val TIMER_SOUND_URI = "timer_sound_uri"
const val TIMER_SOUND_TITLE = "timer_sound_title"
const val TIMER_CHANNEL_ID = "timer_channel_id"
const val TIMER_LABEL = "timer_label"
const val TOGGLE_STOPWATCH = "toggle_stopwatch"
const val TIMER_MAX_REMINDER_SECS = "timer_max_reminder_secs"
const val ALARM_MAX_REMINDER_SECS = "alarm_max_reminder_secs"
const val ALARM_LAST_CONFIG = "alarm_last_config"
const val TIMER_LAST_CONFIG = "timer_last_config"
const val INCREASE_VOLUME_GRADUALLY = "increase_volume_gradually"
const val ALARMS_SORT_BY = "alarms_sort_by"
const val STOPWATCH_LAPS_SORT_BY = "stopwatch_laps_sort_by"
const val WAS_INITIAL_WIDGET_SET_UP = "was_initial_widget_set_up"

const val TABS_COUNT = 4
const val EDITED_TIME_ZONE_SEPARATOR = ":"
const val ALARM_ID = "alarm_id"
const val NOTIFICATION_ID = "notification_id"
const val DEFAULT_ALARM_MINUTES = 480
const val DEFAULT_MAX_ALARM_REMINDER_SECS = 300
const val DEFAULT_MAX_TIMER_REMINDER_SECS = 60
const val SIMPLE_PHONE = "Simple_Phone"
const val ALARM_NOTIFICATION_CHANNEL_ID = "Alarm_Channel"
const val EARLY_ALARM_DISMISSAL_CHANNEL_ID = "Early Alarm Dismissal"

const val OPEN_STOPWATCH_TAB_INTENT_ID = 9993
const val PICK_AUDIO_FILE_INTENT_ID = 9994
const val REMINDER_ACTIVITY_INTENT_ID = 9995
const val OPEN_ALARMS_TAB_INTENT_ID = 9996
const val OPEN_APP_INTENT_ID = 9997
const val ALARM_NOTIF_ID = 9998
const val TIMER_RUNNING_NOTIF_ID = 10000
const val STOPWATCH_RUNNING_NOTIF_ID = 10001
const val EARLY_ALARM_DISMISSAL_INTENT_ID = 10002
const val EARLY_ALARM_NOTIF_ID = 10003

const val OPEN_TAB = "open_tab"
const val TAB_CLOCK = 0
const val TAB_ALARM = 1
const val TAB_STOPWATCH = 2
const val TAB_TIMER = 3
const val TIMER_ID = "timer_id"
const val INVALID_TIMER_ID = -1

// stopwatch sorting
const val SORT_BY_LAP = 1
const val SORT_BY_LAP_TIME = 2
const val SORT_BY_TOTAL_TIME = 4

const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 4096
const val SORT_BY_DURATION = 8192
const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072
const val SORT_BY_DATE_CREATED = 262144

// alarm sorting
const val SORT_BY_CREATION_ORDER = 0
const val SORT_BY_ALARM_TIME = 1
const val SORT_BY_DATE_AND_TIME = 2

const val TODAY_BIT = -1
const val TOMORROW_BIT = -2

// stopwatch shortcut
const val STOPWATCH_SHORTCUT_ID = "stopwatch_shortcut_id"
const val STOPWATCH_TOGGLE_ACTION = "com.simplemobiletools.clock.TOGGLE_STOPWATCH"

fun getPassedSeconds(): Int {
    val calendar = Calendar.getInstance()
    val isDaylightSavingActive = TimeZone.getDefault().inDaylightTime(Date())
    var offset = calendar.timeZone.rawOffset
    if (isDaylightSavingActive) {
        offset += TimeZone.getDefault().dstSavings
    }
    return ((calendar.timeInMillis + offset) / 1000).toInt()
}

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

fun getTomorrowBit(): Int {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_WEEK, 1)
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return 2.0.pow(dayOfWeek).toInt()
}

fun getTodayBit(): Int {
    val calendar = Calendar.getInstance()
    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return 2.0.pow(dayOfWeek).toInt()
}

fun getCurrentDayMinutes(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

fun getTimeDifferenceInMinutes(currentTimeInMinutes: Int, alarmTimeInMinutes: Int, daysUntilAlarm: Int): Int {
    val minutesInADay = 24 * 60
    val minutesUntilAlarm = daysUntilAlarm * minutesInADay + alarmTimeInMinutes
    return if (minutesUntilAlarm > currentTimeInMinutes) {
        minutesUntilAlarm - currentTimeInMinutes
    } else {
        minutesInADay - (currentTimeInMinutes - minutesUntilAlarm)
    }
}