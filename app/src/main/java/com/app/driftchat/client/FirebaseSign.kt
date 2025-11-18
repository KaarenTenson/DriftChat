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

    override fun listenForEvents(onEvent: (SignalingEvent) -> Unit) {
        Log.d(TAG, "4444444444")
        db.collection("videoCalls")
            .whereEqualTo("callee", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }


                snapshot?.documentChanges?.forEach { change ->
                    val data = change.document.data
                    val type = data["type"] as? String ?: run {
                        Log.w(TAG, "listenForEvents: Missing 'type' in document ${change.document.id}")
                        return@forEach
                    }
                    val caller = data["caller"] as? String ?: ""
                    lastCaller = caller
                    Log.d(TAG, "listenForEvents: Event type=$type from caller=$caller")

                    val sdpString = data["sdp"] as? String
                    val sdp = sdpString?.let {
                        SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(
                                type
                            ), it
                        )
                    }

                    val iceCandidateData = data["iceCandidate"] as? Map<String, Any>
                    val iceCandidate = iceCandidateData?.let {
                        IceCandidate(
                            it["sdpMid"] as String,
                            (it["sdpMLineIndex"] as Long).toInt(),
                            it["sdp"] as String
                        )
                    }

                    onEvent(SignalingEvent(type, caller, sdp, iceCandidate))
                }
            }
    }

    override fun sendOffer(target: String, sdp: String) = sendEvent(target, "Offer", sdp)
    override fun sendAnswer(target: String, sdp: String) = sendEvent(target, "Answer", sdp)
    override fun sendIceCandidate(target: String, candidate: IceCandidate) {
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