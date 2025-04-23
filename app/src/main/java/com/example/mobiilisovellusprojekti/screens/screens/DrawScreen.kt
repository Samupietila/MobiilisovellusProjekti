package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.ViewModels.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DrawScreen(navController: NavController, modifier: Modifier) {
    val drawingViewModel = viewModel<DrawingViewModel>()
    val drawingState by drawingViewModel.state.collectAsStateWithLifecycle()

    val wordViewModel = viewModel<WordViewModel>()
    val word by wordViewModel.randomWord.collectAsState()

    LaunchedEffect(Unit) {
        wordViewModel.getRandomWord()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Piirrä: ${word?.word ?: "Ladataan..."}")

        DrawingCanvas(
            paths = drawingState.paths,
            currentPath = drawingState.currentPath,
            onAction = drawingViewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
        CanvasControls(
            selectedColor = drawingState.selectedColor,
            colors = allColors,
            onSelectColor = {
                drawingViewModel.onAction(DrawingAction.OnSelectColor(it))
            },
            onClearCanvas = {
                drawingViewModel.onAction(DrawingAction.OnClearCanvasClick)
            },
        )
    }
}
