package com.app.driftchat.client

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
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
        Log.d("webr","999999")
        firebaseSignaling.listenForEvents { event ->
            when (event.type) {

                "Offer" -> {
                    webRtcClient.currentTarget = event.caller
                    event.sdp?.let { sdp ->
                        webRtcClient.onRemoteSessionReceived(sdp, onComplete = {
                            // **Add a small post-delay to give WebRTC stack time to process m-lines.**
                            // This is a common, though imperfect, hack.
                            Handler(Looper.getMainLooper()).postDelayed({
                                webRtcClient.answer(target = event.caller)
                            }, 100) // e.g., 50ms
                        })
                    }
                }

                "Answer" -> {
                    if (event.caller == webRtcClient.currentTarget) {
                        // Only caller should apply answer
                        event.sdp?.let { sdp -> webRtcClient.onRemoteSessionReceived(sdp) }
                    } else {
                        Log.d("WEBRTC", "Ignoring self-generated answer")
                    }
                }

                "IceCandidate" -> {
                    event.iceCandidate?.let { webRtcClient.addIceCandidateToPeer(it) }
                }
                "EndCall" -> _callEndedEvents.tryEmit(Unit)
            }
        }
    }

    fun startCall(target: String) {
        Log.d("WEB", "startinc chat Leftchat event — triggered by current user")
        Log.d("WEB",target)
        firebaseSignaling.sendStartCall(target)

        webRtcClient.call(target)
    }

    fun endCall() {
        webRtcClient.closeConnection()
        firebaseSignaling.sendEndCall()
    }
}