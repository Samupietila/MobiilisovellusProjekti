package com.example.mobiilisovellusprojekti.ViewModels

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.material3.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

class BleViewModel : ViewModel() {
    val scanResults = MutableLiveData<List<ServerDevice>>(emptyList())
    val isScanning = MutableLiveData(false)

    fun scanDevices(context: Context) {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.Manifest.permission.BLUETOOTH_SCAN
        } else {
            android.Manifest.permission.BLUETOOTH
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val aggregator = BleScanResultAggregator()
            isScanning.value = true
            BleScanner(context).scan()
                .map { aggregator.aggregateDevices(it) }
                .onEach { devices ->
                    scanResults.value = devices
                    isScanning.value = false
                }
                .launchIn(viewModelScope)
        } else {
            Log.e("BleViewModel", "Bluetooth is not enabled")
        }
    }
}