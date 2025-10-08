package com.app.driftchat.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.driftchat.R
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme

@Composable
fun UserAboutScreen(viewModel: UserDataViewModel,onSwipeLeft: () -> Unit) {
    val userData = viewModel.data.collectAsState().value

    Scaffold {
        padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            if (totalDrag < -50) onSwipeLeft()
                            totalDrag = 0f
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))

            //name
            Text(
                text = userData?.name ?: "Unknown User",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            //desc
            Text(
                text = userData?.description ?: "Unknown Desc",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 32.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Hobbies:",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Use a safe call to execute this loop only if hobbies is not null
                userData?.hobbies?.let { hobbies ->
                    hobbies.forEach { hobby ->
                        AssistChip(
                            onClick = {},
                            label = { Text(hobby) },
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }

            }


        }
    }
}