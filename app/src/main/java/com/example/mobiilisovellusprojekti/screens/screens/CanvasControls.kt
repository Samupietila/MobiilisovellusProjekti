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
