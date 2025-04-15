package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors


@Composable
fun GuessScreen(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf("") }
    var guesses by remember { mutableStateOf(listOf<String>()) }
    var isDarkTheme by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(8.dp),
                reverseLayout = true
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
                    .fillMaxWidth()
                    .imePadding(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (message.isNotBlank()) {
                            guesses = guesses + message
                            println("Submitted guess: $message")
                            message = ""
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teeman vaihtonappi testausta varten (poistetaan myöhemmin)
            Button(
                onClick = { isDarkTheme = !isDarkTheme },
                modifier = Modifier.fillMaxWidth(),
                colors = primaryButtonColors()
            ) {
                Text(text = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme")
            }
        }
    }
}
