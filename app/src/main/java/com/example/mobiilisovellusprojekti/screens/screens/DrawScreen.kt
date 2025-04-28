package com.example.mobiilisovellusprojekti.screens.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingAction
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.PathData
import com.example.mobiilisovellusprojekti.ViewModels.allColors
import kotlinx.coroutines.flow.update
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun serializePathDataBinary(data: PathData): ByteArray {
    // bufferSize = id + color(RGBA) + path.size + each Offset point * all points (in bytes)
    val bufferSize = 8 + 4 * 4 + 4 + data.path.size * 8
    val buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putLong(data.id.toLong())
    buffer.putFloat(data.color.red.toFloat())
    buffer.putFloat(data.color.green.toFloat())
    buffer.putFloat(data.color.blue.toFloat())
    buffer.putFloat(data.color.alpha.toFloat())
    buffer.putInt(data.path.size)

    data.path.forEach { offset ->
        buffer.putFloat(offset.x.toFloat())
        buffer.putFloat(offset.y.toFloat())
    }

    return buffer.array()

}

fun deserializePathDataBinary(bytes: ByteArray): PathData {
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val id = buffer.long.toString()
    val color = Color(
        buffer.float,
        buffer.float,
        buffer.float,
        buffer.float
    )
    val pathSize = buffer.int
    val path = mutableListOf<Offset>()
    repeat(pathSize) {
        val x = buffer.float
        val y = buffer.float
        path.add(Offset(x,y))
    }
    return PathData(id, color, path)
}

@Composable
fun DrawScreen(navController: NavController, modifier: Modifier, bleViewModel: BleViewModel, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel) {
    val state by drawingViewModel.state.collectAsStateWithLifecycle()


    /*LaunchedEffect(state.paths) {
        Log.d("DBG", "LauchEffect launched")
        Log.d("DBG", "${state.paths.isNotEmpty()}")
        //if (state.paths.isNotEmpty()) {
            val serialized = state.paths.map { serializePathDataBinary(it) }
            val receivedPaths = serialized.map { deserializePathDataBinary(it) }


            receiverViewModel.updatePaths(receivedPaths)
        //}
    }*/


    LaunchedEffect(state.paths) {
        Log.d("DS","Composed")
        Log.d("DS",state.paths.toString())
    }
    LaunchedEffect(key1 = bleViewModel) {
        bleViewModel.observeNotifications(navController.context, chatViewModel)
    }

=======
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

        /*DrawingCanvas(
            paths = receiverState.paths,
            currentPath = receiverState.currentPath,
            // onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {}*/


        DrawingCanvas(
            paths = state.paths,
            currentPath = state.currentPath,
            onAction = { action -> drawingViewModel.onAction(action, bleViewModel) },
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
                drawingViewModel.onAction(DrawingAction.OnSelectColor(it), bleViewModel)
            },
            onClearCanvas = {
                drawingViewModel.onAction((DrawingAction.OnClearCanvasClick), bleViewModel)
                drawingViewModel.onAction(DrawingAction.OnSelectColor(it))
            },
            onClearCanvas = {
                drawingViewModel.onAction(DrawingAction.OnClearCanvasClick)
            },
        )
    }
}
