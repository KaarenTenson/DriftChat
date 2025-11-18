package com.app.driftchat.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun VideoView(
    context: Context,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier
) {
    // Create only once
    val eglBase = remember { EglBase.create() }

    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                init(eglBase.eglBaseContext, null)
                setMirror(true)

                videoTrack?.addSink(this)
            }
        },
        update = { view ->
            // If track changes, update sink safely
            videoTrack?.addSink(view)
        },
        modifier = modifier,
        onRelease = { view ->
            videoTrack?.removeSink(view)
            view.release()
        }
    )
}
