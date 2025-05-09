package com.example.mobiilisovellusprojekti.ViewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a chat message, including the message content and whether it was sent by the user.
 *
 * @param message The content of the chat message.
 * @param isSentByUser Indicates if the message was sent by the user.
 */
data class ChatMessage(
    val message: String,
    val isSentByUser: Boolean
)

/**
 * ViewModel class that manages the state of chat messages.
 * It provides functionality to add new messages and reset the chat state.
 */
class ChatViewModel : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    /**
     * Adds a new chat message to the list of messages.
     *
     * @param message The content of the chat message.
     * @param isSentByUser Indicates if the message was sent by the user.
     */
    fun addMessage(message: String, isSentByUser: Boolean) {
        _chatMessages.value = _chatMessages.value + ChatMessage(message, isSentByUser)
    }

    /**
     * Resets the chat state by clearing all chat messages.
     */
    fun resetChatState() {
        _chatMessages.value = emptyList()
    }
}