package com.app.driftchat.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
@Composable
fun VideoView(context: Context, videoTrack: VideoTrack?, modifier: Modifier = Modifier) {
    val eglBase = remember { EglBase.create() }
    val renderer = remember { SurfaceViewRenderer(context) }

    AndroidView(
        factory = { ctx ->
            renderer.apply {
                init(eglBase.eglBaseContext, null)
                setMirror(true)
                videoTrack?.addSink(this)  // attach immediately
            }
        },
        modifier = modifier
    )

    DisposableEffect(videoTrack) {
        onDispose {
            videoTrack?.removeSink(renderer)
        }
    }
}