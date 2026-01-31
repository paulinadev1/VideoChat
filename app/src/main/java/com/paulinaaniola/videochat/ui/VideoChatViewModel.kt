package com.paulinaaniola.videochat.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class VideoChatViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<VideoChatUiState>(VideoChatUiState.CheckingPersmissions)
    val uiState: StateFlow<VideoChatUiState> = _uiState

}
