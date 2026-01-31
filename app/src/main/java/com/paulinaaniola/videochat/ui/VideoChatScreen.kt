package com.paulinaaniola.videochat.ui

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoChatScreen(
    viewModel: VideoChatViewModel
) {
    val viewState = viewModel.uiState.collectAsStateWithLifecycle()

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
            // TODO: implement
        } else {
            // TODO: implement
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermissions.value && !permissionsState.allPermissionsGranted) {
            hasRequestedPermissions.value = true
            permissionsState.launchMultiplePermissionRequest()
        }
    }

}
