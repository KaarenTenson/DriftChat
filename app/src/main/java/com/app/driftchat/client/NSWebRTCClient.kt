package com.app.driftchat.client
import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.RtpTransceiver
import org.webrtc.DataChannel

class NSWebRTCClient(
    private val context: Context
) {
    private val eglBase: EglBase = EglBase.create()
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var localVideoSource: VideoSource
    private lateinit var localAudioSource: AudioSource
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var remoteTrackListener: ((VideoTrack) -> Unit)? = null
    fun getLocalVideoTrack(): VideoTrack? = localVideoTrack
    fun setOnRemoteTrackListener(callback: (VideoTrack) -> Unit) {
        remoteTrackListener = callback
    }
    fun initWebrtcClient(username: String) {
        // Initialize PeerConnectionFactory
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        // Create local media
        localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("AUDIO_$username", localAudioSource)

        val videoCapturer = createCameraCapturer()
        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext),
            context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(1280, 720, 30)
        localVideoTrack = peerConnectionFactory.createVideoTrack("VIDEO_$username", localVideoSource)
    }

    private fun createCameraCapturer(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }
        throw IllegalStateException("No front camera found")
    }

    fun call(target: String) {
        // Create peer connection & offer SDP
        createPeerConnection()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {}
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sessionDescription)
                // Send offer via Firebase (handled in Repository)
            }

            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    fun answer(target: String) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(answer: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {}
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, answer)
                // Send answer via Firebase
            }

            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    fun onRemoteSessionReceived(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    fun addIceCandidateToPeer(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun closeConnection() {
        peerConnection?.close()
        peerConnection = null
    }

    private fun createPeerConnection(): PeerConnection {
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                // Send ICE candidate via Firebase
            }

            override fun onAddStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                mediaStreams?.firstOrNull()?.videoTracks?.firstOrNull()?.let { track ->
                    remoteTrackListener?.invoke(track)
                }
            }
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(p0: RtpTransceiver?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
        })
        val mediaStream = peerConnectionFactory.createLocalMediaStream("LOCAL_STREAM")
        mediaStream.addTrack(localAudioTrack)
        mediaStream.addTrack(localVideoTrack)
        peerConnection?.addStream(mediaStream)
        return peerConnection!!
    }
}
