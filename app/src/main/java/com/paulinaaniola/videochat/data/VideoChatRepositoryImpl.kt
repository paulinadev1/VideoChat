package com.paulinaaniola.videochat.data

import android.content.Context
import android.util.Log
import android.view.View
import com.opentok.android.BaseVideoRenderer
import com.opentok.android.OpentokError
import com.opentok.android.Publisher
import com.opentok.android.Session
import com.opentok.android.Stream
import com.opentok.android.Subscriber
import com.paulinaaniola.videochat.domain.VideoChatEvent
import com.paulinaaniola.videochat.domain.VideoChatFacade
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class VideoChatRepositoryImpl @Inject constructor(
    private val config: VonageVideoConfig,
    @ApplicationContext private val appContext: Context,
    val publisherListener: PublisherListener,
    val subscriberListener: SubscriberListener
) : VideoChatRepository {


    override fun initializeSession(): VideoChatFacade {
        return VonageVideoChat(
            appContext,
            config.appId,
            config.sessionId,
            config.token,
            publisherListener,
            subscriberListener
        )
    }
}

private class VonageVideoChat(
    private val appContext: Context,
    private val appId: String,
    private val sessionId: String,
    private val token: String,
    private val publisherListener: PublisherListener,
    private val subscriberListener: SubscriberListener,
) : VideoChatFacade {

    companion object Companion {
        private const val TAG = "TestVideoApp"
    }

    private val _publisherView = MutableStateFlow<View?>(null)
    override val publisherView: StateFlow<View?> = _publisherView

    private val _subscriberView = MutableStateFlow<View?>(null)
    override val subscriberView: StateFlow<View?> = _subscriberView

    private val _isPublisherMuted = MutableStateFlow(false)
    override val isPublisherMuted: StateFlow<Boolean> = _isPublisherMuted

    private val _isPublisherCameraEnabled = MutableStateFlow(true)
    override val isPublisherCameraEnabled: StateFlow<Boolean> = _isPublisherCameraEnabled


    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null

    override fun connect(): Flow<VideoChatEvent> = callbackFlow {
        val session = Session.Builder(appContext, appId, sessionId).build()
        this@VonageVideoChat.session = session

        session.setSessionListener(object : Session.SessionListener {
            override fun onConnected(session: Session) {
                Log.d(TAG, "Connected to session: ${session.sessionId}")
                publisher = Publisher.Builder(appContext).build().apply {
                    setPublisherListener(publisherListener)
                    renderer.setStyle(
                        BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL
                    )
                }
                publisher?.let { session.publish(it) }
                updatePublisherState()
                trySend(VideoChatEvent.Connected)
            }

            override fun onDisconnected(session: Session) {
                Log.d(TAG, "Disconnected from session: ${session.sessionId}")
                trySend(VideoChatEvent.Disconnected)
            }

            override fun onStreamReceived(session: Session, stream: Stream) {
                Log.d(TAG, "Stream received: ${stream.streamId}")
                if (subscriber == null) {
                    subscriber = Subscriber.Builder(appContext, stream).build().apply {
                        renderer.setStyle(
                            BaseVideoRenderer.STYLE_VIDEO_SCALE,
                            BaseVideoRenderer.STYLE_VIDEO_FILL
                        )
                        setSubscriberListener(subscriberListener)
                    }
                    subscriber?.let { session.subscribe(it) }
                    updateSubscriberState()
                }
            }

            override fun onStreamDropped(session: Session, stream: Stream) {
                Log.i(TAG, "Stream dropped: ${stream.streamId}")
                updateSubscriberState()
            }

            override fun onError(session: Session, opentokError: OpentokError) {
                Log.e(TAG, "Session error: ${opentokError.message}")
                trySend(VideoChatEvent.Error(opentokError.message ?: "Unknown session error"))
            }
        })

        session.connect(token)
        awaitClose()
    }

    private fun updatePublisherState() {
        _publisherView.value = publisher?.view
    }

    private fun updateSubscriberState() {
        _subscriberView.value = subscriber?.view
    }

    override fun toggleMicrophone() {
        val currentPublisher = publisher ?: return
        currentPublisher.publishAudio = !currentPublisher.publishAudio
        _isPublisherMuted.value = currentPublisher.publishAudio == false
    }

    override fun toggleCamera() {
        val currentPublisher = publisher ?: return
        currentPublisher.publishVideo = !currentPublisher.publishVideo
        _isPublisherCameraEnabled.value = currentPublisher.publishVideo != false
    }

    override fun endSession() {
        session?.disconnect()
        clearSession()
    }

    private fun clearSession() {
        session = null
        publisher = null
        subscriber = null
        _publisherView.value = null
        _subscriberView.value = null
        _isPublisherMuted.value = false
        _isPublisherCameraEnabled.value = true
    }
}
