package com.app.driftchat.client

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

class FirebaseSign(
    private val db: FirebaseFirestore,
    private val currentUserId: String
) : FirebaseSignaling {

    private var lastCaller: String = ""
    private val MAX_SIGNALING_AGE_MS = 2 * 60 * 1000L // 2 minutes
    private val seenDocIds = mutableSetOf<String>()
    private var reg: ListenerRegistration? = null

    override fun sendStartCall(target: String) = sendEvent(target, "StartVideoCall", "")

    override fun listenForEvents(onEvent: (SignalingEvent) -> Unit) {
        reg?.remove()
        seenDocIds.clear()

        reg = db.collection("videoCalls")
            .whereEqualTo("callee", currentUserId) // ✅ only events addressed to me
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type != DocumentChange.Type.ADDED) return@forEach

                    val docId = change.document.id
                    if (!seenDocIds.add(docId)) return@forEach

                    val data = change.document.data
                    val ts = (data["timestamp"] as? Long) ?: 0L
                    val age = System.currentTimeMillis() - ts

                    // ✅ ignore old junk that existed before app opened
                    if (ts == 0L || age > MAX_SIGNALING_AGE_MS) {
                        // optional cleanup of old docs
                        change.document.reference.delete()
                        return@forEach
                    }

                    val type = data["type"] as? String ?: return@forEach
                    val caller = data["caller"] as? String ?: ""
                    if (caller.isBlank() || caller == currentUserId) return@forEach

                    lastCaller = caller

                    val sdpString = data["sdp"] as? String
                    val sdp = if (!sdpString.isNullOrBlank() && (type == "Offer" || type == "Answer")) {
                        SessionDescription(
                            if (type == "Offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
                            sdpString
                        )
                    } else null

                    val iceMap = data["iceCandidate"] as? Map<*, *>
                    val ice = if (iceMap != null) {
                        IceCandidate(
                            iceMap["sdpMid"] as? String,
                            (iceMap["sdpMLineIndex"] as? Long)?.toInt() ?: 0,
                            iceMap["sdp"] as? String ?: ""
                        )
                    } else null

                    onEvent(SignalingEvent(type, caller, sdp, ice))

                    // ✅ OPTIONAL but highly recommended: delete so it never replays again
                    change.document.reference.delete()
                }
            }
    }
    override fun stopListening() {
        reg?.remove()
        reg = null
        seenDocIds.clear()
    }
    override fun sendEndCall(target: String) {
        val data = mapOf(
            "type" to "EndCall",
            "caller" to currentUserId,
            "callee" to target,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("videoCalls").add(data)
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