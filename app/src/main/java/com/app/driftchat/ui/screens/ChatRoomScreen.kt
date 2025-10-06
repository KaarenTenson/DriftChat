package com.app.driftchat.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.driftchat.ui.components.MatchCam
import com.app.driftchat.ui.components.MessageBox
import com.app.driftchat.ui.components.UserCam
import com.app.driftchat.ui.viewmodels.ChatViewModel

@Composable
fun ChatRoom(onSwipeRight: () -> Unit, chatViewModel: ChatViewModel = viewModel()) {
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 50) {
                    onSwipeRight()
                }
            }
        },) {
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        MatchCam()
        UserCam(offsetX = offsetX, offsetY = offsetY, onDrag = { dragAmount ->
            offsetX += dragAmount.x
            offsetY += dragAmount.y
        })

        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 40.dp, start = 40.dp, end = 40.dp)
        ) {
            MessageBox(
                onSendMessage = { Text -> chatViewModel.sendMessage(Text) },
            )
        }
    }
}