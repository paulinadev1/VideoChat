package com.paulinaaniola.videochat.domain.repository

import com.paulinaaniola.videochat.domain.VideoChatFacade


interface VideoChatRepository {
    fun initializeSession(): VideoChatFacade
}
