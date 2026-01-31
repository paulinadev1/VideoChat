package com.paulinaaniola.videochat.domain

import kotlinx.coroutines.flow.Flow

interface VideoChatFacade {
    fun connect(): Flow<VideoChatEvent>
}

sealed interface VideoChatEvent {
    data object Connected : VideoChatEvent
    data object Disconnected : VideoChatEvent
    data class Error(val message: String) : VideoChatEvent
}
