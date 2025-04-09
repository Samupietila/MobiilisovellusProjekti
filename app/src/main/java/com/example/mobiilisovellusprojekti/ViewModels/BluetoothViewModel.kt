package com.example.mobiilisovellusprojekti.ViewModels

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import java.util.UUID
import kotlin.collections.isNotEmpty

class BleViewModel : ViewModel() {
    companion object {
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }

    val scanResults = MutableLiveData<List<ServerDevice>>(emptyList())
    val isScanning = MutableLiveData(false)
    val connectionState = MutableLiveData<String>()
    val _bpmValue = MutableLiveData<Int>()

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

    suspend fun connectToDevice(context: Context, device: ServerDevice) {
        try {
            Log.d("ConnectToDevice", "Attempting to connect to device: ${device.name ?: "Unknown"}")
            val connection = ClientBleGatt.connect(context, device.address, viewModelScope)
            Log.d("ConnectToDevice", "Connection established")

            val services = connection.discoverServices()
            Log.d("ConnectToDevice", "Services discovered: ${services.services.size}")

            connectionState.postValue("Connected to ${device.name ?: "Unknown"}")

            val heartRateService = services.findService(HEART_RATE_SERVICE_UUID)
            if (heartRateService == null) {
                Log.e("ConnectToDevice", "Heart Rate Service not found")
                return
            }

            val bpmCharacteristic = heartRateService.findCharacteristic(HEART_RATE_MEASUREMENT_UUID)
            if (bpmCharacteristic == null) {
                Log.e("ConnectToDevice", "Heart Rate Measurement Characteristic not found")
                return
            }

            Log.d("ConnectToDevice", "Found characteristic: $bpmCharacteristic")

            if (bpmCharacteristic.properties.contains(BleGattProperty.PROPERTY_NOTIFY) ||
                bpmCharacteristic.properties.contains(BleGattProperty.PROPERTY_INDICATE)) {
                try {
                    Log.d("ConnectToDevice", "Enabling notifications/indications")
                    bpmCharacteristic.getNotifications()
                        .onEach { data ->
                            val bpmDataValue = data.value

                            if (bpmDataValue != null && bpmDataValue.isNotEmpty()) {
                                val bpmValue = bpmDataValue[1].toInt()
                                _bpmValue.value = bpmValue
                                Log.d("ConnectToDevice", "Parsed BPM Value: $bpmValue")
                            } else {
                                Log.e("ConnectToDevice", "No data received or characteristic value is empty")
                            }
                        }
                        .launchIn(viewModelScope)
                } catch (e: Exception) {
                    Log.e("ConnectToDevice", "Error enabling notifications: ${e.message}")
                }
            } else {
                Log.e("ConnectToDevice", "Characteristic does not support notifications or indications")
            }
        } catch (e: Exception) {
            Log.e("ConnectToDevice", "Error during connection: ${e.message}")
        }
    }

}