package com.paulinaaniola.videochat.data

import android.content.Context
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoChatRepositoryImpl @Inject constructor(
    private val config: VonageVideoConfig,
    @ApplicationContext private val appContext: Context,
) : VideoChatRepository {

}