package com.example.mobiilisovellusprojekti.Permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val PERMISSION_REQUEST_CODE = 1
/*
private val bluetoothPermissions = arrayOf(
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT
)
*/
private val bluetoothPermissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}


fun hasBluetoothPermissions(context: Context): Boolean {
    return bluetoothPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun requestPermissions(activity: Activity) {
    ActivityCompat.requestPermissions(activity, bluetoothPermissions, 1)
}