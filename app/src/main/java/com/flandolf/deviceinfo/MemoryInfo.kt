package com.flandolf.deviceinfo

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun MemoryInfoTab() {
    val memoryInfo = remember { gatherMemoryInfo() }
    val storageInfo = remember { getStorageInfo() }

    if (memoryInfo.isEmpty() && storageInfo.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No memory or storage info available",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = "Memory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            itemsIndexed(memoryInfo) { index, pair ->
                PropertyRow(label = pair.first, value = pair.second)
                if (index < memoryInfo.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            if (storageInfo.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Storage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                itemsIndexed(storageInfo) { index, pair ->
                    PropertyRow(label = pair.first, value = pair.second)
                    if (index < storageInfo.lastIndex) {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


fun gatherMemoryInfo(): List<Pair<String, String>> {
    val labelMap = mapOf(
        "MemTotal" to "Total Memory",
        "MemFree" to "Free Memory",
        "MemAvailable" to "Available Memory",
        "Buffers" to "Buffers",
        "Cached" to "Cached"
    )
    val keys = labelMap.keys
    val memInfo = mutableListOf<Pair<String, String>>()
    try {
        java.io.File("/proc/meminfo").forEachLine { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                val label = parts[0].trim()
                if (label in keys) {
                    val value = parts[1].trim()
                    val valueInMb = if (value.endsWith("kB")) {
                        val kbValue = value.removeSuffix("kB").trim().toLongOrNull()
                        if (kbValue != null) "${kbValue / 1024} MB" else value
                    } else {
                        value
                    }
                    val readableLabel = labelMap[label] ?: label
                    memInfo.add(readableLabel to valueInMb)
                }
            }
        }
    } catch (_: Exception) {
    }
    return memInfo
}

fun getStorageInfo(): List<Pair<String, String>> {
    val stat = StatFs(Environment.getDataDirectory().absolutePath)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    val availableBlocks = stat.availableBlocksLong

    val totalBytes = totalBlocks * blockSize
    val availableBytes = availableBlocks * blockSize

    fun formatSize(bytes: Long): String {
        val gb = bytes / (1024 * 1024 * 1024.0)
        return String.format(Locale.getDefault(), "%.2f GB", gb)
    }

    return listOf(
        "Total Storage" to formatSize(totalBytes),
        "Available Storage" to formatSize(availableBytes)
    )
}