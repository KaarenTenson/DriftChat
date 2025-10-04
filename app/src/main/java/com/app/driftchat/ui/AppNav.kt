package com.app.driftchat.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.driftchat.ui.screens.UserAboutScreen
import com.app.driftchat.ui.screens.UserDataScreen
import com.app.driftchat.ui.viewmodels.UserDataViewModel

@Composable
fun AppNav(viewModel: UserDataViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.UserData.route
    ) {
        composable(Screen.UserData.route) {
            UserDataScreen(
                viewModel = viewModel,
                onSwipeRight = { navController.navigate(Screen.UserAbout.route) }
            )
        }

        composable(Screen.UserAbout.route) {
            UserAboutScreen(
                viewModel = viewModel,
                onSwipeLeft = { navController.popBackStack() }
            )
        }
    }
}