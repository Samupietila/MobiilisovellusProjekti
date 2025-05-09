package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel: ViewModel() {
    private val _currentWord = MutableStateFlow<String>("")
    val currentWord = _currentWord.asStateFlow()
    fun setWord(word: String) {
        _currentWord.value = word
    }

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> get() = _gameOver

    /**
     * Sets the game over state.
     */
    fun setGameOver(value: Boolean) {
        if (_gameOver.value != value) {
            _gameOver.value = value
        }
    }

    fun onNewMessage(message: String) {
        if (message.trim().equals(_currentWord.value.trim(), ignoreCase = true)) {
            Log.d("onNewMessage", "CORRECT ANSWER $message")
            setGameOver(true)
        } else {
            Log.d("onNewMessage", "NOT CORRECT ANSWER ${_currentWord.value} ≠ $message")
            setGameOver(false)
        }
    }

    fun resetGameState() {
        _currentWord.value = ""
        _gameOver.value = false
    }
}