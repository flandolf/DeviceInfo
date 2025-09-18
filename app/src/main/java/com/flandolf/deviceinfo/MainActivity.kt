package com.flandolf.deviceinfo

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flandolf.deviceinfo.ui.theme.DeviceInfoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeviceInfoTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Device", "SoC", "Camera")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = "Device Info") },
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DeviceInfoTab()
                1 -> SoCInfoTab()
                2 -> CameraInfoTab()
            }
        }
    }
}

@Composable
fun DeviceInfoTab() {
    val ctx = LocalContext.current
    val deviceInfo = remember { gatherDeviceInfo(ctx) }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        itemsIndexed(deviceInfo) { index, pair ->
            InfoRow(label = pair.first, value = pair.second)
            if (index < deviceInfo.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(140.dp)
            )
            Text(text = value)
        }
    }
}

fun gatherDeviceInfo(ctx: Context): List<Pair<String, String>> {
    val metrics = ctx.resources.displayMetrics
    val density = metrics.densityDpi
    val width = metrics.widthPixels
    val height = metrics.heightPixels

    val abi = Build.SUPPORTED_ABIS.joinToString(", ")

    return listOf(
        "Manufacturer" to (Build.MANUFACTURER ?: "unknown"),
        "Model" to (Build.MODEL ?: "unknown"),
        "Brand" to (Build.BRAND ?: "unknown"),
        "Device" to (Build.DEVICE ?: "unknown"),
        "Product" to (Build.PRODUCT ?: "unknown"),
        "Android SDK" to "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE ?: "?"})",
        "ABIs" to abi,
        "Screen" to "${width}x${height} @ ${density}dpi",
        "Fingerprint" to (Build.FINGERPRINT ?: "unknown"),
        "Radio" to (Build.getRadioVersion() ?: "unknown"),
        "Kernel" to (System.getProperty("os.version") ?: "unknown"),
        "Bootloader" to (Build.BOOTLOADER ?: "unknown"),
    )
}
