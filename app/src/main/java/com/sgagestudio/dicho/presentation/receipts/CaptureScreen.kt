package com.sgagestudio.dicho.presentation.receipts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sgagestudio.dicho.data.local.storage.ReceiptImageStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

@Composable
fun CaptureScreen(
    onClose: () -> Unit,
    onOpenQueue: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val imageStorage = remember { ReceiptImageStorage(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            bindCamera(context, lifecycleOwner, previewView, imageCapture)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Capturar factura") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenQueue) {
                        Icon(imageVector = Icons.Filled.List, contentDescription = "Ver cola")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasPermission) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )
                FloatingActionButton(
                    onClick = {
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            imageStorage = imageStorage,
                            onSaved = { uri -> onPhotoCaptured(uri.toString()) },
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                ) {
                    Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Disparar")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Necesitamos permiso de cámara para capturar facturas.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    imageStorage: ReceiptImageStorage,
    onSaved: (Uri) -> Unit,
) {
    val file: File = imageStorage.createImageFile()
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSaved(Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) = Unit
        },
    )
}

private suspend fun bindCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
) {
    val cameraProvider = context.getCameraProvider()
    val preview = Preview.Builder().build().apply {
        setSurfaceProvider(previewView.surfaceProvider)
    }
    val selector = CameraSelector.DEFAULT_BACK_CAMERA
    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            { continuation.resume(future.get()) },
            ContextCompat.getMainExecutor(this),
        )
    }
}
