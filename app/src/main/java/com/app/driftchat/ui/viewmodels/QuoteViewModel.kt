package com.app.driftchat.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.driftchat.data.RetrofitInstance
import kotlinx.coroutines.launch

class QuoteViewModel : ViewModel() {
    var quote by mutableStateOf("")
        public set

    var isLoading by mutableStateOf(false)
        private set

    private val apiKey = "OyJzjuIbLhOemi1zotkxLA==oBkgcSAS0zMyhSGM"

    // API call function
    fun fetchRandomQuote() {
        if (isLoading) return // returns if another API call in progress

        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitInstance.api.getRandomQuote(apiKey)
                if (response.isSuccessful) {
                    val answer = response.body()!!.first()
                    quote = "\"${answer.quote}\" - ${answer.author}"
                } else {
                    quote = "Error: Failed to fetch quote"
                }
            } catch (e: Exception) {
                quote = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}