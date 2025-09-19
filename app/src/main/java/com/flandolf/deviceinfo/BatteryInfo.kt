package com.flandolf.deviceinfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
fun BatteryInfoTab() {
    // Get the Android context from the local composition
    val context = LocalContext.current

    val batteryInfo = remember {
        getBatteryInfo(context)
    }

    if (batteryInfo.isEmpty()) {
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
                    text = "No battery info available", color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            item {
                Text(
                    text = "Battery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            itemsIndexed(batteryInfo) { index, pair ->
                PropertyRow(label = pair.first, value = pair.second)
                if (index < batteryInfo.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Gathers detailed battery information about the device and returns it as a list of key-value pairs.
 *
 * This function uses the ACTION_BATTERY_CHANGED sticky intent to get a snapshot of the
 * current battery state. It also uses the BatteryManager service to get real-time
 * current and charging status.
 *
 * @param context The application context, required to access system services and broadcast intents.
 * @return A List of Pair objects, where the first element is the title (String) and the second is the value (String).
 */
fun getBatteryInfo(context: Context): List<Pair<String, String>> {
    val batteryInfoList = mutableListOf<Pair<String, String>>()

    // Get the sticky intent for battery status
    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
        context.registerReceiver(null, intentFilter)
    }

    // Get the BatteryManager service
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    // Check if the intent is not null before proceeding
    if (batteryStatus != null) {
        // Current charge level and scale
        val level: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level != -1 && scale != -1) {
            val batteryPct = (level / scale.toFloat()) * 100
            batteryInfoList.add(Pair("Charge Level", "%.1f%%".format(batteryPct)))
        }

        // Battery health
        val health: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            else -> "Unknown"
        }
        batteryInfoList.add(Pair("Health", healthString))

        // Charging status
        val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val statusString = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        batteryInfoList.add(Pair("Status", statusString))

        // Plugged status
        val plugged: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedString = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Plugged In"
        }
        batteryInfoList.add(Pair("Plugged Into", pluggedString))

        // Battery technology
        val technology: String? = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        if (technology != null) {
            batteryInfoList.add(Pair("Technology", technology))
        }

        // Temperature (in tenths of a degree Celsius)
        val temperature: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        if (temperature != -1) {
            val tempInCelsius = temperature / 10.0f
            batteryInfoList.add(Pair("Temperature", "$tempInCelsius °C"))
        }

        // Voltage (in millivolts)
        val voltage: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        if (voltage != -1) {
            batteryInfoList.add(Pair("Voltage", "$voltage mV"))
        }
        // Battery capacity in mAh (if available)
        batteryInfoList.add("Capacity" to "${getBatteryCapacity(context)} mAh")


    }

    return batteryInfoList
}

@SuppressLint("PrivateApi")
fun getBatteryCapacity(context: Context?): Double {
    val mPowerProfile: Any?
    var batteryCapacity = 0.0
    val powerProfileClass = "com.android.internal.os.PowerProfile"

    try {
        mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(context)

        batteryCapacity =
            Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return batteryCapacity
}