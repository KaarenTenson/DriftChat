package com.app.driftchat.client

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSign(
    private val db: FirebaseFirestore,
    private val currentUserId: String
) : FirebaseSignaling {

    private var lastCaller: String = ""

    override fun sendStartCall(target: String) = sendEvent(target, "StartVideoCall", "")

    override fun listenForEvents(onEvent: (SignalingEvent) -> Unit) {
        Log.d(TAG, "listenForEvents: starting listeners for $currentUserId")

        // Incoming events (I am the callee)
        listenSide("callee", onEvent)

        // Outgoing confirmations (I am the caller)
        listenSide("caller", onEvent)
    }

    private fun listenSide(field: String, onEvent: (SignalingEvent) -> Unit) {
        db.collection("videoCalls")
            .whereEqualTo(field, currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val data = change.document.data
                    Log.d(TAG, "Firestore event ($field): ${change.document.id} -> $data")

                    val type = data["type"] as? String ?: return@forEach
                    val caller = data["caller"] as? String ?: ""
                    val callee = data["callee"] as? String ?: ""

                    // 🔥 ignore our own Offer/Answer/ICE that bounce back
                    if (caller == currentUserId) {
                        Log.d(TAG, "Ignoring self-sent signaling message.")
                        return@forEach
                    }

                    lastCaller = caller

                    // ---- SDP parsing ----
                    val sdpString = data["sdp"] as? String
                    val sdp = if (sdpString != null && (type == "Offer" || type == "Answer")) {
                        SessionDescription(
                            if (type == "Offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
                            sdpString
                        )
                    } else null

                    // ---- ICE parsing ----
                    val iceMap = data["iceCandidate"] as? Map<*, *>
                    val ice = if (iceMap != null) {
                        IceCandidate(
                            iceMap["sdpMid"] as? String,
                            (iceMap["sdpMLineIndex"] as? Long)?.toInt() ?: 0,
                            iceMap["sdp"] as? String ?: ""
                        )
                    } else null

                    // ---- Emit event ----
                    onEvent(SignalingEvent(type, caller, sdp, ice))
                }
            }
    }

    override fun sendOffer(target: String, sdp: String) = sendEvent(target, "Offer", sdp)
    override fun sendAnswer(target: String, sdp: String) = sendEvent(target, "Answer", sdp)
    override fun sendIceCandidate(target: String, candidate: IceCandidate) {
        Log.d("fb","sendingICE")
        val data = mapOf(
            "type" to "IceCandidate",
            "caller" to currentUserId,
            "callee" to target,
            "iceCandidate" to mapOf(
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "sdp" to candidate.sdp
            ),
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("videoCalls").add(data)
    }

    override fun sendEndCall() {
        val data = mapOf(
            "type" to "EndCall",
            "caller" to currentUserId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("videoCalls").add(data)
    }

    override fun getLastCaller(): String = lastCaller

    private fun sendEvent(target: String, type: String, sdp: String) {
        val data = mapOf(
            "type" to type,
            "caller" to currentUserId,
            "callee" to target,
            "sdp" to sdp,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("videoCalls").add(data)
    }
}