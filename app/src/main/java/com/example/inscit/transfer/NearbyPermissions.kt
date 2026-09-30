package com.example.inscit.transfer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// Runtime permissions Nearby Connections needs to advertise/discover.
// These are declared in the manifest but were never requested at runtime,
// so startAdvertising()/startDiscovery() failed silently (logcat only) and
// both devices sat on endless spinners with empty lists.
object NearbyPermissions {

    // API-aware set: Android 12+ needs BT_SCAN/ADVERTISE/CONNECT at runtime;
    // Android 13+ needs NEARBY_WIFI_DEVICES; below 12, fine location covers BLE scan.
    fun required(): Array<String> = when {
        Build.VERSION.SDK_INT >= 33 -> arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
        Build.VERSION.SDK_INT >= 31 -> arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun missing(context: Context): List<String> =
        required().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

    fun hasAll(context: Context): Boolean = missing(context).isEmpty()
}
