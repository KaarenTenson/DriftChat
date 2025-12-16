package com.app.driftchat.client

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.SessionDescription


class WebRtcRepository(
    private val firebaseSignaling: FirebaseSignaling,
    private val webRtcClient: NSWebRTCClient
) {
    private val _incomingCallEvents = MutableSharedFlow<String>()
    val incomingCallEvents: SharedFlow<String> = _incomingCallEvents

    private val _callEndedEvents = MutableSharedFlow<Unit>()
    val callEndedEvents: SharedFlow<Unit> = _callEndedEvents

    fun init(username: String) {
        Log.d("webr", "init()")
        firebaseSignaling.listenForEvents { event ->
            when (event.type) {

                "Offer" -> {
                    val from = event.caller ?: return@listenForEvents
                    val desc = event.sdp ?: return@listenForEvents

                    webRtcClient.currentTarget = from

                    webRtcClient.onRemoteSessionReceived(
                        type = SessionDescription.Type.OFFER,
                        sdp = desc.description,
                        onComplete = { webRtcClient.answer(from) }
                    )
                }

                "Answer" -> {
                    val from = event.caller ?: return@listenForEvents
                    val desc = event.sdp ?: return@listenForEvents

                    if (from == webRtcClient.currentTarget) {
                        webRtcClient.onRemoteSessionReceived(
                            type = SessionDescription.Type.ANSWER,
                            sdp = desc.description
                        )
                    } else {
                        Log.d("WEBRTC", "Ignoring answer from=$from currentTarget=${webRtcClient.currentTarget}")
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
        webRtcClient.currentTarget = target
        firebaseSignaling.sendStartCall(target)
        webRtcClient.call(target)
    }

    fun endCall() {
        webRtcClient.closeConnection()
        firebaseSignaling.sendEndCall()
    }
}