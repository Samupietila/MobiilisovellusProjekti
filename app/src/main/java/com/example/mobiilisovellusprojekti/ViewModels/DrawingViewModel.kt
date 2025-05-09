package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.ByteBuffer
import java.nio.ByteOrder
/**
 * Represents a drawing color, including a flag to denote if it is an eraser.
 *
 * @param color The [Color] of the drawing.
 * @param isEraser Indicates if the color is an eraser.
 */
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
/**
 * Represents the state of the drawing, including the selected color, current path, and list of paths.
 *
 * @param selectedColor The currently selected [DrawingColor].
 * @param currentPath The current drawing path, if any.
 * @param paths The list of completed drawing paths.
 */
data class DrawingState(
    val selectedColor: DrawingColor = allColors.first(),
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList()
)
/**
 * Represents a drawing path, including its ID, color, and list of points.
 *
 * @param id The unique ID of the path.
 * @param color The color of the path.
 * @param path The list of [Offset] points in the path.
 */
data class PathData(
    val id: String,
    val color: Color,
    val path: List<Offset>
)
/**
 * Represents actions that can be performed on the drawing canvas.
 */
sealed interface DrawingAction {
    /** Action to start a new path. */
    data object OnNewPathStart : DrawingAction

    /** Action to draw a point at the given offset. */
    data class OnDraw(val offset: Offset) : DrawingAction

    /** Action to end the current path. */
    data object OnPathEnd : DrawingAction

    /** Action to select a new drawing color. */
    data class OnSelectColor(val color: DrawingColor) : DrawingAction

    /** Action to clear the canvas. */
    data object OnClearCanvasClick : DrawingAction
}
/**
 * A class representing the ViewModel for handling drawing-related state and actions.
 * This ViewModel manages user interactions, including selecting colors, drawing paths, clearing the canvas,
 * and serializing/deserializing path data.
 */
class DrawingViewModel : ViewModel(){
    /**
     * The internal state of the drawing, represented as a [MutableStateFlow].
     */
    private val _state = MutableStateFlow(DrawingState())
    /**
     * Publicly exposed state as a StateFlow for observing changes.
     */
    val state = _state.asStateFlow()

    /**
     * Handles various drawing actions such as starting a path, drawing, selecting a color, and clearing the canvas.
     *
     * @param action The action to perform, represented as a [DrawingAction].
     * @param bleViewModel The BLE view model for handling Bluetooth-related functionality.
     */
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
    /**
     * Clears the canvas by resetting the current path and list of paths.
     */
    fun onClearCanvas() {
        _state.update {
            it.copy(currentPath = null, paths = emptyList())
        }
    }
    /**
     * Updates the selected drawing color.
     *
     * @param color The [DrawingColor] to set as the selected color.
     */
    private fun onSelectColor(color: DrawingColor) {
        _state.update { it.copy(selectedColor = color) }
    }
    /**
     * Adds a new point to the current drawing path.
     *
     * @param offset The [Offset] of the point to add to the path.
     */
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
    /**
     * Starts a new drawing path with the currently selected color.
     */
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
    /**
     * Ends the current drawing path and sends the data to the server via the BLE ViewModel.
     *
     * @param bleViewModel The BLE view model for sending path data to the server.
     */
    private fun onPathEnd(bleViewModel: BleViewModel) {
        val currentPathData = state.value.currentPath ?: return
        _state.update {
            it.copy(
                currentPath = null,
                paths = it.paths + currentPathData
            )
        }
        // Send Path to the server
        bleViewModel.sendCoordinatesToServer(
            _state.value,
            drawingViewModel = this,
        )


    }
    /**
     * Updates the paths with the given [PathData].
     *
     * @param newPaths The new [PathData] to add to the list of paths.
     */
    fun updatePaths(newPaths: PathData) {
        Log.d("DWM", "updatePaths triggered")
        Log.d("DWM",newPaths.toString())

        _state.update { it.copy(paths = it.paths + newPaths) }
    }
    /**
     * Serializes the given [PathData] to a binary format.
     *
     * @param data The [PathData] to serialize.
     * @return A byte array containing the serialized data.
     */
    fun serializePathDataBinary(data: PathData): ByteArray {
        // bufferSize = id + color(RGBA) + path.size + each Offset point * all points (in bytes)
        val bufferSize = 8 + 4 * 4 + 4 + data.path.size * 8
        val buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(data.id.toLong())
        buffer.putFloat(data.color.red)
        buffer.putFloat(data.color.green)
        buffer.putFloat(data.color.blue)
        buffer.putFloat(data.color.alpha)
        buffer.putInt(data.path.size)

        data.path.forEach { offset ->
            buffer.putFloat(offset.x)
            buffer.putFloat(offset.y)
        }

        return buffer.array()
    }

    /**
     * Deserializes binary data into a [PathData] object.
     *
     * @param bytes The byte array containing serialized path data.
     * @return The deserialized [PathData] object.
     */
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
    /**
     * Resets the drawing state to its initial values.
     */
    fun resetDrawingState() {
        _state.value = DrawingState()
    }

}
