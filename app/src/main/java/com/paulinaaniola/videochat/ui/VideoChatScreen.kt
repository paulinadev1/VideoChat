package com.paulinaaniola.videochat.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

                else -> {
                    // TODO: to implement
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
