package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.mobiilisovellusprojekti.screens.navigation.NavigationScreens

@Composable
fun Test(navController: NavController, modifier: Modifier) {
  
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      
         Button(onClick = {
            navController.navigate(NavigationScreens.WORD.title)
        }
               ) {
            Text("Go to Word Screen")
        }
      
        Button(onClick = {
            navController.navigate(NavigationScreens.HOME.title)
        }, modifier = Modifier) {
            Text("Go to Home")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.GAMESCREEN.title)
        }, modifier = Modifier) {
            Text("Go to Game Screen")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.BTCONNECT.title)
        }, modifier = Modifier) {
            Text("Go to BT Connect")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.CONTACTS.title)
        }, modifier = Modifier) {
            Text("Go to Contacts")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.DRAWSCREEN.title)
        }, modifier = Modifier) {
            Text("Go to Draw Screen")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.HISTORY.title)
        }, modifier = Modifier) {
            Text("Go to History")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.NEWPROFILE.title)
        }, modifier = Modifier) {
            Text("Go to New Profile")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.PLAYER.title)
        }, modifier = Modifier) {
            Text("Go to Player")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.STATISTICS.title)
        }, modifier = Modifier) {
            Text("Go to Statistics")
        }

        Button(onClick = {
            navController.navigate(NavigationScreens.GUESS.title)
        }, modifier = Modifier) {
            Text("Go to Guess Screen")
        }
    }
}