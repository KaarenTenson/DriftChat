package com.app.driftchat.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.getValue
import com.app.driftchat.ui.viewmodels.ThemeViewModel

@Composable
fun UserAboutScreen(
    viewModel: UserDataViewModel,
    themeViewModel: ThemeViewModel,
    onSwipeLeft: () -> Unit
) {
    val userData = viewModel.data.collectAsState().value
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { padding ->
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

                Text(
                    text = userData?.name ?: "Unknown User",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userData?.description ?: "Unknown Desc",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Hobbies:",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
        IconButton(
            onClick = { themeViewModel.toggleTheme() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 50.dp, start = 10.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
