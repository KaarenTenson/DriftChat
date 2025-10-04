package com.app.driftchat.ui

sealed class Screen(val route: String) {
    object UserData : Screen("userData")
    object UserAbout : Screen("userAbout")
}