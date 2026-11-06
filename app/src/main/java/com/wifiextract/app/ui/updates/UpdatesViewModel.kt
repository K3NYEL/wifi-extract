package com.wifiextract.app.ui.updates

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wifiextract.app.core.VersionComparator
import com.wifiextract.app.data.InstalledVersion
import com.wifiextract.app.data.ReleaseInfo
import com.wifiextract.app.data.SettingsRepository
import com.wifiextract.app.data.UpdateRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.UnknownHostException

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class UpToDate(val latest: ReleaseInfo?) : UpdateUiState
    data class Available(val info: ReleaseInfo) : UpdateUiState
    /** [percent] is null when the size is unknown. */
    data class Downloading(val info: ReleaseInfo, val percent: Int?) : UpdateUiState
    data class Ready(val info: ReleaseInfo, val file: File) : UpdateUiState
    data class Failed(val message: String, val retry: ReleaseInfo? = null) : UpdateUiState
}

class UpdatesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UpdateRepository(application)
    private val settings = SettingsRepository(application)
    val installed: InstalledVersion = repository.installedVersion()

    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        if (settings.checkUpdatesOnStart) check()
    }

    fun check() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.value = UpdateUiState.Checking
            _state.value = try {
                val release = repository.fetchLatest()
                if (VersionComparator.isNewer(release.tag, installed.name)) UpdateUiState.Available(release)
                else UpdateUiState.UpToDate(release)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UpdateUiState.Failed(friendly(e))
            }
        }
    }

    fun download(info: ReleaseInfo) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.value = UpdateUiState.Downloading(info, null)
            try {
                val file = repository.download(info) { pct -> _state.value = UpdateUiState.Downloading(info, pct) }
                _state.value = UpdateUiState.Ready(info, file)
            } catch (e: CancellationException) {
                _state.value = UpdateUiState.Available(info)
                throw e
            } catch (e: SecurityException) {
                _state.value = UpdateUiState.Failed(
                    "Verificación de seguridad fallida: ${e.message}. El archivo se descartó y no se instalará.", info
                )
            } catch (e: Exception) {
                _state.value = UpdateUiState.Failed(friendly(e), info)
            }
        }
    }

    fun cancelDownload() {
        job?.cancel()
    }

    /** Hands the verified APK to the system installer, which always asks the user to confirm. */
    fun install(context: Context) {
        val ready = _state.value as? UpdateUiState.Ready ?: return
        if (!repository.canInstall()) {
            Toast.makeText(context, "Permite instalar apps desde MNA y vuelve a pulsar «Instalar».", Toast.LENGTH_LONG).show()
            context.startActivity(repository.permissionIntent())
        } else {
            context.startActivity(repository.installIntent(ready.file))
        }
    }

    private fun friendly(e: Exception): String = when {
        e is UnknownHostException -> "Sin conexión con GitHub. Revisa tu red e inténtalo de nuevo."
        e is IOException && e.message?.contains("HTTP 404") == true -> "Todavía no hay versiones publicadas."
        e is IOException && e.message?.contains("HTTP 403") == true -> "GitHub limitó las peticiones. Inténtalo más tarde."
        e is SecurityException -> "Verificación de seguridad fallida: ${e.message}."
        else -> "No se pudo completar la operación. Inténtalo de nuevo."
    }
}
