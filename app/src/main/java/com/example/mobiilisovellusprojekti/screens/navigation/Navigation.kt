package com.example.mobiilisovellusprojekti.screens.navigation

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
import com.example.mobiilisovellusprojekti.screens.screens.Test

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
    TEST("Test")
}


// IMPLEMENTOI BACKSTACK JA SEN KÄYTTÖLIITTYMÄ???
@Composable
fun Navigation(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationScreens.TEST.title
    ) {
        composable(NavigationScreens.HOME.title) {Home(navController, modifier)}
        composable(NavigationScreens.BTCONNECT.title) { BTConnect(navController, modifier) }
        composable(NavigationScreens.CONTACTS.title) { Contacts(navController, modifier) }
        composable(NavigationScreens.DRAWSCREEN.title) { DrawScreen(navController, modifier) }
        composable(NavigationScreens.GAMESCREEN.title) { GameScreen(navController, modifier) }
        composable(NavigationScreens.HISTORY.title) { History(navController, modifier) }
        composable(NavigationScreens.NEWPROFILE.title) { NewProfile(navController, modifier) }
        composable(NavigationScreens.PLAYER.title) { Player(navController, modifier) }
        composable(NavigationScreens.STATISTICS.title) { GameStatistics(navController, modifier) }
        composable(NavigationScreens.TEST.title) { Test(navController, modifier) }
    }
}