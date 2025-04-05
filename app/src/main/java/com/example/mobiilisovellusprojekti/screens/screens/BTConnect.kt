package com.example.mobiilisovellusprojekti.screens.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel

@Composable
fun BTConnect(navController: NavController, modifier: Modifier, bleViewModel: BleViewModel) {

    val context = LocalContext.current
    val devices by bleViewModel.scanResults.observeAsState(emptyList())
    val isScanning by bleViewModel.isScanning.observeAsState(false)


    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp))
    {
        Button(
            onClick = { bleViewModel.scanDevices(context) },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isScanning) "Scanning..." else "Start Scanning")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(devices) { result ->
                val name = result.name ?: "Unknown"
                val address = result.address

                Text(
                    text = "$name\n$address",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

    }


}