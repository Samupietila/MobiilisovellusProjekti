package com.example.mobiilisovellusprojekti.screens.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiilisovellusprojekti.screens.screens.BTConnect
import com.example.mobiilisovellusprojekti.screens.screens.Contacts
import com.example.mobiilisovellusprojekti.screens.screens.DrawScreen
import com.example.mobiilisovellusprojekti.screens.screens.GameScreen
import com.example.mobiilisovellusprojekti.screens.screens.History
import com.example.mobiilisovellusprojekti.screens.screens.Home
import com.example.mobiilisovellusprojekti.screens.screens.NewProfile
import com.example.mobiilisovellusprojekti.screens.screens.Player
import com.example.mobiilisovellusprojekti.screens.screens.GameStatistics
import androidx.compose.ui.Modifier
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.ViewModels.GameViewModel
import com.example.mobiilisovellusprojekti.screens.screens.GuessScreen
import com.example.mobiilisovellusprojekti.screens.screens.Test
import com.example.mobiilisovellusprojekti.screens.screens.WordScreen


enum class NavigationScreens(val title: String) {
    HOME("Home"),
    BTCONNECT("BTConnect"),
    CONTACTS("Contacts"),
    DRAWSCREEN("DrawScreen"),
    GAMESCREEN("GameScreen"),
    HISTORY("History"),
    NEWPROFILE("NewProfile"),
    PLAYER("Player"),
    STATISTICS("GameStatistics"),
    TEST("Test"),
    WORD("Word"),
    GUESSSCREEN("GuessScreen")
}


// IMPLEMENTOI BACKSTACK JA SEN KÄYTTÖLIITTYMÄ???
@Composable
fun Navigation(modifier: Modifier,
               bleViewModel: BleViewModel,
               chatViewModel: ChatViewModel,
               drawingViewModel: DrawingViewModel,
               gameViewModel: GameViewModel
            ) {
    val navController = rememberNavController()
    val isDarkTheme = isSystemInDarkTheme()

    fun resetAll() {
        // Reset all ViewModels to their initial state
        bleViewModel.resetBleViewModel()
        chatViewModel.resetChatState()
        drawingViewModel.resetDrawingState()
        gameViewModel.resetGameState()
    }

    NavHost(
        navController = navController,
        startDestination = NavigationScreens.TEST.title
    ) {

        composable(NavigationScreens.WORD.title) {WordScreen(
            navController,
            modifier,
            isDarkTheme) }

        composable(NavigationScreens.HOME.title) {Home(
            navController,
            modifier,
            isDarkTheme)}

        composable(NavigationScreens.BTCONNECT.title) { BTConnect(
            navController,
            modifier,
            bleViewModel,
            chatViewModel,
            drawingViewModel,
            gameViewModel,
            isDarkTheme) }

        composable(NavigationScreens.CONTACTS.title) { Contacts(
            navController,
            modifier,
            isDarkTheme) }



        composable(NavigationScreens.GAMESCREEN.title) { GameScreen(
            navController, modifier, bleViewModel, chatViewModel,
            gameViewModel,
            drawingViewModel,
            isDarkTheme
        ) }

        composable(NavigationScreens.HISTORY.title) { History(
            navController,
            modifier,
            isDarkTheme) }

        composable(NavigationScreens.NEWPROFILE.title) { NewProfile(
            navController,
            modifier,
            isDarkTheme) }

        composable(NavigationScreens.PLAYER.title) { Player(
            navController,
            modifier,
            isDarkTheme) }

        composable(NavigationScreens.STATISTICS.title) { GameStatistics(
            navController,
            modifier,
            isDarkTheme) }

        composable(NavigationScreens.TEST.title) { Test(
            navController,
            modifier,
            isDarkTheme) }


        composable(NavigationScreens.DRAWSCREEN.title) { DrawScreen(
            navController,
            modifier,
            onBackToHome = {
                // Navigate back to Home when "Back to Home" is pressed
                navController.navigate(NavigationScreens.HOME.title) {
                    resetAll()
                    // Pop the back stack to avoid going back to the Guess screen
                    popUpTo(NavigationScreens.GUESSSCREEN.title) { inclusive = true }
                }
            },
            onPlayAgain = {
                // Navigate to BTConnect when "Play Again" is pressed
                navController.navigate(NavigationScreens.BTCONNECT.title) {
                    resetAll()
                    // Pop the back stack to avoid going back to the Guess screen
                    popUpTo(NavigationScreens.GUESSSCREEN.title) { inclusive = true }
                }
            },
            bleViewModel,
            chatViewModel,
            drawingViewModel,
            gameViewModel,
            isDarkTheme
        ) }


        // Handle both Back to Home and Play Again button navigation
        composable(NavigationScreens.GUESSSCREEN.title) {
            GuessScreen(
                modifier = modifier,
                onBackToHome = {
                    // Navigate back to Home when "Back to Home" is pressed
                    navController.navigate(NavigationScreens.HOME.title) {
                        resetAll()
                        // Pop the back stack to avoid going back to the Guess screen
                        popUpTo(NavigationScreens.GUESSSCREEN.title) { inclusive = true }
                    }
                },
                onPlayAgain = {
                    // Navigate to BTConnect when "Play Again" is pressed
                    navController.navigate(NavigationScreens.BTCONNECT.title) {
                    resetAll()
                        // Pop the back stack to avoid going back to the Guess screen
                        popUpTo(NavigationScreens.GUESSSCREEN.title) { inclusive = true }
                    }
                },
                drawingViewModel,
                navController,
                bleViewModel,
                chatViewModel,
                isDarkTheme,
                gameViewModel
            )
        }
    }
}
