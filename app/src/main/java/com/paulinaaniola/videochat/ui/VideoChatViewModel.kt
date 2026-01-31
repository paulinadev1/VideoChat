package com.paulinaaniola.videochat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulinaaniola.videochat.domain.VideoChatEvent
import com.paulinaaniola.videochat.domain.VideoChatFacade
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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

    private val _viewEvents = MutableSharedFlow<VideoChatViewEvent>()
    val viewEvents: SharedFlow<VideoChatViewEvent> = _viewEvents

    private var connectJob: Job? = null

    fun onPermissionsChecking() {
        _uiState.update { VideoChatUiState.CheckingPersmissions }
    }

    fun onPermissionsDenied() {
        _uiState.update { VideoChatUiState.PermissionsDenied }
    }

    fun initializeSession() {
        connectJob?.cancel()
        currentCall()?.endSession()
        _uiState.update { VideoChatUiState.Connecting }

        val newCall = repository.initializeSession()
        connectJob = newCall.connect()
            .onEach { event ->
                when (event) {
                    VideoChatEvent.Connected -> {
                        _uiState.update { VideoChatUiState.Connected(call = newCall) }
                    }
                    is VideoChatEvent.ParticipantLeftChat -> {
                        viewModelScope.launch {
                            _viewEvents.emit(VideoChatViewEvent.SubscriberLeft)
                        }
                    }
                    is VideoChatEvent.Error -> {
                        viewModelScope.launch {
                            _viewEvents.emit(VideoChatViewEvent.Error(event.message))
                        }
                    }
                    else -> {
                        // implement
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPause() {
        currentCall()?.pauseSession()
    }

    fun onResume() {
        currentCall()?.resumeSession()
    }


    fun onAudioToggleClick() {
        currentCall()?.toggleMicrophone()
    }

    fun onVideoToggleClick() {
        currentCall()?.toggleCamera()
    }

    fun leaveChat() {
        currentCall()?.endSession()
    }

    private fun currentCall(): VideoChatFacade? =
        (uiState.value as? VideoChatUiState.Connected)?.call
}

sealed class VideoChatViewEvent(open val message: String) {
    data object SubscriberLeft : VideoChatViewEvent("The other participant left the chat")
    data class Error(override val message: String) : VideoChatViewEvent(message)
}
