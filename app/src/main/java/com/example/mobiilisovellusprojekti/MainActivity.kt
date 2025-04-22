package com.example.mobiilisovellusprojekti

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mobiilisovellusprojekti.ViewModels.BleViewModel
import com.example.mobiilisovellusprojekti.ui.theme.MobiilisovellusProjektiTheme
import com.example.mobiilisovellusprojekti.screens.navigation.Navigation
import com.example.mobiilisovellusprojekti.Permissions.hasBluetoothPermissions
import com.example.mobiilisovellusprojekti.Permissions.requestPermissions
import com.example.mobiilisovellusprojekti.Permissions.PERMISSION_REQUEST_CODE
import com.example.mobiilisovellusprojekti.ViewModels.ChatViewModel

class MainActivity : ComponentActivity() {

    private val bleViewModel: BleViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!hasBluetoothPermissions(this)) {
            requestPermissions(this)
        }

        checkAndEnableBluetooth()

        setContent {
            MobiilisovellusProjektiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding), bleViewModel, chatViewModel)
                }
            }
        }
    }

    private fun checkAndEnableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Prompt the user to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            // Check if permissions are granted
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("Permissions", "All Bluetooth permissions granted")
            } else {
                Log.e("Permissions", "Bluetooth permissions denied")
            }
        }
    }
}