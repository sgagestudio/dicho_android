package com.sgagestudio.dicho.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.sgagestudio.dicho.data.camera.ReceiptImageStore

@Composable
fun CaptureScreen(
    onClose: () -> Unit,
    onPhotoCaptured: (android.net.Uri) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasPermission = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission.value = granted
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        hasPermission.value = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (hasPermission.value) {
            CameraPreview(controller = cameraController)
        } else {
            PermissionFallback(onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            })
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar cámara",
                tint = Color.White,
            )
        }

        FloatingActionButton(
            onClick = {
                if (!hasPermission.value) return@FloatingActionButton
                val photoFile = ReceiptImageStore.createTempImageFile(context)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                cameraController.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(
                            outputFileResults: ImageCapture.OutputFileResults,
                        ) {
                            onPhotoCaptured(photoFile.toUri())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CaptureScreen", "Error al guardar la foto.", exception)
                            ReceiptImageStore.deleteImageUri(context, photoFile.toUri())
                        }
                    },
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = "Tomar foto",
            )
        }
    }
}

@Composable
fun PreviewScreen(
    imageUri: android.net.Uri,
    onRetake: () -> Unit,
    onUse: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Previsualización del ticket",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = onRetake,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Reintentar")
            }
            Button(
                onClick = onUse,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Usar")
            }
        }
    }
}

@Composable
private fun CameraPreview(controller: LifecycleCameraController) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun PermissionFallback(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Necesitamos permiso de cámara para escanear el ticket.",
            color = Color.White,
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(text = "Conceder permiso")
        }
    }
}
