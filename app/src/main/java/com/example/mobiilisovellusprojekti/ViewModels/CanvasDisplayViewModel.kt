package com.example.mobiilisovellusprojekti.ViewModels

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import java.util.UUID


class DisplayBleManager(
    context: Context,
    scanner: BleScanner
) {
    private val SERVICE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("0000cdef-0000-1000-8000-00805f9b34fb")
}

class CanvasDisplayViewModel {
}
