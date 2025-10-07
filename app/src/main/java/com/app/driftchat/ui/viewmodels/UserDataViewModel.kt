package com.app.driftchat.ui.viewmodels

import UserDataRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.driftchat.domainmodel.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDataViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _data = MutableStateFlow<UserData?>(null)
    val data = _data.asStateFlow()

    init {
        loadInitialUser(1)
    }

    private fun loadInitialUser(userId: Int) {
        viewModelScope.launch {
            userDataRepository.getUserById(userId).collect { userFromDb ->
                _data.value = userFromDb
            }
        }
    }

    /**
     * Updates the user data in both the UI state and the database.
     */
    fun updateUserData(updated: UserData) {
        viewModelScope.launch {
            userDataRepository.insertUser(updated)
        }
    }
}