package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 * A ViewModel class that manages the state of a word-guessing game.
 * It handles the current word, game over status, and user input validation.
 */
class GameViewModel: ViewModel() {

    /**
     * Holds the current word to guess.
     * Backed by [MutableStateFlow] for observing changes.
     */
    private val _currentWord = MutableStateFlow<String>("")
    /**
     * Publicly exposed state of the current word as a [StateFlow].
     */
    val currentWord = _currentWord.asStateFlow()
    fun setWord(word: String) {
        _currentWord.value = word
    }
    /**
     * Holds the game over status.
     * Backed by [MutableStateFlow] for observing changes.
     */
    private val _gameOver = MutableStateFlow(false)
    /**
     * Publicly exposed state of the game over status as a [StateFlow].
     */
    val gameOver: StateFlow<Boolean> get() = _gameOver
    /**
     * Updates the game over status.
     *
     * @param value The new game over state.
     */
    fun setGameOver(value: Boolean) {
        if (_gameOver.value != value) {
            _gameOver.value = value
        }
    }
    /**
     * Handles a new message input from the user.
     * Compares the message with the current word to determine if it is correct.
     *
     * @param message The user's input message.
     */
    fun onNewMessage(message: String) {
        if (message.trim().equals(_currentWord.value.trim(), ignoreCase = true)) {
            Log.d("onNewMessage", "CORRECT ANSWER $message")
            setGameOver(true)
        } else {
            Log.d("onNewMessage", "NOT CORRECT ANSWER ${_currentWord.value} ≠ $message")
            setGameOver(false)
        }
    }
    /**
     * Resets the game state, clearing the current word and resetting the game over status.
     */
    fun resetGameState() {
        _currentWord.value = ""
        _gameOver.value = false
    }
}