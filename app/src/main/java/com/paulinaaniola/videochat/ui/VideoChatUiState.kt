package com.paulinaaniola.videochat.ui

import com.paulinaaniola.videochat.domain.VideoChatFacade

sealed class VideoChatUiState {
    data object CheckingPersmissions : VideoChatUiState()
    data object PermissionsDenied : VideoChatUiState()
    data object Connecting : VideoChatUiState()
    data class Connected(
        val call: VideoChatFacade? = null,
    ) : VideoChatUiState()
}