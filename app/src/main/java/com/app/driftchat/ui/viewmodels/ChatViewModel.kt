package com.app.driftchat.ui.viewmodels

import android.R
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.app.driftchat.domainmodel.UserData
import androidx.lifecycle.viewModelScope
import com.app.driftchat.client.FirebaseSign
import com.app.driftchat.client.NSWebRTCClient
import com.app.driftchat.client.WebRtcRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import org.webrtc.VideoTrack
import android.os.Handler
import android.os.Looper
import com.app.driftchat.client.FirebaseSignaling
@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore("(default)")

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    val messages = mutableStateListOf<String>()
    private var firebaseSignal: FirebaseSignaling? = null
    var localVideoTrack = mutableStateOf<VideoTrack?>(null)
    var remoteVideoTrack = mutableStateOf<VideoTrack?>(null)
    //for showing user errors from firestore
    val errorMsg = mutableStateOf<String>("");
    //when user is waiting for connection from another user
    val isWaitingForOtherPerson = mutableStateOf<Boolean>(false);
    private var userID: String? = null
    private var roomID: String? = null
    private var timeSinceLast = 0L

    private var lastLeftChatCall = 0L
    var timeSinceLastRemoval: Long = 0L
    private var chatRoomListenerRegistration: ListenerRegistration? = null
    private var messageListenerRegistration: ListenerRegistration? = null

    private var leftChatListenerRegistration: ListenerRegistration? = null
    private var repo: WebRtcRepository? = null
    private var webRtcClient: NSWebRTCClient? = null
    private var webRtcInitialized = false
    init {
        messages.add("")
        messages.add("")
        messages.add("Welcome to the chat!")
        startRemoteTrackLogger();
    }

    fun initWebRTC(context: Context, username: String) {
        if (userID.isNullOrBlank()) {
            Log.w(TAG, "initWebRTC: userID is null - cannot init")
            return
        }

        if (webRtcInitialized) {
            Log.d(TAG, "initWebRTC: already initialized, skipping")
            return
        }

        Log.d("web", "initWebRTC start")

        if (firebaseSignal == null) {
            firebaseSignal = FirebaseSign(db, userID!!)
        }
        val signal = firebaseSignal!!

        if (webRtcClient == null) {
            webRtcInitialized = true
            webRtcClient = NSWebRTCClient(context, signal).apply {
                // Listen for remote track first
                setOnRemoteTrackListener { track ->
                    Handler(Looper.getMainLooper()).post {
                        Log.d("vid", "Remote VideoTrack received: id=${track.id()}, enabled=${track.enabled()}")
                        remoteVideoTrack.value = track
                    }
                }
                initWebrtcClient(username)

                val localTrack = getLocalVideoTrack()
                if (localTrack == null) {
                    Log.e("vid", "Local video track null after init!")
                } else {
                    Log.d("vid", "Local video ready: id=${localTrack.id()}")
                }
                localVideoTrack.value = localTrack
            }
        }

        repo = WebRtcRepository(signal, webRtcClient!!)
        repo?.init(username)

        Log.d(TAG, "initWebRTC: completed for user=$userID")
    }
    fun startMessageListener() {
        val currentRoomID = roomID
        val currentUserID = userID

        if (currentRoomID.isNullOrBlank() || currentUserID.isNullOrBlank()) { //? lahti saamine
            Log.w(TAG, "Cannot start message listener: Room ID or User ID is missing.")
            throw Exception("couldn't start message session");
        }

        messageListenerRegistration?.remove()

        messageListenerRegistration = db.collection("messages")
            .whereEqualTo("roomID", currentRoomID)
            .whereNotEqualTo("id", currentUserID)
            .limit(1)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    errorMsg.value = "failed to listen to other persons messages";
                    Log.e(TAG, "Message listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    for (change in snapshots.documentChanges) {
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                val document = change.document
                                val msgText = document.getString("Message")
                                val senderName = document.getString("name")
                                Log.d(TAG, "📩 New message received: ${document.id}")
                                messages.add("$senderName: $msgText")
                            }

                            else -> {}
                        }
                    }
                }
            }
    }
    fun startChatRoomListener(userData: UserData? = null) {
        val searchID = userID

        if (searchID.isNullOrBlank()) {
            Log.w(TAG, "userID null or blank")
            return
        }
        chatRoomListenerRegistration?.remove()

        chatRoomListenerRegistration = db.collection("chatRooms")
            .whereArrayContains("members", searchID)
            .limit(1)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMsg.value = "failed to connect to other person";
                    Log.e(TAG, "Chat room listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        setIsWaiting(false)
                        errorMsg.value = ""
                        roomID = document.id
                        startMessageListener()
                        startLeftChatListener(userData)

                        val members = document.get("members") as? List<*>
                        Log.d(TAG, "Found chat room: ${document.id}")
                        Log.d(TAG, "Room ${document.id} members: $members")
                        if (members != null && members.size == 2) {
                            val other = members.firstOrNull { it != searchID } as? String
                            if (!other.isNullOrBlank()) {
                                // only initiate call from one side (deterministic)
                                if (searchID < other) {
                                    Log.d(TAG, "startChatRoomListener: we are caller, initiating call to $other")
                                    repo?.startCall(other)
                                } else {
                                    Log.d(TAG, "startChatRoomListener: waiting for offer from $other")
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No active chat rooms found for userId: $userID")
                }
            }
    }

    fun startLeftChatListener(userData: UserData?) {//Starts a listener for collection Leftchat which consists of rooms that have 1 participant
        //If the user is in a room that was added to Leftchat collection they will be added back to the waitlist
        val currentRoomID = roomID
        val currentUserID = userID
        if (currentRoomID.isNullOrBlank()) {
            Log.w(TAG, "Cannot start LeftChat listener: Room ID missing.")
            return
        }


        leftChatListenerRegistration?.remove()

        leftChatListenerRegistration = db.collection("Leftchat")
            .whereEqualTo("roomID", currentRoomID)
            .limit(1)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "LeftChat listener failed: ${error.message}", error)
                    errorMsg.value = "Failed to listen for left chat updates"
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    for (doc in snapshots.documents) {
                        val leftBy = doc.get("leftBy")?.toString() ?: ""
                        val currentUserDataId = userID.toString()
                        if (leftBy.isNotBlank() && leftBy == currentUserDataId) {
                            Log.d(TAG, "Ignoring Leftchat event — triggered by current user ($leftBy).")
                            return@addSnapshotListener
                        }

                        Log.d(TAG, "Other user left the chat ($leftBy), re-adding current user to waitlist.")
                        errorMsg.value = "The other person left the chat."
                        setIsWaiting(false)

                        messageListenerRegistration?.remove()
                        reset();
                        addUserToWaitList(userData)

                        leftChatListenerRegistration?.remove()
                        break
                    }
                }
            }
    }

    suspend fun waitForUserID(): String {
        while (userID.isNullOrBlank()) {
            kotlinx.coroutines.delay(50) // small delay to avoid busy loop
        }
        return userID!!
    }

    private lateinit var nsWebRTCClient: NSWebRTCClient
    fun addUserToWaitList(userData: UserData?) {//Adds user to the waitlist so we can connect them  with another participant
        val now = System.currentTimeMillis()
        setIsWaiting(true)
        if (now-timeSinceLast<2000) {
            Log.d(TAG, "WaitList addition skipped. Already started.")
            return
        }

        timeSinceLast = now

        val waitListEntry = hashMapOf<String, Any>(
            "userId" to (userData?.id ?: ""),
            "createdAt" to (System.currentTimeMillis())
        )

        db.collection("waitList")
            .add(waitListEntry)
            .addOnSuccessListener { documentReference ->
                userID=documentReference.id
                Log.d(TAG, "User added to waitList with ID: ${documentReference.id}")
                startChatRoomListener(userData)
            }
            .addOnFailureListener { e ->
                setIsWaiting(false)
                errorMsg.value = "failed to connect to the server"
                Log.w(TAG, "Error adding to waitList", e)
            }

    }

    fun removeUserFromWaitList() {//Function that is meant to remove a user from the waitlist if they do not wait to be connected to another user
        val currentUserDocId = userID ?: return
        val now = System.currentTimeMillis()
        if (now - timeSinceLastRemoval < 2000) {
            Log.d(TAG, "WaitList removal skipped. Already removing...")
            return
        }
        db.collection("waitList")
            .document(currentUserDocId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Removed from waitList: $currentUserDocId")
                userID = null
                setIsWaiting(false)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove waitList entry", e)
            }
    }
    fun addUserToLeftChat(userData: UserData?) {//Adds room to Leftchat collection which consists of the roomID of the room user left from
        // userID that left the room and a timestamp.
        val currentRoomID = roomID
        val currentUserID = userID
        if (currentRoomID.isNullOrBlank() || isWaitingForOtherPerson.value) {
            Log.w(TAG, "Cannot add to Leftchat — roomID is null'/blank or user is not in chat'.")
            return
        }

        val now = System.currentTimeMillis()

        if (now - lastLeftChatCall < 2000) {
            Log.d(TAG, "Skipping duplicate Leftchat addition.")
            return
        }
        lastLeftChatCall = now

        val leftChatEntry = hashMapOf<String, Any>(
            "roomID" to currentRoomID,
            "leftBy" to (userID.toString()),
            "createdAt" to now
        )


        db.collection("Leftchat")
            .document(currentRoomID)
            .set(leftChatEntry)
            .addOnSuccessListener {
                Log.d(TAG, "Leftchat entry created for room: $currentRoomID")
            }
            .addOnFailureListener { e ->
                setIsWaiting(false)
                errorMsg.value = "Failed to connect to the server"
                Log.w(TAG, "Error adding to Leftchat", e)
            }

    }



    fun sendMessage(message: String, userData: UserData?) {
        if (roomID == null) {
            return
        }

        if (message.isNotBlank()) {
            val trimmedMessage = message.trim()
            val senderName = userData?.name ?: "You"

            messages.add("$senderName: $trimmedMessage")


            val messageMap = hashMapOf<String, Any>(
                "id" to (userID ?: 0),
                "name" to (userData?.name ?: "Unknown"),
                "Message" to message.trim(),  //message
                "roomID" to (roomID ?: 0),
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("messages")
                .add(messageMap)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    errorMsg.value = "failed to send message"
                }

        }

    }
    fun setIsWaiting(value: Boolean) {
        isWaitingForOtherPerson.value = value;
    }

    fun cleanMessages(){
        messages.clear()
        messages.addAll(listOf("", "", "Welcome to the chat!"))
    }

    private fun startRemoteTrackLogger() {
        viewModelScope.launch {
            while (true) {
                val track = remoteVideoTrack.value
                if (track != null) {
                    Log.d(
                        "RemoteTrackLogger",
                        "remoteTrack id=${track.id()}, enabled=${track.enabled()}, state=${track.state()}"
                    )
                } else {
                    Log.d("RemoteTrackLogger", "remoteTrack = null")
                }

                kotlinx.coroutines.delay(1000) // log every 2 seconds
            }
        }
    }

    fun reset() {
        chatRoomListenerRegistration?.remove()
        chatRoomListenerRegistration = null

        messageListenerRegistration?.remove()
        messageListenerRegistration = null

        leftChatListenerRegistration?.remove()
        leftChatListenerRegistration = null

// --- stop signaling listener ---
        firebaseSignal?.stopListening()
        firebaseSignal = null

// --- shutdown webrtc cleanly ---
        repo = null
        webRtcClient?.closeConnection()
// If you want a full release (camera + native objects), use:
        webRtcClient?.dispose()
        webRtcClient = null
        webRtcInitialized = false


        localVideoTrack.value = null
        remoteVideoTrack.value = null
        //for showing user errors from firestore
        errorMsg.value ="";
        //when user is waiting for connection from another user
        isWaitingForOtherPerson.value = false;
        userID = null
        roomID = null
        timeSinceLast = 0L

        lastLeftChatCall = 0L
        timeSinceLastRemoval = 0L
        repo = null
        webRtcClient  = null
        webRtcInitialized = false
        cleanMessages()
    }

    override fun onCleared() {
        setIsWaiting(false)
        super.onCleared()
        reset() // reset now removes listeners + stops signaling + closes webrtc
    }
}