package com.app.driftchat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.app.driftchat.ui.screens.ChatRoom
import com.app.driftchat.ui.screens.UserAboutScreen
import com.app.driftchat.ui.screens.UserDataScreen
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel
import com.app.driftchat.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class) // for pager API
@Composable
fun AppNav(
    viewModel: UserDataViewModel,
    chatViewModel: ChatViewModel,
    quoteViewModel: QuoteViewModel,
    themeViewModel: ThemeViewModel
) {
    val pageCount = 3
    val initialPage = 1
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> UserAboutScreen(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                onSwipeLeft = {
                    scope.launch {
                        pagerState.animateScrollToPage(initialPage)
                    }
                }
            )

            1 -> UserDataScreen(
                viewModel = viewModel,
                quoteViewModel = quoteViewModel,
                onSwipeRight = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                onSwipeLeft = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            )

            2 -> ChatRoom(
                userViewModel = viewModel,
                chatViewModel = chatViewModel,
                onSwipeRight = {
                    if (chatViewModel.isWaitingForOtherPerson.value) {
                        chatViewModel.removeUserFromWaitList()
                    } else {
                        chatViewModel.addUserToLeftChat(viewModel.data.value)
                    }
                    scope.launch {
                        pagerState.animateScrollToPage(initialPage)
                    }
                }
            )
        }
    }
}