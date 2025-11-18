package com.app.driftchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.driftchat.client.WebRtcRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CallState {
    object Idle : CallState()
    data class IncomingCall(val caller: String) : CallState()
    object Connecting : CallState()
    object InCall : CallState()
    object Ended : CallState()
}

class CallViewModel(
    private val webRtcRepository: WebRtcRepository
) : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    init {
        viewModelScope.launch {
            webRtcRepository.incomingCallEvents.collect { caller ->
                _callState.value = CallState.IncomingCall(caller)
            }
        }
        viewModelScope.launch {
            webRtcRepository.callEndedEvents.collect {
                _callState.value = CallState.Ended
            }
        }
    }

    fun startCall(target: String) {
        _callState.value = CallState.Connecting
        webRtcRepository.startCall(target)
    }

    fun answerCall() {
        _callState.value = CallState.Connecting
        webRtcRepository.answerCall()
    }

    fun endCall() {
        webRtcRepository.endCall()
        _callState.value = CallState.Ended
    }
}