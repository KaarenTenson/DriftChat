package com.app.driftchat.client

import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.coroutines.flow.*

class WebRtcRepository(
    private val firebaseSignaling: FirebaseSignaling,
    public val webRtcClient: NSWebRTCClient
) {
    private val _incomingCallEvents = MutableSharedFlow<String>()
    val incomingCallEvents: SharedFlow<String> = _incomingCallEvents

    private val _callEndedEvents = MutableSharedFlow<Unit>()
    val callEndedEvents: SharedFlow<Unit> = _callEndedEvents

    fun init(username: String) {
        webRtcClient.initWebrtcClient(username)
        firebaseSignaling.listenForEvents { event ->
            when (event.type) {
                "Offer" -> {
                    event.sdp?.let { webRtcClient.onRemoteSessionReceived(it) }
                }
                "Answer" -> {
                    event.sdp?.let { webRtcClient.onRemoteSessionReceived(it) }
                }
                "IceCandidate" -> event.iceCandidate?.let { webRtcClient.addIceCandidateToPeer(it) }
                "StartVideoCall" -> _incomingCallEvents.tryEmit(event.caller)
                "EndCall" -> _callEndedEvents.tryEmit(Unit)
            }
        }
    }

    fun startCall(target: String) {
        Log.d("WEB", "startinc chat Leftchat event — triggered by current user")
        Log.d("WEB",target)
        webRtcClient.call(target)
    }

    fun answerCall() {
        Log.d("WEB", "answering call")
        webRtcClient.answer(target = firebaseSignaling.getLastCaller())
    }

    fun endCall() {
        webRtcClient.closeConnection()
        firebaseSignaling.sendEndCall()
    }
}