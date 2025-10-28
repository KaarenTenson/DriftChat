package com.app.driftchat.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import com.app.driftchat.utils.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Start monitoring network connection
        NetworkMonitor.register(this)

        setContent {
            val userViewModel: UserDataViewModel = viewModel()
            val chatViewModel: ChatViewModel = hiltViewModel()

            // ✅ Observe real-time network state
            val isConnected by NetworkMonitor.isConnected.collectAsState()

            if (!isConnected) {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
            }

            AppNav(userViewModel, chatViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Stop monitoring to avoid leaks
        NetworkMonitor.unregister()
    }
}
