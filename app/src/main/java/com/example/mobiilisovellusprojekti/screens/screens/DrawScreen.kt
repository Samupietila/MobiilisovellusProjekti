package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.R
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingAction
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.GameViewModel
import com.example.mobiilisovellusprojekti.ViewModels.PathData
import com.example.mobiilisovellusprojekti.ViewModels.WordViewModel
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
fun DrawScreen(navController: NavController,
               modifier: Modifier,
               onBackToHome: () -> Unit,
               onPlayAgain: () -> Unit,
               bleViewModel: BleViewModel,
               chatViewModel: ChatViewModel,
               drawingViewModel: DrawingViewModel,
               gameViewModel: GameViewModel,
               isDarkTheme: Boolean
) {


    LaunchedEffect(key1 = bleViewModel) {

        Log.d("LaunchedEffect","Initializing all of the observsations")

        bleViewModel.observeChatNotifications(
            navController.context,
            chatViewModel,
            gameViewModel,
            drawingViewModel
        )
        bleViewModel.observeCordinateNotifications(
            navController.context,
            drawingViewModel)
    }

    val drawingState by drawingViewModel.state.collectAsStateWithLifecycle()
    val wordViewModel = viewModel<WordViewModel>()
    val gameOver = gameViewModel.gameOver.collectAsState()
    val word by wordViewModel.randomWord.collectAsState()

    LaunchedEffect(drawingState.paths) {
        Log.d("DS","Composed")
        Log.d("DS",drawingState.paths.toString())
    }

    LaunchedEffect(Unit) {
        wordViewModel.getRandomWord()
    }

    LaunchedEffect(gameOver.value) {
        Log.d("LE - gameOver", gameOver.value.toString())
    }

    LaunchedEffect(word) {
        word?.word?.let { gameViewModel.setWord(it) }
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
            onAction = { action -> drawingViewModel.onAction(action, bleViewModel) },
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
                drawingViewModel.onAction(DrawingAction.OnSelectColor(it), bleViewModel)
            },
            onClearCanvas = {
                drawingViewModel.onAction((DrawingAction.OnClearCanvasClick), bleViewModel)
            },
            bleViewModel = bleViewModel,
        )
    }

    if (gameOver.value) {
        bleViewModel.sendMessage("GAME_OVER",chatViewModel)
        Dialog(onDismissRequest = { gameViewModel.setGameOver(false) }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 400.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.large)
                        .padding(24.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Star",
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 10.dp),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = "CORRECT!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Navigate to Home
                            onBackToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Back to Home")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            // Navigate to BTConnectScreen
                            onPlayAgain()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Play Again")
                    }
                }
            }
        }

    }


}
