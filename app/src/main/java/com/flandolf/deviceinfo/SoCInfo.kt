package com.flandolf.deviceinfo

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import android.os.Build
import java.util.Locale

data class CPUCore(
    val processor: String, val properties: List<Pair<String, String>>
)

@Composable
fun SoCInfoTab() {
    val cpuCores = remember { gatherCPUInfo() }
    val generalInfo = remember { gatherCPUGeneralInfo() }

    if (cpuCores.isEmpty() && generalInfo.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CPU information unavailable",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General CPU info card at the top
            item {
                CPUGeneralCard(generalInfo = generalInfo)
            }

            // Individual core cards
            items(cpuCores) { core ->
                CPUCoreCard(core = core)
            }
        }
    }
}

@Composable
fun CPUGeneralCard(generalInfo: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // General info header
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // General properties
            generalInfo.forEach { (label, value) ->
                PropertyRow(label = label, value = value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CPUCoreCard(core: CPUCore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Core header
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Core ${core.processor}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))

                // CPU Architecture badge
                core.properties.find { it.first == "Architecture" }?.let { arch ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = arch.second,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CPU properties
            core.properties.filter { it.first != "Architecture" }.forEach { (label, value) ->
                PropertyRow(label = label, value = value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun gatherCPUMaxSpeed(cpuNumber: Number): String {
    return try {
        val speedPath = "/sys/devices/system/cpu/cpu$cpuNumber/cpufreq/cpuinfo_max_freq"
        val speedKHz = File(speedPath).readText().trim().toInt()
        val speedMHz = speedKHz / 1_000.0
        String.format(
            Locale.getDefault(), "%.2f MHz", speedMHz
        )
    } catch (_: Exception) {
        "Unknown"
    }

}

@SuppressLint("DefaultLocale")
fun gatherCPUMinSpeed(cpuNumber: Number): String {
    return try {
        val speedPath = "/sys/devices/system/cpu/cpu$cpuNumber/cpufreq/cpuinfo_min_freq"
        val speedKHz = File(speedPath).readText().trim().toInt()
        val speedMHz = speedKHz / 1_000.0
        String.format(Locale.getDefault(), "%.2f MHz", speedMHz)
    } catch (_: Exception) {
        "Unknown"
    }

}

fun gatherCPUInfo(): List<CPUCore> {
    val cpuInfo = File("/proc/cpuinfo").bufferedReader().readLines().joinToString("\n")

    val cpuCores = mutableListOf<CPUCore>()
    val coreBlocks = cpuInfo.split("\n\n")

    for (block in coreBlocks) {
        if (block.isBlank()) continue

        var processorNumber: String? = null
        val properties = mutableListOf<Pair<String, String>>()

        block.lines().forEach { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()

                if (key.equals("processor", ignoreCase = true)) {
                    processorNumber = value
                } else {
                    // Format keys for better display in the UI
                    val formattedKey = key.replace("CPU ", "", ignoreCase = true)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    properties.add(formattedKey to value)
                }
            }
        }

        processorNumber?.let { procNum ->
            val maxSpeed = gatherCPUMaxSpeed(procNum.toIntOrNull() ?: 0)
            val minSpeed = gatherCPUMinSpeed(procNum.toIntOrNull() ?: 0)
            properties.add(0, "Max Speed" to maxSpeed)
            properties.add(0, "Min Speed" to minSpeed)

            cpuCores.add(CPUCore(processor = procNum, properties = properties))
        }
    }
    return cpuCores
}

fun gatherCPUGeneralInfo(): List<Pair<String, String>> {
    return listOf(
        "Hardware" to (Build.HARDWARE ?: "Unknown"),
        "SOC Model" to Build.SOC_MODEL,
        "SOC Manufacturer" to Build.SOC_MANUFACTURER,
        "Board" to (Build.BOARD ?: "Unknown"),
        "Brand" to (Build.BRAND ?: "Unknown"),
        "Device" to (Build.DEVICE ?: "Unknown"),
        "Product" to (Build.PRODUCT ?: "Unknown"),
        "Architecture" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"),
        "Supported ABIs" to Build.SUPPORTED_ABIS.joinToString(", "),
        "CPU Cores" to Runtime.getRuntime().availableProcessors().toString()
    )
}

