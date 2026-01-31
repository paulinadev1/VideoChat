package com.paulinaaniola.videochat.domain

import android.view.View
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VideoChatFacade {
    val publisherView: StateFlow<View?>
    val subscriberView: StateFlow<View?>
    val isPublisherMuted: StateFlow<Boolean>
    val isPublisherCameraEnabled: StateFlow<Boolean>

    fun connect(): Flow<VideoChatEvent>
    fun endSession()
    fun toggleMicrophone()
    fun toggleCamera()
}

sealed interface VideoChatEvent {
    data object Connected : VideoChatEvent
    data object Disconnected : VideoChatEvent
    data class Error(val message: String) : VideoChatEvent
}
