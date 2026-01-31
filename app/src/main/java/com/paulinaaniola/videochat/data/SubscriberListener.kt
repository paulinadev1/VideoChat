package com.paulinaaniola.videochat.data

import android.util.Log
import com.opentok.android.OpentokError
import com.opentok.android.SubscriberKit
import javax.inject.Inject

class SubscriberListener @Inject constructor()  : SubscriberKit.SubscriberListener {
    companion object {
        private const val TAG = "TestVideoApp"
    }

    override fun onConnected(subscriberKit: SubscriberKit) {
        Log.d(TAG, "Subscriber connected: ${subscriberKit.stream?.streamId}")
    }

    override fun onDisconnected(subscriberKit: SubscriberKit) {
        Log.d(TAG, "Subscriber disconnected: ${subscriberKit.stream?.streamId}")
    }

    override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
        Log.e(TAG, "Subscriber error: ${opentokError.message}")
    }
}