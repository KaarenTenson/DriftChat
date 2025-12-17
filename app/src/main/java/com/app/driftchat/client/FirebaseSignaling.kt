package com.app.driftchat.client

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

data class SignalingEvent(
    val type: String,
    val caller: String = "",
    val sdp: SessionDescription? = null,
    val iceCandidate: IceCandidate? = null
)

interface FirebaseSignaling {
    fun stopListening()
    fun sendEndCall(target: String)
    fun listenForEvents(onEvent: (SignalingEvent) -> Unit)
    fun sendOffer(target: String, sdp: String)
    fun sendAnswer(target: String, sdp: String)
    fun sendIceCandidate(target: String, candidate: IceCandidate)
    fun sendEndCall()
    fun getLastCaller(): String
    fun sendStartCall(target: String)
}