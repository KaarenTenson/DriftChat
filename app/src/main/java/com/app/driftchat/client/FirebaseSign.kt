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
        Log.d(TAG, "listenForEvents: listener started for user=$currentUserId")
        db.collection("videoCalls")
            .whereEqualTo("callee", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val data = change.document.data
                    Log.d(TAG, "Firestore event: docId=${change.document.id} data=$data")

                    val type = data["type"] as? String ?: run {
                        Log.w(TAG, "Missing type in document")
                        return@forEach
                    }
                    val caller = data["caller"] as? String ?: ""
                    lastCaller = caller

                    Log.d(TAG, "Event received: type=$type, caller=$caller, callee=${data["callee"]}")

                    onEvent(SignalingEvent(type, caller))
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