package com.app.driftchat.client

import android.util.Log
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import java.io.IOException

class MyWebSocketSignaling(
    roomID: String
) : SignalingClient {
    private var isConnected = false
    private val messageQueue = mutableListOf<String>()
    private val serverUrl = "wss://10.0.2.2:8000/join?roomID=$roomID"
    companion object {
        private const val TAG = "WebSocketSignaling"
    }
    var onOpenCallback: (() -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // keep-alive
        .build()

    private val request = Request.Builder()
        .url(serverUrl)
        .build()

    private var ws: WebSocket? = null

    // callbacks
    override var onRemoteSessionReceived: ((SessionDescription) -> Unit)? = null
    override var onIceCandidateReceived: ((IceCandidate) -> Unit)? = null

    init {
        connect()
    }

    private fun connect() {
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true

                // call queued messages
                messageQueue.forEach { ws?.send(it) }
                messageQueue.clear()

                // notify external listener
                onOpenCallback?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                parseMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // optional: handle binary messages
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
            }
        })
    }
    private fun sendMessage(message: String) {
        if (isConnected) {
            ws?.send(message)
            Log.d(TAG, "Sent: $message")
        } else {
            messageQueue.add(message)
            Log.d(TAG, "Queued message (WS not open yet): $message")
        }
    }

    override fun sendIceCandidate(candidate: IceCandidate) {
        val message = JSONObject().apply {
            put("type", "ice")
            put("sdpMid", candidate.sdpMid)
            put("sdpMLineIndex", candidate.sdpMLineIndex)
            put("candidate", candidate.sdp)
        }.toString()
        sendMessage(message)
    }


    override fun sendSessionDescription(sdp: SessionDescription) {
        val message = JSONObject().apply {
            put("type", "sdp")
            put("sdpType", sdp.type.name)
            put("sdp", sdp.description)
        }.toString()
        sendMessage(message)
    }

    private fun parseMessage(message: String) {
        try {
            val json = org.json.JSONObject(message)
            when (json.getString("type")) {
                "sdp" -> {
                    val sdpType = SessionDescription.Type.valueOf(json.getString("sdpType"))
                    val sdpDescription = json.getString("sdp")
                    val sdp = SessionDescription(sdpType, sdpDescription)
                    onRemoteSessionReceived?.invoke(sdp)
                }
                "ice" -> {
                    val candidate = IceCandidate(
                        json.getString("sdpMid"),
                        json.getInt("sdpMLineIndex"),
                        json.getString("candidate")
                    )
                    onIceCandidateReceived?.invoke(candidate)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: $message", e)
        }
    }
    fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket connected")
        isConnected = true

        // Send any queued messages
        messageQueue.forEach { ws?.send(it) }
        messageQueue.clear()
    }
    fun close() {
        ws?.close(1000, "Client closed")
    }
}