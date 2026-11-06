package com.wifiextract.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NetworkUiState {
    data object Loading : NetworkUiState
    data class Success(val info: NetworkInfo) : NetworkUiState
    data class Error(val message: String) : NetworkUiState
}

class NetworkOverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkInfoRepository(application)
    private val _state = MutableStateFlow<NetworkUiState>(NetworkUiState.Loading)
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.observeNetworkInfo().collect { info ->
                _state.value = NetworkUiState.Success(info)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.read() }
                .onSuccess { _state.value = NetworkUiState.Success(it) }
                .onFailure {
                    _state.value = NetworkUiState.Error(
                        "No se pudo leer el estado de la conexión. Comprueba que los servicios de red estén disponibles e inténtalo de nuevo."
                    )
                }
        }
    }
}
