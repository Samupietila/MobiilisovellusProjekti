package com.example.mobiilisovellusprojekti.screens.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.PathData
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.R
import com.example.mobiilisovellusprojekti.ViewModels.GameViewModel

@Composable
fun GuessScreen(
    modifier: Modifier = Modifier,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit,
    drawingViewModel: DrawingViewModel,
    navController: NavController,
    bleViewModel: BleViewModel,
    chatViewModel: ChatViewModel,
    isDarkTheme: Boolean,
    gameViewModel: GameViewModel
) {
    val state by drawingViewModel.state.collectAsStateWithLifecycle()
    val gameOver = gameViewModel.gameOver.collectAsState()
    var message by remember { mutableStateOf("") }
    var guesses by remember { mutableStateOf(listOf<String>()) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = bleViewModel) {
        bleViewModel.observeCordinateNotifications(navController.context, drawingViewModel)
    }

    LaunchedEffect(gameOver.value) {
        Log.d("LE - gameOver", gameOver.value.toString())
    }

    MobiilisovellusProjektiTheme(darkTheme = isDarkTheme) {

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            DrawingCanvas(
                paths = state.paths,
                currentPath = state.currentPath,
                onAction = {},
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth()
                .background(colors.surface)
                .padding(8.dp),
            reverseLayout = false
        ) {
            items(guesses.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.tertiary,
                        contentColor = colors.onTertiary
                    )
                ) {
                    Text(
                        text = guesses[index],
                        style = typography.bodyLarge,
                        color = colors.onTertiary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (gameOver.value) {
            Dialog(onDismissRequest = { gameViewModel.setGameOver(false) }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp, max = 400.dp)
                            .background(Color.White, shape = MaterialTheme.shapes.large)
                            .padding(24.dp)
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Star",
                            modifier = Modifier
                                .size(160.dp)
                                .padding(bottom = 10.dp),
                            contentScale = ContentScale.FillBounds
                        )

                        Text(
                            text = "CORRECT!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Navigate to Home
                                onBackToHome()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Back to Home")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                // Navigate to BTConnectScreen
                                onPlayAgain()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Play Again")
                        }
                    }
                }
            }

        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = {
                        Text("Enter your guess...", style = typography.bodyLarge)
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedPlaceholderColor = colors.onSurface.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = colors.onSurface.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (message.isNotBlank()) {
                                bleViewModel.sendMessage(message, chatViewModel)
                                guesses = guesses + message
                                message = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            bleViewModel.sendMessage(message, chatViewModel)
                            guesses = guesses + message
                            println("Submitted guess: $message")
                            message = ""
                            focusManager.clearFocus()
                        }
                    },
                    colors = primaryButtonColors(),
                    modifier = Modifier.height(56.dp),
                    enabled = message.isNotBlank(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Send")
                }
            }
        }
    }

    LaunchedEffect(guesses.size) {
        if (guesses.isNotEmpty()) {
            listState.animateScrollToItem(guesses.size - 1)
        }
    }
}}
