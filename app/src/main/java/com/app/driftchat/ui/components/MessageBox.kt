package com.app.driftchat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun MessageBox(
    onSendMessage: (String) -> Unit) {
        var message by remember { mutableStateOf("") }
        val maxCharacters = 100 // Text field max characters

        OutlinedTextField(
            value = message,
            onValueChange = { newText ->
                if (newText.contains("\n")) {
                    onSendMessage(message)
                    message = ""
                } else if (newText.length <= maxCharacters) {
                    message = newText
                }
            },
            placeholder = { Text("Send message...") },
            textStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            maxLines = 2,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(50.dp))
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(50.dp))
        )
}