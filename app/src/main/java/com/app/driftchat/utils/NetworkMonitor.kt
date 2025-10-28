package com.app.driftchat.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

object NetworkMonitor {

    private lateinit var connectivityManager: ConnectivityManager
    private val _isConnected = MutableStateFlow(true)
    val isConnected = _isConnected.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            verifyInternetAccess(network)
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }
    }

    fun register(context: Context) {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            _isConnected.value = false
        }

        // Immediately check current state on launch
        val network = connectivityManager.activeNetwork
        if (network == null) {
            _isConnected.value = false
        } else {
            verifyInternetAccess(network)
        }
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) { }
    }

    // Actually test a small socket connection to verify internet access
    private fun verifyInternetAccess(network: Network) {
        CoroutineScope(Dispatchers.IO).launch {
            val hasInternet = try {
                val socket = Socket()
                val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                socket.connect(socketAddress, 1500)
                socket.close()
                true
            } catch (e: IOException) {
                false
            }
            _isConnected.value = hasInternet
        }
    }
}
