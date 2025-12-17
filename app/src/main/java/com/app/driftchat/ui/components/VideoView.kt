package com.app.driftchat.ui.components

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.app.driftchat.client.EglBaseProvider
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack


@Composable
fun VideoView(
    context: Context,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    overlay: Boolean = false
) {
    val eglBase = remember { EglBaseProvider.eglBase }

    // FIX: Use explicit state object
    val attachedTrack = remember { mutableStateOf<VideoTrack?>(null) }

    val renderer = remember { SurfaceViewRenderer(context) }

    AndroidView(
        modifier = modifier,
        factory = {
            renderer.apply {
                init(eglBase.eglBaseContext, null)
                setEnableHardwareScaler(true)
                setMirror(true)
                if (overlay) {
                    setZOrderMediaOverlay(true)
                    setZOrderOnTop(true)
                }
            }
        },
        update = { view ->

            // Detach old track
            if (attachedTrack.value != null && attachedTrack.value !== videoTrack) {
                attachedTrack.value?.removeSink(view)
            }

            // Attach new track
            videoTrack?.let { track ->
                track.setEnabled(true)
                track.addSink(view)
                attachedTrack.value = track
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            attachedTrack.value?.removeSink(renderer)
            renderer.release()
        }
    }
}