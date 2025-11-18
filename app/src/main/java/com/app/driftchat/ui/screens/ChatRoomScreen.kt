package com.app.driftchat.ui.screens

import android.util.Log
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.app.driftchat.ui.components.MatchCam
import com.app.driftchat.ui.components.MessageBox
import com.app.driftchat.ui.components.UserCam
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import androidx.compose.runtime.LaunchedEffect
import com.app.driftchat.client.ClientRtc
import androidx.compose.ui.platform.LocalContext
import com.app.driftchat.client.MyWebSocketSignaling
import androidx.compose.runtime.mutableStateOf
import org.webrtc.SurfaceViewRenderer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.size


@Composable
fun ChatRoom(onSwipeRight: () -> Unit, chatViewModel: ChatViewModel, userViewModel: UserDataViewModel) {
    // screen
    val userData = userViewModel.data.collectAsState().value
    val context = LocalContext.current

    var clientRtcInitialized by remember { mutableStateOf(false) }
    var clientRtc by remember { mutableStateOf<ClientRtc?>(null) }
    var localRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var remoteRenderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    val roomID by chatViewModel.roomID

    LaunchedEffect(userData?.id) {
        chatViewModel.cleanMessages()
        chatViewModel.addUserToWaitList(userData)
    }

    LaunchedEffect(roomID, localRenderer, remoteRenderer) {
        Log.d("kal", "roomID: $roomID, localRenderer: $localRenderer, remoteRenderer: $remoteRenderer")
        if (!clientRtcInitialized && roomID != null && localRenderer != null && remoteRenderer != null) {
            val signalingClient = MyWebSocketSignaling(roomID!!)
            signalingClient.onOpenCallback = { clientRtc?.createOffer() }

            clientRtc = ClientRtc(
                context = context,
                localRenderer = localRenderer!!,
                remoteRenderer = remoteRenderer!!,
                signalingClient = signalingClient
            )

            clientRtc?.init()
            clientRtcInitialized = true
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 50) {
                    onSwipeRight()
                }
            }
        },) {
        AndroidView(factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                init(org.webrtc.EglBase.create().eglBaseContext, null)
                setZOrderMediaOverlay(false)
                remoteRenderer = this
            }
        }, modifier = Modifier.fillMaxSize())
        // cameras
        AndroidView(factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                init(org.webrtc.EglBase.create().eglBaseContext, null)
                setZOrderMediaOverlay(true) // overlay so it’s above remote
                localRenderer = this
            }
        }, modifier = Modifier.size(120.dp).align(Alignment.TopEnd))

        // Messages display
        var listTopPos by remember { mutableFloatStateOf(0f) }
        var listHeight by remember { mutableFloatStateOf(0f) }

        LazyColumn(
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .align( Alignment.BottomEnd)
                .fillMaxHeight(0.4f)
                .fillMaxWidth()
                .padding(bottom = 110.dp, start = 35.dp, end = 20.dp)
                // onGloballyPositioned gives top-left coordinates (positionInWindow() and size
                .onGloballyPositioned { coordinates ->
                    listTopPos = coordinates.positionInWindow().y // top-left coordinates (x1, y2) -> y2
                    listHeight = coordinates.size.height.toFloat() // height = y2 - y1
                }
        ) {
            items(chatViewModel.messages.reversed()) { messageText ->
                var itemTopPos by remember { mutableFloatStateOf(0f) }
                // items position translated to alpha (opacity) - 1.0 at the top, 0.0 at the bottom
                val relativePosition = ((itemTopPos - listTopPos) / listHeight).coerceIn(0f, 1f)
                Text(
                    text = messageText,
                    modifier = Modifier
                        .padding(4.dp)
                        .onGloballyPositioned { coordinates ->
                            itemTopPos = coordinates.positionInWindow().y
                        }
                        .graphicsLayer {
                            // opacity
                            this.alpha = relativePosition
                        },
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // Send message box
        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 40.dp, start = 40.dp, end = 40.dp)
        ) {
            MessageBox(
                onSendMessage = { Text -> chatViewModel.sendMessage(Text,userData) },
            )
        }
    }
    if (!chatViewModel.errorMsg.value.isEmpty()) {
        ErrorBox(chatViewModel.errorMsg.value)
    } else {
        if (chatViewModel.isWaitingForOtherPerson.value) {
            LoadingBox();
        }
    }
}



@Composable
fun ErrorBox(errorMessage: String) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.95f },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Surface(
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier.padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage)
            }
        }
    }
}

@Composable
fun LoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .graphicsLayer { alpha = 0.95f },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
        Text(
            text = "Connecting...",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}