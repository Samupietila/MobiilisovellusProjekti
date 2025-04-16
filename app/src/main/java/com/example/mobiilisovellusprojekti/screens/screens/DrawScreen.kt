package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingAction
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.allColors

@Composable
fun DrawScreen(navController: NavController, modifier: Modifier) {
    val viewModel = viewModel<DrawingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawingCanvas(paths = state.paths,
            currentPath = state.currentPath,
            // onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { }

        /*
        DrawingCanvas(
            paths = state.paths,
            currentPath = state.currentPath,
            onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
        CanvasControls(
            selectedColor = state.selectedColor,
            colors = allColors,
            onSelectColor = {
                viewModel.onAction(DrawingAction.OnSelectColor(it))
            },
            onClearCanvas = {
                viewModel.onAction((DrawingAction.OnClearCanvasClick))
            },

            )*/
    }
}
