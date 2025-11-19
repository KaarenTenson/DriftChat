package com.app.driftchat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.driftchat.data.AppDatabase
import com.app.driftchat.data.UserDataRepository
import com.app.driftchat.ui.AppNav
import com.app.driftchat.ui.viewmodels.ChatViewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel
import com.app.driftchat.ui.viewmodels.ThemeViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeNavigationUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createTestRepo(): UserDataRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        return UserDataRepository(db.userDataDao())
    }

    @Test
    fun swipeNavigationAcrossAllScreens() {

        val repo = createTestRepo()
        val userDataViewModel = UserDataViewModel(repo)
        val chatViewModel = ChatViewModel()
        val quoteViewModel = QuoteViewModel()
        val themeViewModel = ThemeViewModel()

        composeRule.setContent {
            AppNav(
                viewModel = userDataViewModel,
                chatViewModel = chatViewModel,
                quoteViewModel = quoteViewModel,
                themeViewModel = themeViewModel
            )
        }

        // START SCREEN
        composeRule.onNodeWithText("What do you want to be called?")
            .assertExists()

        // RIGHT → ABOUT
        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.onNodeWithText("Hobbies:")
            .assertExists()

        // LEFT → BACK TO DATA
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("What do you want to be called?")
            .assertExists()

        // LEFT → CHAT
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("Connecting...")
            .assertExists()
    }
}
