package com.k3nyel.wifiextract.data

import android.content.Context

/** Local preferences. Stored on-device only and excluded from backups (allowBackup=false). */
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("mna_settings", Context.MODE_PRIVATE)

    var checkUpdatesOnStart: Boolean
        get() = prefs.getBoolean(KEY_UPDATES, true)
        set(value) = prefs.edit().putBoolean(KEY_UPDATES, value).apply()

    var resolveHostnames: Boolean
        get() = prefs.getBoolean(KEY_HOSTNAMES, true)
        set(value) = prefs.edit().putBoolean(KEY_HOSTNAMES, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_SCREEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN, value).apply()

    var timeoutMs: Int
        get() = prefs.getInt(KEY_TIMEOUT, 1000).takeIf { it in TIMEOUT_OPTIONS } ?: 1000
        set(value) = prefs.edit().putInt(KEY_TIMEOUT, value).apply()

    companion object {
        val TIMEOUT_OPTIONS = listOf(500, 1000, 2000)
        private const val KEY_UPDATES = "check_updates_on_start"
        private const val KEY_HOSTNAMES = "resolve_hostnames"
        private const val KEY_SCREEN = "keep_screen_on"
        private const val KEY_TIMEOUT = "timeout_ms"
    }
}
