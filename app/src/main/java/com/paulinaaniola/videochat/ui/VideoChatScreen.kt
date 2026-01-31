package com.paulinaaniola.videochat.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoChatScreen(
    viewModel: VideoChatViewModel
) {
    val viewState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    val hasRequestedPermissions = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onPermissionsChecking()
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.initializeSession()
        } else {
            viewModel.onPermissionsDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermissions.value && !permissionsState.allPermissionsGranted) {
            hasRequestedPermissions.value = true
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(WindowInsets(0, 0, 0, 0))
        ) {
            when (val state = viewState.value) {
                is VideoChatUiState.CheckingPersmissions -> ConnectingLoading(
                    title = "Checking permissions",
                    message = "Preparing video chat access."
                )

                is VideoChatUiState.PermissionsDenied -> PermissionsDeniedScreen(
                    onGrantPermissionsClick = {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                )

                is VideoChatUiState.Connecting -> ConnectingLoading()
                is VideoChatUiState.Connected -> {
                    val call = state.call
                    if (call == null) {
                        ConnectingLoading()
                    } else {
                        val publisherView = call.publisherView.collectAsStateWithLifecycle().value
                        val subscriberView = call.subscriberView.collectAsStateWithLifecycle().value
                        val isMuted = call.isPublisherMuted.collectAsStateWithLifecycle().value
                        val isVideoEnabled = call.isPublisherCameraEnabled.collectAsStateWithLifecycle().value
                        ConnectedScreen(publisherView,
                            subscriberView,
                            isMuted,
                            isVideoEnabled,
                            { viewModel.onAudioToggleClick() },
                            { viewModel.onVideoToggleClick() },
                            { viewModel.leaveChat() })
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectingLoading(
    modifier: Modifier = Modifier,
    title: String = "Connecting to a session",
    message: String = "Stay tuned - this can take a few seconds."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PermissionsDeniedScreen(
    onGrantPermissionsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera and Audio permissions are required",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onGrantPermissionsClick,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text("Grant Permissions")
        }
    }
}


@Composable
fun ConnectedScreen(
    publisherView: android.view.View?,
    subscriberView: android.view.View?,
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    onAudioToggleClick: () -> Unit,
    onVideoToggleClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    Box() {
        Column(modifier = Modifier.fillMaxSize()) {
            subscriberView?.let {
                AndroidView(
                    factory = { subscriberView },
                    modifier = Modifier.weight(1f)
                )

                publisherView?.let { view ->
                    AndroidView(
                        factory = { view },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        ActionBar(
            isMuted = isMuted,
            isVideoEnabled = isVideoEnabled,
            onAudioToggleClick = onAudioToggleClick,
            onVideoToggleClick = onVideoToggleClick,
            onLeaveClick = onLeaveClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun ActionBar(
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    onAudioToggleClick: () -> Unit,
    onVideoToggleClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(top = 20.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAudioToggleClick, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = if (isMuted) "Unmute microphone" else "Mute microphone",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(onClick = onVideoToggleClick, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = if (isVideoEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = if (isVideoEnabled) "Turn off camera" else "Turn on camera",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(onClick = onLeaveClick, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Leave chat",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
