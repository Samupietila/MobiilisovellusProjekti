package com.example.mobiilisovellusprojekti.screens.screens

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.example.mobiilisovellusprojekti.Permissions.*
import com.example.mobiilisovellusprojekti.ViewModels.AdvertisingState
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.secondaryButtonColors
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.screens.navigation.NavigationScreens
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.text.compareTo
import kotlin.toString

@Composable
fun BTConnect(navController: NavController, modifier: Modifier, bleViewModel: BleViewModel, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel) {

    val context = LocalContext.current


    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val activity = context as Activity
            if (!hasBluetoothPermissions(context)) {
                requestPermissions(activity)
            }
        }
        bleViewModel.initializeChatBleServer(context, chatViewModel)
    }

    val coroutineScope = rememberCoroutineScope()
    var darkTheme by remember { mutableStateOf(false) }
    val devices by bleViewModel.scanResults.observeAsState(emptyList())
    val isScanning by bleViewModel.isScanning.observeAsState(false)
    val advertisingState by bleViewModel.advertisingState.collectAsState(AdvertisingState(isAdvertising = false))

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
            Text(
                text = "${UUID.fromString("a902a33a-7a3a-4937-b4bf-b0cd141346b5")}"
            )
            // Aloita Advertising
            Button(
                onClick = { bleViewModel.startAdvertising(context, chatViewModel, drawingViewModel) {
                    bleViewModel.isHost.value = true
                    navController.navigate(NavigationScreens.DRAWSCREEN.title)
                } },
                enabled = !isScanning,
                colors = primaryButtonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (advertisingState.isAdvertising == true) "Advertising" else "Advertise", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))


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
                            .padding(8.dp)
                            .clickable {
                                Log.d("Clicked me!!!","Hello!?")
                                coroutineScope.launch {
                                    try {
                                        Log.d("Test", result.toString())
                                        val isConnected = bleViewModel.connectToDevice(context, result)
                                        if (isConnected) {
                                            navController.navigate(NavigationScreens.GUESSSCREEN.title)
                                        } else {
                                            Log.e("BTConnect", "Failed to connect to device: ${result.name ?: "Unknown"}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("BTConnect", "Error during connection: ${e.message}")
                                    }
                                }
                            },
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