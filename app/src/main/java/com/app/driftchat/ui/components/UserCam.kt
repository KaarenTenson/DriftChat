package com.app.driftchat.ui.components

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UserCam() {
    // camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // request camera permissions
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    // commented box should be the anchoredDraggable boundary

    //Box(
    //    modifier = Modifier
    //        .padding(top = 65.dp, start = 25.dp, end = 25.dp)
    //        .height(height = 160.dp)
    //        .fillMaxWidth()
    //        .border(width = 1.dp, Color.Blue)
    //) {
        Box(
            modifier = Modifier
                .padding(top = 65.dp, start = 25.dp, end = 25.dp) // comment this out if implementing anchoredDraggable
                .height(height = 160.dp) // comment this out if implementing anchoredDraggable
                //.fillMaxHeight()
                .aspectRatio(ratio = 3f / 4f) // 4:3 aspect ratio
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(10.dp))
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreview()
            } else {
                Text(text = "Camera permission not granted")
            }
        }
    //}
}

@Composable
private fun CameraPreview() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()

    )
}
