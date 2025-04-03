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

enum class NavigationScreens(val title: String) {
    BTCONNECT("BTConnect"),
    CONTACTS("Contacts"),
    DRAWSCREEN("DrawScreen"),
    GAMESCREEN("GameScreen"),
    HISTORY("History"),
    NEWPROFILE("NewProfile"),
    PLAYER("Player"),
    STATISTICS("GameStatistics")
}


// IMPLEMENTOI BACKSTACK JA SEN KÄYTTÖLIITTYMÄ???
@Composable
fun Navigation(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home(navController, modifier)
        ) {
        composable(NavigationScreens.BTCONNECT.title) { navController.navigate(BTConnect(navController, modifier)) }
        composable(NavigationScreens.CONTACTS.title) { navController.navigate(Contacts(navController, modifier)) }
        composable(NavigationScreens.DRAWSCREEN.title) { navController.navigate(DrawScreen(navController, modifier)) }
        composable(NavigationScreens.GAMESCREEN.title) { navController.navigate(GameScreen(navController, modifier)) }
        composable(NavigationScreens.HISTORY.title) { navController.navigate(History(navController, modifier)) }
        composable(NavigationScreens.NEWPROFILE.title) { navController.navigate(NewProfile(navController, modifier)) }
        composable(NavigationScreens.PLAYER.title) { navController.navigate(Player(navController, modifier)) }
        composable(NavigationScreens.STATISTICS.title) { navController.navigate(GameStatistics(navController, modifier)) }

    }
}