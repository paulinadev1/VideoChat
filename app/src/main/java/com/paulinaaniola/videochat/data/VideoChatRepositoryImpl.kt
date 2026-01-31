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

    /**
     * Builds a new video chat facade using the current Vonage configuration.
     */
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

/**
 *  Wrapper around the Vonage SDK that exposes a simple facade for the UI.
 *
 * @param appContext Application context used to build SDK sessions and streams.
 * @param appId Vonage application identifier for the SDK session.
 * @param sessionId Vonage session identifier to join.
 * @param token Auth token used to connect to the session.
 * @param publisherListener Listener for local publisher lifecycle callbacks.
 * @param subscriberListener Listener for remote subscriber lifecycle callbacks.
 */
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
    /**
     * Emits the current local publisher view for display.
     */
    override val publisherView: StateFlow<View?> = _publisherView

    private val _subscriberView = MutableStateFlow<View?>(null)
    /**
     * Emits the current remote subscriber view for display.
     */
    override val subscriberView: StateFlow<View?> = _subscriberView

    private val _isPublisherMuted = MutableStateFlow(false)
    /**
     * Emits whether the local publisher microphone is muted.
     */
    override val isPublisherMuted: StateFlow<Boolean> = _isPublisherMuted

    private val _isPublisherCameraEnabled = MutableStateFlow(true)
    /**
     * Emits whether the local publisher camera is enabled.
     */
    override val isPublisherCameraEnabled: StateFlow<Boolean> = _isPublisherCameraEnabled


    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null

    /**
     * Bridges the SDK session lifecycle into a flow of domain events.
     */
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
                val wasSubscriber = subscriber != null
                val isSubscriberStream = subscriber?.stream?.streamId == stream.streamId
                subscriber = null

                updateSubscriberState()
                if (wasSubscriber && isSubscriberStream) {
                    trySend(VideoChatEvent.ParticipantLeftChat)
                }
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

    /**
     * Toggles the publisher microphone and updates the exposed state flow.
     */
    override fun toggleMicrophone() {
        val currentPublisher = publisher ?: return
        currentPublisher.publishAudio = !currentPublisher.publishAudio
        _isPublisherMuted.value = currentPublisher.publishAudio == false
    }

    /**
     * Toggles the publisher camera and updates the exposed state flow.
     */
    override fun toggleCamera() {
        val currentPublisher = publisher ?: return
        currentPublisher.publishVideo = !currentPublisher.publishVideo
        _isPublisherCameraEnabled.value = currentPublisher.publishVideo != false
    }

    /**
     * Pauses the SDK session to align with the host lifecycle.
     */
    override fun pauseSession() {
        session?.onPause()
    }

    /**
     * Resumes the SDK session after the host comes back to foreground.
     */
    override fun resumeSession() {
        session?.onResume()
    }

    override fun endSession() {
        session?.disconnect()
        clearSession()
    }

    /**
     * Resets local state after the SDK session ends.
     */
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
