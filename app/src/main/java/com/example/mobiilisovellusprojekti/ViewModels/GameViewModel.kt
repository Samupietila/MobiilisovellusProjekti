package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel: ViewModel() {
    private val _currentWord = MutableStateFlow<String>("")
    val currentWord = _currentWord.asStateFlow()
    fun setWord(word: String) {
        _currentWord.value = word
    }

    fun onNewMessage(message: String) {
        if (message.trim().equals(_currentWord.value.trim(), ignoreCase = true)) {
            Log.d("onNewMessage", "CORRECT ANSWER $message")
        } else {
            Log.d("onNewMessage", "NOT CORRECT ANSWER ${_currentWord.value} ≠ $message")
        }
    }
}