package com.flandolf.deviceinfo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
    val scope = rememberCoroutineScope()
    val tabs = listOf("Device", "SOC", "Memory", "Screen", "Battery", "Bluetooth", "Camera")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Device Info") })
                androidx.compose.material3.ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
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
                2 -> MemoryInfoTab()
                3 -> ScreenInfoTab()
                4 -> BatteryInfoTab()
                5 -> BluetoothInfoTab()
                6 -> CameraInfoTab()
            }
        }
    }
}

@Composable
fun AppInfoDialog(onDismissRequest: () -> Unit, context: Context) {
    val appName = stringResource(id = R.string.app_name)
    val appVersion = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (_: PackageManager.NameNotFoundException) {
        "N/A"
    }
    val uriHandler = LocalUriHandler.current // Get the UriHandler
    val githubUrl = "https://github.com/flandolf/DeviceInfo"

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = appName) },
        text = { Text(text = "Version: $appVersion") },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("OK")
            }
        },
        dismissButton = { // Use dismissButton for secondary actions
            TextButton(onClick = {
                uriHandler.openUri(githubUrl) // Open the URL
                onDismissRequest() // Optionally dismiss the dialog after clicking
            }) {
                Text("View Source")
            }
        }
    )
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
            PropertyRow(label = pair.first, value = pair.second) // Assuming you have this Composable
            if (index < deviceInfo.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
        "Kernel" to (System.getProperty("os.version") ?: "unknown"),
        "Android SDK" to "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE ?: "?"})",
        "ABIs" to abi,
        "Screen" to "${width}x${height} @ ${density}dpi",
        "Fingerprint" to (Build.FINGERPRINT ?: "unknown"),
        "Radio" to (Build.getRadioVersion() ?: "unknown"),
        "Bootloader" to (Build.BOOTLOADER ?: "unknown"),
    )
}
