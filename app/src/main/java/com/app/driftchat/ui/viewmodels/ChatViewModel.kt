package com.app.driftchat.ui.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore("messages12")

val messageMap = hashMapOf(
    "id" to 1,
    "name" to "Andrus",
    "Message" to "Hello"
)

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
        db.collection("messages")
            .add(messageMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}