package com.paulinaaniola.videochat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulinaaniola.videochat.domain.VideoChatEvent
import com.paulinaaniola.videochat.domain.VideoChatFacade
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoChatViewModel @Inject constructor(
    private val repository: VideoChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoChatUiState>(VideoChatUiState.CheckingPersmissions)
    val uiState: StateFlow<VideoChatUiState> = _uiState

    private var connectJob: Job? = null

    fun onPermissionsChecking() {
        _uiState.update { VideoChatUiState.CheckingPersmissions }
    }

    fun onPermissionsDenied() {
        _uiState.update { VideoChatUiState.PermissionsDenied }
    }

    fun initializeSession() {
        connectJob?.cancel()
        _uiState.update { VideoChatUiState.Connecting }

        val newCall = repository.initializeSession()
        connectJob = newCall.connect()
            .onEach { event ->
                when (event) {
                    VideoChatEvent.Connected -> {
                        _uiState.update { VideoChatUiState.Connected(call = newCall) }
                    }
                    else -> {
                        // implement
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun currentCall(): VideoChatFacade? =
        (uiState.value as? VideoChatUiState.Connected)?.call
}
