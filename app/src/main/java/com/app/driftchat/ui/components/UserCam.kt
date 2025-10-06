package com.app.driftchat.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun UserCam() {
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
            Text(text = "You")
        }
    //}
}
