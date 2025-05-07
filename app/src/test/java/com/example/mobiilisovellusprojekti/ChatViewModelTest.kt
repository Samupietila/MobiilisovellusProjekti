package com.example.mobiilisovellusprojekti.ViewModels

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        viewModel = ChatViewModel()
    }

    @Test
    fun addMessage_shouldAddMessageToList() = runTest {
        viewModel.addMessage("Hello!", isSentByUser = true)

        val messages = viewModel.chatMessages.value
        assertThat(messages).hasSize(1)
        assertThat(messages.first()).isEqualTo(ChatMessage("Hello!", true))
    }

    @Test
    fun addMultipleMessages_shouldMaintainOrder() = runTest {
        viewModel.addMessage("First", true)
        viewModel.addMessage("Second", false)

        val messages = viewModel.chatMessages.value
        assertThat(messages).hasSize(2)
        assertThat(messages[0]).isEqualTo(ChatMessage("First", true))
        assertThat(messages[1]).isEqualTo(ChatMessage("Second", false))
    }

    @Test
    fun resetChatState_shouldClearMessages() = runTest {
        viewModel.addMessage("Temp", true)
        viewModel.resetChatState()

        assertThat(viewModel.chatMessages.value).isEmpty()
    }
}
