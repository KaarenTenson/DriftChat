package com.app.driftchat.ui.viewmodels

import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    val messages = mutableListOf<String>()

    fun sendMessage(message: String) {
        messages.add(message)
    }
}