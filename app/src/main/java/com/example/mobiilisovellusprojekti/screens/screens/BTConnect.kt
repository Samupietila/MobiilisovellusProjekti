package com.example.mobiilisovellusprojekti.screens.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme

@Composable
fun BTConnect(navController: NavController, modifier: Modifier, bleViewModel: BleViewModel) {
    var darkTheme by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val devices by bleViewModel.scanResults.observeAsState(emptyList())
    val isScanning by bleViewModel.isScanning.observeAsState(false)

    MobiilisovellusProjektiTheme(darkTheme = darkTheme) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {

            // Otsikko
            Text(
                text = "Bluetooth Devices",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Aloita skannaus
            Button(
                onClick = { bleViewModel.scanDevices(context) },
                enabled = !isScanning,
                colors = primaryButtonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isScanning) "Scanning..." else "Start Scanning", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn {
                items(devices) { result ->
                    val name = result.name ?: "Unknown"
                    val address = result.address

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Teeman vaihtopainike (voi kommentoida pois, kun ei enää tarvitse tällä sivulla)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { darkTheme = !darkTheme },
                colors = secondaryButtonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Toggle Dark Theme", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
