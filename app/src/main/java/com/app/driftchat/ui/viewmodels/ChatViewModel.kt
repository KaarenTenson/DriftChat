package com.app.driftchat.ui.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.app.driftchat.domainmodel.UserData

@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore("(default)")


val messageMap = hashMapOf<String, Any>(
    "id" to 0,
    "name" to "testkasutaja",
    "Message" to "test"
)

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<String>()
    private var userID: String? = null

    init {
        messages.add("")
        messages.add("")
        messages.add("Welcome to the chat!")
    }


    fun addUserToWaitList(userData: UserData?) {
        val waitListEntry = hashMapOf<String, Any>(
            "userId" to (userData?.id ?: 0),
            "createdAt" to (System.currentTimeMillis())
        )

        db.collection("waitList")
            .add(waitListEntry)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "User added to waitList with ID: ${documentReference.id}")
                userID=documentReference.id
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding to waitList", e)
            }
    }

    fun sendMessage(message: String, userData: UserData?) {      //Kui message tyhi pole motet database lisada
        if (message.isNotBlank()) {
            messages.add(message.trim())

            val messageMap = hashMapOf<String, Any>(
                "id" to (userID ?: 0),
                "name" to (userData?.name ?: "Unknown"),
                "Message" to message.trim()
            )

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
}