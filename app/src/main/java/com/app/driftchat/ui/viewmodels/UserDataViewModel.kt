package com.app.driftchat.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.app.driftchat.domainmodel.Gender
import com.app.driftchat.domainmodel.UserData

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class UserDataViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private companion object { const val KEY_VIEW = "view_mode" }

    private val _data = MutableStateFlow<UserData>(loadUserData())
    val data = _data.asStateFlow()



    fun updateUserData(updated: UserData) {
        _data.value = updated
    }
    fun loadUserData() :UserData {
        return UserData(
            name = "Madis",
            hobbies = setOf(),
            description = "Kaja Kallas on mu hear me out",
            gender = Gender.MALE)
    }

}