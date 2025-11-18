package com.app.driftchat.client

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignalingClient {
    fun sendIceCandidate(candidate: IceCandidate)
    fun sendSessionDescription(sdp: SessionDescription)

    // callbacks from remote peer
    var onRemoteSessionReceived: ((SessionDescription) -> Unit)?
    var onIceCandidateReceived: ((IceCandidate) -> Unit)?
}
