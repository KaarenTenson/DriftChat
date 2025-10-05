package com.app.driftchat.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.app.driftchat.domainmodel.UserData
import com.app.driftchat.ui.components.MatchCam
import com.app.driftchat.ui.components.UserCam

@Composable
fun ChatRoom(onSwipeRight: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 50) {
                    onSwipeRight()
                }
            }
        },) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        MatchCam()
        UserCam(offsetX = offsetX, offsetY = offsetY, onDrag = { dragAmount ->
            offsetX += dragAmount.x
            offsetY += dragAmount.y
        })
    }
}