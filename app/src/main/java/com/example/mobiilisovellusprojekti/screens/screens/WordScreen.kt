package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun WordScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: WordViewModel = viewModel()
)
 {
    val wordList by viewModel.allWords.collectAsStateWithLifecycle()

    var wordInput by remember { mutableStateOf("") }
    var difficultyInput by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = wordInput,
            onValueChange = { wordInput = it },
            label = { Text("Sana") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = difficultyInput,
            onValueChange = { difficultyInput = it },
            label = { Text("Vaikeustaso (esim. 1–3)") },
            modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Tallenna sana")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tallennetut sanat:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(wordList) { word ->
                Text("- ${word.word} (vaikeustaso ${word.difficulty})")
            }
        }
    }
}
