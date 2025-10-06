package com.app.driftchat.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<String>()

    init {
        messages.add("")
        messages.add("")
        messages.add("Welcome to the chat!")
    }

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            messages.add(message.trim())
        }
    }
}