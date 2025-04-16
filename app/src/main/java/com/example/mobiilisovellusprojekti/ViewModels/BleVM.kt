package com.example.mobiilisovellusprojekti.ViewModels
/*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

private var advertisingCallback: AdvertiseCallback? = null

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun startAdvertising() = suspendCancellableCoroutine { continuation ->
    advertisingCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            continuation.resume(Unit) { }
        }

        /*override fun onStartFailure(errorCode: Int) {
            continuation.resumeWithException(AdvertisingException(errorCode))
        }*/
    }

    continuation.invokeOnCancellation {
        bluetoothLeAdvertiser.stopAdvertising(advertisingCallback)
    }

    val advertisingSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setConnectable(true)
        .build()

    val advertisingData = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE))
        .build()

    val scanResponse = AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .build()

    bluetoothLeAdvertiser.startAdvertising(
        advertisingSettings,
        advertisingData,
        scanResponse,
        advertisingCallback
    )
}

fun stopAdvertising() {
    bluetoothLeAdvertiser.stopAdvertising(
        advertisingCallback
    )
}
}*/