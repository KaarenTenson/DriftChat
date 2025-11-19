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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import com.app.driftchat.ui.components.VideoView

@Composable
fun ChatRoom(onSwipeRight: () -> Unit, chatViewModel: ChatViewModel, userViewModel: UserDataViewModel) {
    // screen
    val userData = userViewModel.data.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(userData?.id) {
        chatViewModel.cleanMessages()
        chatViewModel.addUserToWaitList(userData)
        chatViewModel.waitForUserID()
        Log.d("web","siin")
        chatViewModel.initWebRTC(context, userData?.name ?: "User")
    }




    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 50) onSwipeRight()
            }
        }
    ) {
        //Remote video
        chatViewModel.remoteVideoTrack.value?.let { remoteTrack ->
            VideoView(
                context = context,
                videoTrack = remoteTrack,
                modifier = Modifier.fillMaxSize()
            )
        }

        //Local video

        chatViewModel.localVideoTrack.value?.let { localTrack ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 120.dp, height = 160.dp)
                    .padding(8.dp)
            ) {
                VideoView(
                    context = context,
                    videoTrack = localTrack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        //Messages display
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