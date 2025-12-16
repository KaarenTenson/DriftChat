package com.app.driftchat.client
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.RtpTransceiver
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import java.util.UUID

class NSWebRTCClient(
    private val context: Context,
    private val signaling: FirebaseSignaling
) {
    private val TAG = "NSWebRTCClient"
    private var localTracksAdded = false
    private var streamId: String? = null
    // --- native / long-lived resources (keep references) ---
    private val eglBase = EglBaseProvider.eglBase
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var cameraCapturer: CameraVideoCapturer? = null

    private var isInitialized = false;
    private val peerConnectionFactory by lazy {createPeerConnectionFactory()}
    private var peerConnection: PeerConnection? = null

    private val localVideoSource by lazy {peerConnectionFactory?.createVideoSource(false)}
    private val localAudioSource by lazy {peerConnectionFactory?.createAudioSource(MediaConstraints())}

    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private val pendingIce = mutableListOf<IceCandidate>()
    @Volatile private var remoteSdpSet = false


    private var remoteTrackListener: ((VideoTrack) -> Unit)? = null
    public var currentTarget: String? = null

    // --- Public API ---------------------------------------------------------
    fun setOnRemoteTrackListener(callback: (VideoTrack) -> Unit) {
        remoteTrackListener = callback
    }

    fun getLocalVideoTrack(): VideoTrack? = localVideoTrack
    private fun ensureLocalTracksAdded() {
        val pc = peerConnection ?: return
        if (localTracksAdded) return

        val video = localVideoTrack ?: run {
            Log.w(TAG, "ensureLocalTracksAdded: video track null")
            return
        }
        val audio = localAudioTrack ?: run {
            Log.w(TAG, "ensureLocalTracksAdded: audio track null")
            return
        }

        if (streamId == null) streamId = "stream-${UUID.randomUUID()}"
        val sid = streamId!!

        try {
            pc.addTrack(video, listOf(sid))
            pc.addTrack(audio, listOf(sid))
            localTracksAdded = true
            Log.d(TAG, "ensureLocalTracksAdded: ✅ added local tracks to PeerConnection")
        } catch (e: Exception) {
            Log.e(TAG, "ensureLocalTracksAdded: addTrack failed: ${e.message}", e)
        }
    }
    /**
     * Initialize PeerConnectionFactory and local media. Must be called on main thread.
     */
    fun createPeerConnectionFactory(): PeerConnectionFactory {
        if (isInitialized) {
            return peerConnectionFactory
        }
        Log.d(TAG, "createPeerConnectionFactory()")
        val options = PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)
        isInitialized = true
        return PeerConnectionFactory.builder().setVideoDecoderFactory(
            DefaultVideoDecoderFactory(eglBase.eglBaseContext)).setVideoEncoderFactory(
            DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        ).setOptions(PeerConnectionFactory.Options().apply{disableNetworkMonitor = false
            disableEncryption = false}).createPeerConnectionFactory()

    }
    fun  initWebrtcClient(username: String) {
        Log.d(TAG, "initWebrtcClient() start username=$username")
        if (localVideoTrack != null || localAudioTrack != null) {
            return
        }

        // 1) SurfaceTextureHelper
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        //Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE)
        // 2) Initialize PeerConnectionFactory

        // 3) Audio
        localAudioTrack = peerConnectionFactory.createAudioTrack("AUDIO_$username", localAudioSource)
        localAudioTrack!!.setEnabled(true)


        // 4) Video
        cameraCapturer = createCameraCapturer()
        if (cameraCapturer == null) {
            Log.e(TAG, "initWebrtcClient: no camera available")
            return
        }

        // Create video source and track
        cameraCapturer!!.initialize(surfaceTextureHelper, context, localVideoSource!!.capturerObserver)

        try {
            // Use higher FPS/resolution for better preview
            Handler(Looper.getMainLooper()).post {
                cameraCapturer?.startCapture(640, 480, 10)
            }
            Log.d(TAG, "Camera started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "startCapture failed: ${e.message}")
            return
        }

        localVideoTrack = peerConnectionFactory!!.createVideoTrack("VIDEO_$username", localVideoSource)
        localVideoTrack?.setEnabled(true) // ensure track is enabled

        // If you want immediate preview in Compose, call the listener here:

        Log.d(TAG, "initWebrtcClient() done - audio=${localAudioTrack != null}, video=${localVideoTrack != null}")
    }

    /**
     * Start call to target - will create PeerConnection if needed and createOffer
     */
    fun call(target: String) {
        Log.d(TAG, "call() target=$target")
        currentTarget = target

        // Wait until local tracks exist
        if (localVideoTrack == null || localAudioTrack == null) {
            Log.w(TAG, "call(): local tracks not ready yet, retrying in 100ms")
            Handler(Looper.getMainLooper()).postDelayed({ call(target) }, 100)
            return
        }

        createPeerConnectionIfNeeded(target)
        ensureLocalTracksAdded()

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) {
                    Log.e(TAG, "call: onCreateSuccess sdp==null")
                    return
                }

                Log.d(TAG, "call: created offer len=${sdp.description.length}, setting local desc")
                peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d(TAG, "call: local desc set -> sending offer")
                        signaling.sendOffer(target, sdp.description)
                    }

                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "call: setLocalDescription failed: $error")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "call: createOffer failed: $error")
            }
        }, constraints)
    }


    /**
     * Answer remote offer (target is the caller id)
     */
    fun answer(target: String) {
        Log.d(TAG, "answer() target=$target")
        currentTarget = target

        if (localVideoTrack == null || localAudioTrack == null) {
            Log.w(TAG, "answer(): local tracks not ready yet, retrying in 100ms")
            Handler(Looper.getMainLooper()).postDelayed({ answer(target) }, 100)
            return
        }

        createPeerConnectionIfNeeded(target)
        ensureLocalTracksAdded()

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return

                // ✅ recreate a fresh SessionDescription (avoids native "NULL" issue)
                val localDesc = SessionDescription(sdp.type, sdp.description)

                // ✅ setLocalDescription on MAIN thread
                Handler(Looper.getMainLooper()).post {
                    peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            Log.d(TAG, "answer: local desc set -> sending answer")
                            signaling.sendAnswer(target, localDesc.description)
                        }

                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "answer: setLocalDescription failed: $error")
                        }
                    }, localDesc)
                }
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "answer: createAnswer failed: $error")
            }
        }, constraints)
    }


    /**
     * Set remote SDP (offer or answer)
     */

    fun onRemoteSessionReceived(
        type: SessionDescription.Type,
        sdp: String,
        onComplete: (() -> Unit)? = null
    ) {
        Log.d(TAG, "onRemoteSessionReceived: type=$type")
        createPeerConnectionIfNeeded(currentTarget)

        val desc = SessionDescription(type, sdp)
        peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote SDP set successfully")
                remoteSdpSet = true

                // flush queued ICE
                val pc = peerConnection
                pendingIce.forEach { pc?.addIceCandidate(it) }
                pendingIce.clear()

                onComplete?.invoke()
            }

            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote SDP: $error")
            }
        }, desc)
    }


    fun addIceCandidateToPeer(candidate: IceCandidate) {
        val pc = peerConnection
        if (pc?.remoteDescription == null) {
            pendingIce.add(candidate)
            Log.d(TAG, "Queued ICE (remote SDP not set yet). pending=${pendingIce.size}")
            return
        }
        pc.addIceCandidate(candidate)
    }

    /**
     * Stop active connection but keep factory and local track ready.
     * Call dispose() to fully release native resources.
     */
    fun closeConnection() {
        Log.d(TAG, "closeConnection()")
        try { peerConnection?.close() } catch (_: Exception) {}
        peerConnection = null
        currentTarget = null

        localTracksAdded = false
        streamId = null
    }

    /**
     * Completely release native resources. Call when you won't use WebRTC anymore.
     */
    fun dispose() {
        Log.d(TAG, "dispose() - releasing native resources")
        // Stop camera first
        try {
            cameraCapturer?.stopCapture()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: stopCapture failed: ${e.message}")
        }
        try {
            cameraCapturer?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: cameraCapturer.dispose failed: ${e.message}")
        }
        cameraCapturer = null

        try {
            localVideoSource?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: localVideoSource.dispose failed: ${e.message}")
        }

        try {
            localAudioSource?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: localAudioSource.dispose failed: ${e.message}")
        }

        try {
            peerConnection?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: peerConnection.dispose failed: ${e.message}")
        }
        peerConnection = null

        try {
            peerConnectionFactory?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: peerConnectionFactory.dispose failed: ${e.message}")
        }
        try {
            surfaceTextureHelper?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: surfaceTextureHelper.dispose failed: ${e.message}")
        }
        surfaceTextureHelper = null

        try {
            eglBase.release()
        } catch (e: Exception) {
            Log.w(TAG, "dispose: eglBase.release failed: ${e.message}")
        }
    }

    // --- Internal helpers --------------------------------------------------

    fun createPeerConnectionIfNeeded(target: String?) {
        if (peerConnection != null) return
        Log.d(TAG, "createPeerConnectionIfNeeded target=$target")

        if (peerConnectionFactory == null) {
            Log.e(TAG, "createPeerConnectionIfNeeded: peerConnectionFactory == null; call initWebrtcClient() first")
            return
        }

        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        Log.d(TAG, "createPeerConnectionIfNeeded called. target=$target, currentTarget=$currentTarget, peerConnection=$peerConnection")

        peerConnection = peerConnectionFactory!!.createPeerConnection(rtcConfig, object : PeerConnection.Observer {

            override fun onIceCandidate(candidate: IceCandidate?) {
                Log.d(TAG, "onIceCandidate fired: candidate=$candidate, target=$target, currentTarget=$currentTarget")
                candidate?.let { signaling.sendIceCandidate(target ?: currentTarget!!, it) }
            }

            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "onAddStream: $stream")
                if (stream != null) {
                    remoteTrackListener?.invoke(stream.videoTracks[0])
                }
            }
            override fun onDataChannel(dc: DataChannel?) { Log.d(TAG, "onDataChannel") }
            override fun onIceConnectionReceivingChange(p0: Boolean) { Log.d(TAG, "onIceConnectionReceivingChange: $p0") }
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) { Log.d(TAG, "onIceConnectionChange: $newState") }
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) { Log.d(TAG, "onIceGatheringChange: $newState") }
            override fun onSignalingChange(newState: PeerConnection.SignalingState?) { Log.d(TAG, "onSignalingChange: $newState") }
            override fun onRemoveStream(stream: MediaStream?) { Log.d(TAG, "onRemoveStream: $stream") }
            override fun onRenegotiationNeeded() { Log.d(TAG, "onRenegotiationNeeded") }

            override fun onTrack(transceiver: RtpTransceiver?) {
                val t = transceiver?.receiver?.track()
                Log.d(TAG, "🔥 onTrack fired: kind=${t?.kind()}, id=${t?.id()}")

                if (t is VideoTrack) {
                    Handler(Looper.getMainLooper()).post {
                        remoteTrackListener?.invoke(t)
                    }
                }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) { Log.d(TAG, "onIceCandidatesRemoved: ${candidates?.size ?: 0}") }
        })

        // Attach local tracks using addTrack() (Unified Plan)
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        Log.d(TAG, "createCameraCapturer: devices=${deviceNames.contentToString()}")
        for (device in deviceNames) {
            if (enumerator.isFrontFacing(device)) {
                enumerator.createCapturer(device, null)?.let {
                    Log.d(TAG, "createCameraCapturer: using front camera $device")
                    return it
                }
            }
        }
        for (device in deviceNames) {
            enumerator.createCapturer(device, null)?.let {
                Log.d(TAG, "createCameraCapturer: using camera $device")
                return it
            }
        }
        return null
    }

    // Simple SdpObserver with default no-op implementations
    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onSetSuccess() {
        }
        override fun onCreateFailure(error: String?) { Log.e("s", "SdpObserver onCreateFailure: $error") }
        override fun onSetFailure(error: String?) { Log.e("s", "SdpObserver onSetFailure: $error") }
    }
}