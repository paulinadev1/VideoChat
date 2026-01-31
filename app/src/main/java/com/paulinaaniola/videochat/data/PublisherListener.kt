package com.paulinaaniola.videochat.data

import android.util.Log
import com.opentok.android.OpentokError
import com.opentok.android.PublisherKit
import com.opentok.android.Stream
import javax.inject.Inject

class PublisherListener @Inject constructor() : PublisherKit.PublisherListener {
    companion object {
        private const val TAG = "TestVideoApp"
    }

    override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
        Log.d(TAG, "Publisher stream created: ${stream.streamId}")
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
        Log.d(TAG, "Publisher stream destroyed: ${stream.streamId}")
    }

    override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
        Log.e(TAG, "Publisher error: ${opentokError.message}")
    }
}