package com.flandolf.deviceinfo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothInfoTab() {
    // Get the Android context from the local composition
    val context = LocalContext.current

    val bluetoothInfo = remember {
        getBluetoothInfo(context)
    }

    if (bluetoothInfo.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No bluetooth info available", color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            item {
                Text(
                    text = "Bluetooth",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            itemsIndexed(bluetoothInfo) { index, pair ->
                PropertyRow(label = pair.first, value = pair.second)
                if (index < bluetoothInfo.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Gathers Bluetooth information about the device and returns it as a list of key-value pairs.
 *
 * This function checks for Bluetooth capabilities, its current state, name, address,
 * and lists bonded devices. It requires the BLUETOOTH permission.
 *
 * @param context The application context, required to access system services.
 * @return A List of Pair objects, where the first element is the title (String) and the second is the value (String).
 */

fun getBluetoothInfo(context: Context): List<Pair<String, String>> {
    val bluetoothInfoList = mutableListOf<Pair<String, String>>()
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    // Check if the device has Bluetooth capability
    if (bluetoothAdapter == null) {
        bluetoothInfoList.add(Pair("Bluetooth", "Not supported on this device"))
        return bluetoothInfoList
    }

    // Bluetooth state
    val stateString = when (bluetoothAdapter.state) {
        BluetoothAdapter.STATE_ON -> "On"
        BluetoothAdapter.STATE_OFF -> "Off"
        BluetoothAdapter.STATE_TURNING_ON -> "Turning On"
        BluetoothAdapter.STATE_TURNING_OFF -> "Turning Off"
        else -> "Unknown"
    }
    bluetoothInfoList.add(Pair("State", stateString))

    // Check if Bluetooth is enabled
    val isEnabled = bluetoothAdapter.isEnabled
    bluetoothInfoList.add(Pair("Enabled", if (isEnabled) "Yes" else "No"))


    val leSupport: String = when (bluetoothAdapter.isLeAudioSupported) {
        BluetoothStatusCodes.FEATURE_SUPPORTED -> "Yes"
        BluetoothStatusCodes.FEATURE_NOT_SUPPORTED -> "No"
        else -> "Unknown"
    }
    bluetoothInfoList.add("LE Support" to leSupport)

    val leSupportBool: Boolean = when (bluetoothAdapter.isLeAudioSupported) {
        BluetoothStatusCodes.FEATURE_SUPPORTED -> true
        BluetoothStatusCodes.FEATURE_NOT_SUPPORTED -> false
        else -> false
    }

    val btVersion = when {
        leSupportBool -> "5.2 (or higher)"
        bluetoothAdapter.isLePeriodicAdvertisingSupported -> "5.1 (or higher)"
        bluetoothAdapter.isLeExtendedAdvertisingSupported -> "5.0 (or higher)"
        bluetoothAdapter.isMultipleAdvertisementSupported -> "4.1 or 4.2"
        else -> "4.0 or lower"
    }
    bluetoothInfoList.add("Bluetooth Version" to btVersion)

    val longRange = when (bluetoothAdapter.isLeCodedPhySupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("Long Range" to longRange)

    val advertisingSupport: String = when (bluetoothAdapter.isLeExtendedAdvertisingSupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("LE Extended Advertising" to advertisingSupport)

    val periodicAdvertisingSupport: String = when (bluetoothAdapter.isLePeriodicAdvertisingSupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("LE Periodic Advertising" to periodicAdvertisingSupport)

    val multipleAdvertisementSupport: String = when (bluetoothAdapter.isMultipleAdvertisementSupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("Multiple Advertisement" to multipleAdvertisementSupport)

    val offloadedFilteringSupport: String = when (bluetoothAdapter.isOffloadedFilteringSupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("Offloaded Filtering" to offloadedFilteringSupport)

    val offloadedScanBatchingSupport: String = when (bluetoothAdapter.isOffloadedScanBatchingSupported) {
        true -> "Yes"
        false -> "No"
    }
    bluetoothInfoList.add("Offloaded Scan Batching" to offloadedScanBatchingSupport)

    return bluetoothInfoList
}
