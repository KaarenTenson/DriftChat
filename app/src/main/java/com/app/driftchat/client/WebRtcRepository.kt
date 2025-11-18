package com.app.driftchat.client

import kotlinx.coroutines.flow.*

class WebRtcRepository(
    private val firebaseSignaling: FirebaseSignaling,
    private val webRtcClient: NSWebRTCClient
) {
    private val _incomingCallEvents = MutableSharedFlow<String>()
    val incomingCallEvents: SharedFlow<String> = _incomingCallEvents

    private val _callEndedEvents = MutableSharedFlow<Unit>()
    val callEndedEvents: SharedFlow<Unit> = _callEndedEvents

    fun init(username: String) {
        webRtcClient.initWebrtcClient(username)
        firebaseSignaling.listenForEvents { event ->
            when (event.type) {
                "StartVideoCall" -> _incomingCallEvents.tryEmit(event.caller)
                "EndCall" -> _callEndedEvents.tryEmit(Unit)
                "Offer" -> event.sdp?.let { webRtcClient.onRemoteSessionReceived(it) }
                "Answer" -> event.sdp?.let { webRtcClient.onRemoteSessionReceived(it) }
                "IceCandidate" -> event.iceCandidate?.let { webRtcClient.addIceCandidateToPeer(it) }
            }
        }
    }

    fun startCall(target: String) {
        webRtcClient.call(target)
    }

    fun answerCall() {
        webRtcClient.answer(target = firebaseSignaling.getLastCaller())
    }

    fun endCall() {
        webRtcClient.closeConnection()
        firebaseSignaling.sendEndCall()
    }
}