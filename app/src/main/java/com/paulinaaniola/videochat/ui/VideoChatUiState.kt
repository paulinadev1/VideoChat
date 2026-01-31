package com.paulinaaniola.videochat.ui

sealed class VideoChatUiState {
    data object CheckingPersmissions : VideoChatUiState()
}