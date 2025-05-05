package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    navController: NavController,
    modifier: Modifier,
    bleViewModel: BleViewModel,
    chatViewModel: ChatViewModel,
    gameViewModel: GameViewModel,
    drawingViewModel: DrawingViewModel,
    isDarkTheme: Boolean
) {
    var textInput by remember { mutableStateOf("") }
    val chatMessages by chatViewModel.chatMessages.collectAsState()
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = bleViewModel) {
        bleViewModel.observeChatNotifications(
            navController.context, chatViewModel,
            gameViewModel = gameViewModel,
            drawingViewModel = drawingViewModel
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(chatMessages) { chatMessage ->
                Text(
                    text = if (chatMessage.isSentByUser) "You: ${chatMessage.message}" else "Client: ${chatMessage.message}"
                )
            }
        }


        // TextField for user input
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Enter your message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to send the message
        Button(
            onClick = {
                if (!isSending) {
                    isSending = true // Set the flag to true before starting the coroutine
                    bleViewModel.viewModelScope.launch {
                        try {
                            bleViewModel.sendMessage(textInput, chatViewModel) // Pass the textInput to the function
                            textInput = "" // Clear the input field after sending
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isSending = false // Reset the flag after sending
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send to Client")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isSending) {
                    isSending = true // Set the flag to true before starting the coroutine
                    bleViewModel.viewModelScope.launch {
                        try {
                            bleViewModel.sendMessage(textInput, chatViewModel) // Pass the textInput to the function
                            textInput = "" // Clear the input field after sending
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isSending = false // Reset the flag after sending
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send to Server")
        }




    }
}