package com.app.driftchat.client

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack


class ClientRtc(
    private val context: Context,
    private val localRenderer: SurfaceViewRenderer,
    private val remoteRenderer: SurfaceViewRenderer,
    private val signalingClient: SignalingClient
) {

    companion object {
        private const val TAG = "WebRTCClient"
        private const val LOCAL_VIDEO_TRACK_ID = "LOCAL_VIDEO"
        private const val LOCAL_AUDIO_TRACK_ID = "LOCAL_AUDIO"
    }

    private val eglBase: EglBase = EglBase.create()
    private lateinit var factory: PeerConnectionFactory
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var peerConnection: PeerConnection? = null

    fun init() {
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(org.webrtc.DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(org.webrtc.DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        setupLocalMedia()
        setupPeerConnection()
    }

    private fun setupLocalMedia() {
        // Video
        val videoCapturer = createCameraCapturer()
        val videoSource: VideoSource = factory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(
            org.webrtc.SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext),
            context,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(640, 480, 30)
        localVideoTrack = factory.createVideoTrack(LOCAL_VIDEO_TRACK_ID, videoSource)
        localVideoTrack?.addSink(localRenderer)

        // Audio
        val audioSource: AudioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack(LOCAL_AUDIO_TRACK_ID, audioSource)
    }

    private fun createCameraCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                val capturer = enumerator.createCapturer(name, null)
                if (capturer != null) return capturer
            }
        }
        throw RuntimeException("No front camera found")
    }

    private fun setupPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList()).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: org.webrtc.IceCandidate?) {
                candidate?.let { signalingClient.sendIceCandidate(it) }
            }
            override fun onAddStream(stream: MediaStream?) {}
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver?, streams: Array<out MediaStream>?) {}
            override fun onDataChannel(dc: DataChannel?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out org.webrtc.IceCandidate>?) {}
        })

        // Add tracks
        val stream = factory.createLocalMediaStream("LOCAL_STREAM")
        stream.addTrack(localAudioTrack)
        stream.addTrack(localVideoTrack)
        peerConnection?.addStream(stream)
    }

    fun createOffer() {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : org.webrtc.SdpObserver {
            override fun onCreateSuccess(sdp: org.webrtc.SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(this, it)
                    signalingClient.sendSessionDescription(it)
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun handleRemoteSession(sdp: org.webrtc.SessionDescription) {
        peerConnection?.setRemoteDescription(object : org.webrtc.SdpObserver {
            override fun onCreateSuccess(p0: org.webrtc.SessionDescription?) {}
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, sdp)
    }

    fun addIceCandidate(candidate: org.webrtc.IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }
}