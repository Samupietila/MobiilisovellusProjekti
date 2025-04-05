package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiilisovellusprojekti.R
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiilisovellusprojekti.screens.navigation.NavigationScreens

@Composable
fun Home(navController: NavController, modifier: Modifier) {
    var isDarkTheme by remember { mutableStateOf(false) }

    MobiilisovellusProjektiTheme(darkTheme = isDarkTheme) {
        val backgroundColor = MaterialTheme.colorScheme.background

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            // Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
            }

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
                    color = if (isDarkTheme) Color(0xFFEAEAEA) else Color(0xFF292B3B),
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

                // Painikkeet
                Button(
                    onClick = { navController.navigate(NavigationScreens.BTCONNECT.title) },
                    colors = primaryButtonColors(),
                    modifier = Modifier.width(250.dp)
                ) {
                    Text("Start", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.width(250.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { /* Toiminto tähän */ },
                        colors = secondaryButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Contacts", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { /* Toiminto tähän */ },
                        colors = secondaryButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("History", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    Home(navController = navController, modifier = Modifier)
}
