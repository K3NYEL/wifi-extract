package com.k3nyel.wifiextract.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.k3nyel.wifiextract.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val checkUpdatesOnStart: Boolean,
    val resolveHostnames: Boolean,
    val keepScreenOn: Boolean,
    val timeoutMs: Int
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private fun read() = SettingsState(
        repository.checkUpdatesOnStart,
        repository.resolveHostnames,
        repository.keepScreenOn,
        repository.timeoutMs
    )

    fun setCheckUpdates(value: Boolean) { repository.checkUpdatesOnStart = value; _state.value = read() }
    fun setResolveHostnames(value: Boolean) { repository.resolveHostnames = value; _state.value = read() }
    fun setKeepScreenOn(value: Boolean) { repository.keepScreenOn = value; _state.value = read() }
    fun setTimeout(value: Int) { repository.timeoutMs = value; _state.value = read() }
}
