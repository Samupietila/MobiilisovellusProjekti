package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.ViewModels.WordViewModel
import com.example.mobiilisovellusprojekti.data.Word
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors

@Composable
fun WordScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: WordViewModel = viewModel()
) {
    var isDarkTheme by remember { mutableStateOf(false) }

    MobiilisovellusProjektiTheme(darkTheme = isDarkTheme) {
        val wordList by viewModel.allWords.collectAsStateWithLifecycle()

        var wordInput by remember { mutableStateOf("") }
        var difficultyInput by remember { mutableStateOf("") }

        val colors = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Add Word",
                    style = typography.titleLarge,
                    color = colors.primary,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = wordInput,
                    onValueChange = { wordInput = it },
                    label = { Text("Word", style = typography.bodyLarge) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = difficultyInput,
                    onValueChange = { difficultyInput = it },
                    label = { Text("Difficulty (e.g. 1–3)", style = typography.bodyLarge) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val difficulty = difficultyInput.toIntOrNull() ?: 1
                        if (wordInput.isNotBlank()) {
                            viewModel.insert(Word(word = wordInput, difficulty = difficulty))
                            wordInput = ""
                            difficultyInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = primaryButtonColors()
                ) {
                    Text("Save", style = typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Saved words:", style = typography.titleLarge, color = colors.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Word", style = typography.labelLarge, color = colors.primary)
                    Text("Difficulty", style = typography.labelLarge, color = colors.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn {
                    items(wordList) { word ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surface,
                                contentColor = colors.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = word.word,
                                    style = typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(colors.primary, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = word.difficulty.toString(),
                                        style = typography.bodyLarge,
                                        color = colors.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isDarkTheme = !isDarkTheme },
                    modifier = Modifier.fillMaxWidth(),
                    colors = secondaryButtonColors()
                ) {
                    Text(text = if (isDarkTheme) "Switch to light" else "Switch to dark")
                }
            }
        }
    }
}
