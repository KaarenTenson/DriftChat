package com.app.driftchat.ui.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.app.driftchat.domainmodel.UserData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration


@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore("(default)")


val messageMap = hashMapOf<String, Any>(
    "id" to 0,
    "name" to "testkasutaja",
    "Message" to "test"
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    val messages = mutableStateListOf<String>()
    private var userID: String? = null
    private var roomID: String? = null
    private var timeSinceLast = 0L
    private var chatRoomListenerRegistration: ListenerRegistration? = null
    private var messageListenerRegistration: ListenerRegistration? = null



    init {
        messages.add("")
        messages.add("")
        messages.add("Welcome to the chat!")
    }

    fun startMessageListener() {
        val currentRoomID = roomID
        val currentUserID = userID

        if (currentRoomID.isNullOrBlank() || currentUserID.isNullOrBlank()) { //? lahti saamine
            Log.w(TAG, "Cannot start message listener: Room ID or User ID is missing.")
            return
        }

        messageListenerRegistration?.remove()

        messageListenerRegistration = db.collection("messages")
            .whereEqualTo("roomID", currentRoomID)
            .whereNotEqualTo("id", currentUserID)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Message listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val msgText = document.getString("Message")
                        val senderName = document.getString("name")
                        Log.d(TAG, "Recieved message ${document.id}")
                        messages.add("$senderName: $msgText")

                    }
                }
            }
    }
    fun startChatRoomListener() {
        val searchID = userID

        if (searchID.isNullOrBlank()) { //Selleks et String? lahti saada
            Log.w(TAG, "userID null or blank")
            return
        }
        chatRoomListenerRegistration?.remove()

        chatRoomListenerRegistration = db.collection("chatRooms")
            .whereArrayContains("members", searchID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Chat room listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        Log.d(TAG, "Found chat room: ${document.id}")
                        roomID = document.id
                        startMessageListener()
                        val members = document.get("members") as? List<*>
                        Log.d(TAG, "Room ${document.id} members: $members")

                    }
                } else {
                    Log.d(TAG, "No active chat rooms found for userId: $userID")
                }
            }
    }
    fun addUserToWaitList(userData: UserData?) {
        val now = System.currentTimeMillis()

        if (now-timeSinceLast<2000) {    //chatroomscreen composeb mitu korda aga koos hiltview + lisa checkiga saab ymber astuda mitu korda saatmisest
            Log.d(TAG, "WaitList addition skipped. Already started.")
            return
        }

        timeSinceLast = now

        val waitListEntry = hashMapOf<String, Any>(
            "userId" to (userData?.id ?: 0),
            "createdAt" to (System.currentTimeMillis())
        )

        db.collection("waitList")
            .add(waitListEntry)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "User added to waitList with ID: ${documentReference.id}")
                userID=documentReference.id
                startChatRoomListener()
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
                "Message" to message.trim(),  //message
                "roomID" to (roomID ?: 0)
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
    override fun onCleared() {
        super.onCleared()
        chatRoomListenerRegistration?.remove()
        messageListenerRegistration?.remove()
    }
}