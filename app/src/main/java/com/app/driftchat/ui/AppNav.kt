package com.app.driftchat.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.driftchat.ui.screens.ChatRoom
import com.app.driftchat.ui.screens.UserAboutScreen
import com.app.driftchat.ui.screens.UserDataScreen
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel
import com.app.driftchat.ui.viewmodels.ThemeViewModel

@Composable
fun AppNav(viewModel: UserDataViewModel, chatViewModel: ChatViewModel, quoteViewModel: QuoteViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.UserData.route
    ) {
        composable(Screen.UserData.route) {
            UserDataScreen(
                viewModel = viewModel,
                quoteViewModel = quoteViewModel,
                onSwipeRight = { navController.navigate(Screen.UserAbout.route) },
                onSwipeLeft = { navController.navigate(Screen.ChatRoom.route)}
            )
        }

        composable(Screen.UserAbout.route) {
            UserAboutScreen(
                viewModel = viewModel,
                themeViewModel,
                onSwipeLeft = { navController.navigate(Screen.UserData.route) }
            )
        }

        composable(Screen.ChatRoom.route) {
            ChatRoom(
                userViewModel = viewModel,
                chatViewModel = chatViewModel,
                onSwipeRight =  {
                    if (chatViewModel.isWaitingForOtherPerson.value) {
                        chatViewModel.removeUserFromWaitList()
                    } else {
                        chatViewModel.addUserToLeftChat(viewModel.data.value)
                    }
                    navController.navigate(Screen.UserData.route)
                }
//                    chatViewModel.addUserToLeftChat(viewModel.data.value)
//                    navController.navigate(Screen.UserData.route) }
            )
        }


    }
}