package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiilisovellusprojekti.R
import com.example.mobiilisovellusprojekti.screens.navigation.NavigationScreens
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun Home(navController: NavController, modifier: Modifier = Modifier, isDarkTheme: Boolean) {

    MobiilisovellusProjektiTheme(darkTheme = isDarkTheme) {
        val colors = MaterialTheme.colorScheme

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "GUESS MY DOODLE",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start Button
                Button(
                    onClick = { navController.navigate(NavigationScreens.BTCONNECT.title) },
                    colors = primaryButtonColors(),
                    modifier = Modifier.width(250.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Start Game", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))


            }
        }
    }
}

