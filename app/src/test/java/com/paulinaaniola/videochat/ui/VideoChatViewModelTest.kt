package com.paulinaaniola.videochat.ui

import app.cash.turbine.test
import com.paulinaaniola.videochat.MainDispatcherExtension
import com.paulinaaniola.videochat.domain.VideoChatEvent
import com.paulinaaniola.videochat.domain.VideoChatFacade
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class VideoChatViewModelTest {

    private val repository: VideoChatRepository = mockk()
    private val videoChatFacade: VideoChatFacade = mockk(relaxed = true)
    private lateinit var viewModel: VideoChatViewModel

    private val sdkEventFlow = MutableSharedFlow<VideoChatEvent>()

    @BeforeEach
    fun setup() {
        every { repository.initializeSession() } returns videoChatFacade
        every { videoChatFacade.connect() } returns sdkEventFlow
        viewModel = VideoChatViewModel(repository)
    }

    @Test
    fun `initializeSession transitions state to Connecting immediately`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.initializeSession()

            val uiState = awaitItem()
            assertEquals(VideoChatUiState.Connecting, uiState)
        }
    }

    @Test
    fun `initializeSession transitions state to Connected when SDK emits Connected event`() = runTest {
        viewModel.uiState.test {
            viewModel.initializeSession()
            skipItems(2)

            sdkEventFlow.emit(VideoChatEvent.Connected)

            val uiState = awaitItem()
            assertEquals(VideoChatUiState.Connected(videoChatFacade), uiState)
        }
    }

    @Test
    fun `initializeSession transitions state to Disconnected when SDK emits Disconnected event`() = runTest {
        viewModel.uiState.test {
            viewModel.initializeSession()
            skipItems(2)

            sdkEventFlow.emit(VideoChatEvent.Disconnected)

            val uiState = awaitItem()
            assertEquals(VideoChatUiState.Disconnected, uiState)
        }
    }

    @Test
    fun `initializeSession emits Error ViewEvent when SDK emits Error`() = runTest {
        viewModel.initializeSession()

        viewModel.viewEvents.test {
            val errorMsg = "Network Failure"
            sdkEventFlow.emit(VideoChatEvent.Error(errorMsg))

            val uiState = awaitItem()
            assertEquals(VideoChatViewEvent.Error(errorMsg), uiState)
        }
    }

    @Test
    fun `initializeSession emits SubscriberLeft ViewEvent when SDK emits ParticipantLeftChat`() = runTest {
        viewModel.initializeSession()

        viewModel.viewEvents.test {
            sdkEventFlow.emit(VideoChatEvent.ParticipantLeftChat)

            val viewEvent = awaitItem()
            assertEquals(VideoChatViewEvent.SubscriberLeft, viewEvent)
        }
    }

    @Test
    fun `initializeSession ends previous session if one exists`() = runTest {
        val oldFacade: VideoChatFacade = mockk(relaxed = true)
        val oldEvents = MutableSharedFlow<VideoChatEvent>()

        every { repository.initializeSession() } returns oldFacade
        every { oldFacade.connect() } returns oldEvents

        viewModel.initializeSession()
        oldEvents.emit(VideoChatEvent.Connected)

        val newFacade: VideoChatFacade = mockk(relaxed = true)
        every { repository.initializeSession() } returns newFacade
        every { newFacade.connect() } returns MutableSharedFlow()

        viewModel.initializeSession()

        verify(exactly = 1) { oldFacade.endSession() }
    }
}
