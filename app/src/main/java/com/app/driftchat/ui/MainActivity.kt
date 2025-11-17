package com.app.driftchat.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.driftchat.ui.theme.DriftChatTheme
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel
import com.app.driftchat.ui.viewmodels.ThemeViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import com.app.driftchat.utils.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NetworkMonitor.register(this)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            DriftChatTheme(darkTheme = isDarkTheme) {
                val userViewModel: UserDataViewModel = viewModel()
                val chatViewModel: ChatViewModel = hiltViewModel()
                val quoteViewModel: QuoteViewModel = hiltViewModel()
                val isConnected by NetworkMonitor.isConnected.collectAsState()

                if (!isConnected) {
                    Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
                }

                AppNav(userViewModel, chatViewModel, quoteViewModel, themeViewModel = themeViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkMonitor.unregister()
    }
}