package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DrawingColor(
    val color: Color,
    val isEraser: Boolean = false
)

val allColors = listOf(
    DrawingColor(Color.Black),
    DrawingColor(Color.Red),
    DrawingColor(Color.Blue),
    DrawingColor(Color.Yellow),
    DrawingColor(Color.Green),
    DrawingColor(Color.White, isEraser = true)
)

data class DrawingState(
    val selectedColor: DrawingColor = allColors.first(),
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList()
)

data class PathData(
    val id: String,
    val color: Color,
    val path: List<Offset>
)

sealed interface DrawingAction {
    data object OnNewPathStart : DrawingAction
    data class OnDraw(val offset: Offset) : DrawingAction
    data object OnPathEnd : DrawingAction
    data class OnSelectColor(val color: DrawingColor) : DrawingAction
    data object OnClearCanvasClick : DrawingAction
}

class DrawingViewModel : ViewModel() {
    private val _state = MutableStateFlow(DrawingState())
    val state = _state.asStateFlow()

    fun onAction(action: DrawingAction) {
        when (action) {
            DrawingAction.OnClearCanvasClick -> onClearCanvas()
            is DrawingAction.OnDraw -> onDraw(action.offset)
            DrawingAction.OnNewPathStart -> onNewPathStart()
            DrawingAction.OnPathEnd -> {
                onPathEnd()
                // tähän send coordinates to server?
            }
            is DrawingAction.OnSelectColor -> onSelectColor(action.color)
        }
    }

    private fun onClearCanvas() {
        _state.update {
            it.copy(currentPath = null, paths = emptyList())
        }
    }

    private fun onSelectColor(color: DrawingColor) {
        _state.update { it.copy(selectedColor = color) }
    }

    private fun onDraw(offset: Offset) {
        val currentPath = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = currentPath.copy(
                    path = currentPath.path + offset
                )
            )
        }
    }

    private fun onNewPathStart() {
        val currentColor = state.value.selectedColor
        _state.update {
            it.copy(
                currentPath = PathData(
                    id = System.currentTimeMillis().toString(),
                    color = currentColor.color,
                    path = emptyList()
                )
            )
        }
    }

    private fun onPathEnd() {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = null,
                paths = it.paths + currentPathData
            )
        }
        Log.d("DBG","${_state.value.paths}")
        Log.d("DBG","${_state.value.paths.size}")
    }
}
