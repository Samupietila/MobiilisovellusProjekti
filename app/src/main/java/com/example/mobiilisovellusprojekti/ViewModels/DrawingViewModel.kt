package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

class DrawingViewModel : ViewModel(){
    private val _state = MutableStateFlow(DrawingState())
    val state = _state.asStateFlow()

    fun onAction(action: DrawingAction, bleViewModel: BleViewModel) {
        when (action) {
            DrawingAction.OnClearCanvasClick -> onClearCanvas()
            is DrawingAction.OnDraw -> onDraw(action.offset)
            DrawingAction.OnNewPathStart -> onNewPathStart()
            DrawingAction.OnPathEnd -> {
                onPathEnd(
                    bleViewModel = bleViewModel
                )
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

    private fun onPathEnd(bleViewModel: BleViewModel) {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = null,
                paths = it.paths + currentPathData
            )
        }
        //tähän send to server?
        bleViewModel.sendCoordinatesToServer(
            _state.value,
            drawingViewModel = this,
        )


    }

    //testaamiseen ilman bluetoothia tarkoitettu
    fun updatePaths(newPaths: List<PathData>) {
        _state.update { it.copy(paths = newPaths) }
    }

    // byte serialisaatio
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

    // deserialisointi
    fun deserializePathDataBinary(bytes: ByteArray): PathData {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val id = buffer.long.toString()
        val color = Color(
            buffer.float.toInt(),
            buffer.float.toInt(),
            buffer.float.toInt(),
            buffer.float.toInt()
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
}
