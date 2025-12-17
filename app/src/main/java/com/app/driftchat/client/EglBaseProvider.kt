package com.app.driftchat.client

import org.webrtc.EglBase

object EglBaseProvider {
    val eglBase: EglBase by lazy {
        EglBase.create()
    }
}