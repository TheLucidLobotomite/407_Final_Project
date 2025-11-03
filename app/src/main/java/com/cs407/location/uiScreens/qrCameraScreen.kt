package com.cs407.location.uiScreens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.location.viewModels.qrViewModel

@Composable

fun qrCameraScreen(viewModel: qrViewModel = viewModel(),
        onQrScanned: (String) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current


    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCamPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCamPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(hasCamPermission) {
        if (hasCamPermission) viewModel.setUpCamera(context)
    }

    val controller: LifecycleCameraController? = viewModel.cameraControl

    val qr by viewModel.qrResult.collectAsStateWithLifecycle()

    LaunchedEffect(qr) {
        qr?.let {
            value ->
            Toast.makeText(context, "QR: $value", Toast.LENGTH_SHORT).show()
            onQrScanned(value)
        }
    }


    if (controller != null) {
        // We only need frame analysis for QR scanning
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)


        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { pv -> pv.controller = controller },
            modifier = Modifier.fillMaxSize()
        )

//        AndroidView(
//            factory = { contxt ->
//                PreviewView(contxt).apply {
//                    this.controller = controller        // shows the camera feed
//                    scaleType = PreviewView.ScaleType.FILL_CENTER
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )

        DisposableEffect(lifecycleOwner, controller) {
            controller.bindToLifecycle(lifecycleOwner)
            onDispose { controller.unbind() }
        }
    }





}