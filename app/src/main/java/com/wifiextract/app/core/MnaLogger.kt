package com.wifiextract.app.core

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel { INFO, WARN, ERROR, DEBUG }

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (Math.random() * 1000).toLong(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String,
    val message: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

object MnaLogger {
    private const val MAX_LOGS = 300
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        when (level) {
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
            LogLevel.DEBUG -> Log.d(tag, message)
        }
        val entry = LogEntry(timestamp = System.currentTimeMillis(), level = level, tag = tag, message = message)
        synchronized(this) {
            val current = _logs.value.toMutableList()
            current.add(entry)
            if (current.size > MAX_LOGS) {
                current.removeAt(0)
            }
            _logs.value = current
        }
    }

    fun clear() {
        synchronized(this) {
            _logs.value = emptyList()
        }
    }
}
