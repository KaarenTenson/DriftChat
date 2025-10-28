package com.app.driftchat.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.driftchat.ui.components.MatchCam
import com.app.driftchat.ui.components.MessageBox
import com.app.driftchat.ui.components.UserCam
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
@Composable
fun ChatRoom(onSwipeRight: () -> Unit, chatViewModel: ChatViewModel = viewModel(), userViewModel: UserDataViewModel) {
    // screen
    val userData = userViewModel.data.collectAsState().value

    LaunchedEffect(Unit) {
        chatViewModel.addUserToWaitList(userData)
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

        // cameras
        MatchCam()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            UserCam()
        }

        // Messages display
        var listTopPos by remember { mutableFloatStateOf(0f) }
        var listHeight by remember { mutableFloatStateOf(0f) }

        LazyColumn(
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp, top = 660.dp, start = 35.dp, end = 20.dp)
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
}