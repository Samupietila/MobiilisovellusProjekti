package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp)
                .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)


        ) {
            Text(
                text = "Draw: ${word?.word ?: "..."}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSecondary,
                    letterSpacing = (1.5).sp
                )
            )

        }

        Spacer(modifier = Modifier.height(10.dp))

        DrawingCanvas(
            paths = drawingState.paths,
            currentPath = drawingState.currentPath,
            onAction = drawingViewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface)

        )
        Spacer(modifier = Modifier.height(10.dp))


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
