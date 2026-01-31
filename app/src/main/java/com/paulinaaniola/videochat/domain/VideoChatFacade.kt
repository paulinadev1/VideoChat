package com.paulinaaniola.videochat.domain

import android.view.View
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VideoChatFacade {
    fun connect(): Flow<VideoChatEvent>
    val publisherView: StateFlow<View?>
    val subscriberView: StateFlow<View?>
}

sealed interface VideoChatEvent {
    data object Connected : VideoChatEvent
    data object Disconnected : VideoChatEvent
    data class Error(val message: String) : VideoChatEvent
}
