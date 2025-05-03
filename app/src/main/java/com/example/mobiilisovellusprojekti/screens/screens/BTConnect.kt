package com.example.mobiilisovellusprojekti.screens.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.mobiilisovellusprojekti.Permissions.*
import com.example.mobiilisovellusprojekti.ViewModels.AdvertisingState
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel
import com.example.mobiilisovellusprojekti.ViewModels.DrawingViewModel
import com.example.mobiilisovellusprojekti.screens.navigation.NavigationScreens
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.ui.theme.primaryButtonColors
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mobiilisovellusprojekti.ViewModels.GameViewModel
import java.util.UUID
import kotlin.text.compareTo
import kotlin.toString

@Composable
fun BTConnect(navController: NavController,
              modifier: Modifier,
              bleViewModel: BleViewModel,
              chatViewModel: ChatViewModel,
              drawingViewModel: DrawingViewModel,
              gameViewModel: GameViewModel,
              isDarkTheme: Boolean
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val activity = context as Activity
            if (!hasBluetoothPermissions(context)) {
                requestPermissions(activity)
            }
        }

        if (!bleViewModel.isServerInitialized()) {
            bleViewModel.initializeChatBleServer(context)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val devices by bleViewModel.scanResults.observeAsState(emptyList())
    val isScanning by bleViewModel.isScanning.observeAsState(false)
    val advertisingState by bleViewModel.advertisingState.collectAsState(AdvertisingState(isAdvertising = false))




    MobiilisovellusProjektiTheme(darkTheme = isDarkTheme) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {

            // Otsikko
            Text(
                text = "Find a player",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Choose to host or join a game via Bluetooth.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Host Button
            Button(
                onClick = {
                    bleViewModel.startAdvertising(context, chatViewModel, drawingViewModel, gameViewModel) {
                        bleViewModel.isHost.value = true
                        navController.navigate(NavigationScreens.DRAWSCREEN.title)
                    }
                },
                enabled = !isScanning,
                colors = primaryButtonColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium

            ) {
                Text(
                    text = if (advertisingState.isAdvertising) "Waiting for player..." else "Host a Game",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Join Button
            Button(
                onClick = {
                    bleViewModel.clearScanResults()
                    bleViewModel.scanDevices(context)
                          },
                enabled = !isScanning,
                colors = primaryButtonColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium

            ) {
                Text(
                    text = if (isScanning) "Searching for hosts..." else "Join a Game",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Bluetooth Devices List
            LazyColumn {
                items(devices) { result ->
                    val name = result.name ?: "Unknown"
                    val address = result.address

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                coroutineScope.launch {
                                    try {
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
                            }
                            .shadow(5.dp, shape = MaterialTheme.shapes.medium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
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
                }
            }
        }
    }
