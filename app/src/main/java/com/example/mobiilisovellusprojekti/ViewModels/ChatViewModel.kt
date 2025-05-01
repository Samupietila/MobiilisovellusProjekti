package com.example.mobiilisovellusprojekti.ViewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatMessage(
    val message: String,
    val isSentByUser: Boolean
)

class ChatViewModel : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun addMessage(message: String, isSentByUser: Boolean) {
        _chatMessages.value = _chatMessages.value + ChatMessage(message, isSentByUser)
    }

    fun resetChatState() {
        _chatMessages.value = emptyList()
    }
}