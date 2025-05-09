package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.example.mobiilisovellusprojekti.ViewModels.DrawingColor
import com.example.mobiilisovellusprojekti.R
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors

/**
 * UI components for controlling a canvas, including color selection and clearing the canvas.
 */

/**
 * A composable function that renders the canvas control UI, allowing the user to select colors
 * and clear the canvas. Integrates with a BLE view model for additional functionality.
 *
 * @param selectedColor The currently selected drawing color.
 * @param colors A list of available drawing colors.
 * @param onSelectColor A callback function invoked when a color is selected.
 * @param onClearCanvas A callback function invoked to clear the canvas.
 * @param modifier A [Modifier] to customize the layout.
 * @param bleViewModel An instance of [BleViewModel] used for BLE-related actions.
 */
@Composable
fun ColumnScope.CanvasControls(
    selectedColor: DrawingColor,
    colors: List<DrawingColor>,
    onSelectColor: (DrawingColor) -> Unit,
    onClearCanvas: () -> Unit,
    modifier: Modifier = Modifier,
    bleViewModel: BleViewModel
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        colors.fastForEach { drawingColor ->
            val isSelected = selectedColor == drawingColor

            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectColor(drawingColor) },
                contentAlignment = Alignment.Center
            ) {
                DrawingColorItem(drawingColor, isSelected)

                if (drawingColor.isEraser) {
                    Image(
                        painter = painterResource(id = R.drawable.eraser),
                        contentDescription = "Eraser",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    Button(
        onClick = {
            onClearCanvas()
            bleViewModel.clearCanvas()
        },
        modifier = Modifier
            .padding(11.dp)
            .navigationBarsPadding(),
        colors = secondaryButtonColors(),
        shape = MaterialTheme.shapes.medium
    ) {
        Text("Clear Canvas")
    }
}

/**
 * A composable function that renders a single color item for selection. Displays the selected
 * state with a border and treats eraser colors with a special background.
 *
 * @param drawingColor An instance of [DrawingColor] representing the color or eraser to display.
 * @param isSelected A boolean value indicating whether this color is currently selected.
 */

@Composable
fun DrawingColorItem(
    drawingColor: DrawingColor,
    isSelected: Boolean
) {
    val isDarkTheme = isSystemInDarkTheme()


    val borderColor = when {
        isSelected -> {
            if (isDarkTheme) Color.Black else Color.Transparent
        }
        isDarkTheme -> Color.White
        else -> Color.Black
    }

    val backgroundColor = if (drawingColor.isEraser) {
        Color.White
    } else {
        drawingColor.color
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .size(40.dp)
    )
}
