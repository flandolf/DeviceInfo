package com.flandolf.deviceinfo

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
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
import java.util.Locale
import kotlin.math.sqrt
import android.view.Display.HdrCapabilities.*

@Composable
fun ScreenInfoTab() {

    val ctx = LocalContext.current
    val screenInfo = remember { gatherScreenInfo(ctx) }

    if (screenInfo.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
                    text = "No screen info available", color = MaterialTheme.colorScheme.outline
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
                    text = "Screen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            itemsIndexed(screenInfo) { index, pair ->
                PropertyRow(label = pair.first, value = pair.second)
                if (index < screenInfo.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

fun densityQualifier(dpi: Int): String = when {
    dpi < 140 -> "ldpi"      // ~120 dpi
    dpi < 200 -> "mdpi"      // ~160 dpi (baseline)
    dpi < 280 -> "hdpi"      // ~240 dpi
    dpi < 400 -> "xhdpi"     // ~320 dpi
    dpi < 560 -> "xxhdpi"    // ~480 dpi
    dpi < 640 -> "xxxhdpi"   // ~640 dpi
    else -> "nodpi"          // density-independent
}

fun gatherScreenInfo(ctx: Context): List<Pair<String, String>> {
    val info = mutableListOf<Pair<String, String>>()

    val metrics: DisplayMetrics = ctx.resources.displayMetrics
    val density = metrics.densityDpi
    val width = metrics.widthPixels
    val height = metrics.heightPixels
    val refreshRate = ctx.display.refreshRate
    val widthInches = width / metrics.xdpi
    val heightInches = height / metrics.ydpi
    val widthCm = widthInches * 2.54f
    val heightCm = heightInches * 2.54f
    val physicalSize = sqrt(widthInches * widthInches + heightInches * heightInches)

    val orientation =
        if (width > height) "Landscape" else if (height > width) "Portrait" else "Square"

    val displayModes = ctx.display.supportedModes
    val ppi = sqrt((width * width + height * height).toDouble()) / physicalSize

    // gcd for aspect ratio
    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
    val divisor = gcd(width, height)
    val aspectRatioReadable = "${width / divisor}:${height / divisor}"

    // HDR and Dolby Vision support
    val hdrSupport = run {
        val hdrCapabilities = ctx.display.mode.supportedHdrTypes
        val types = hdrCapabilities ?: IntArray(0)

        if (types.isEmpty()) {
            "Not supported"
        } else {
            val supportedTypes = mutableListOf<String>()
            if (types.contains(HDR_TYPE_DOLBY_VISION)) supportedTypes.add("Dolby Vision")
            if (types.contains(HDR_TYPE_HDR10)) supportedTypes.add("HDR10")
            if (types.contains(HDR_TYPE_HLG)) supportedTypes.add("HLG")
            if (types.contains(HDR_TYPE_HDR10_PLUS)) supportedTypes.add("HDR10+")

            supportedTypes.joinToString(", ")
        }
    }

    val dolbyVisionSupport = run {
        val hdrCapabilities = ctx.display.mode.supportedHdrTypes
        val types = hdrCapabilities ?: IntArray(0)
        if (types.contains(HDR_TYPE_DOLBY_VISION)) "Supported" else "Not supported"
    }



    // Add info
    info.add("Resolution" to "$width x $height pixels")
    info.add("Density" to "$density dpi (${densityQualifier(density)})")
    info.add("PPI" to String.format(Locale.getDefault(), "%.1f ppi", ppi))
    info.add("Size" to String.format(Locale.getDefault(), "%.1f inches", physicalSize))
    info.add(
        "Height × Width" to String.format(
            Locale.getDefault(), "%.1f × %.1f cm", heightCm, widthCm
        )
    )
    info.add("Refresh Rate" to String.format(Locale.getDefault(), "%.2f Hz", refreshRate))
    info.add("Orientation" to orientation)
    info.add("Aspect Ratio" to aspectRatioReadable)

    info.add("Available Display Modes" to displayModes.size.toString())
    displayModes.forEachIndexed { index, mode ->
        val modeInfo = String.format(
            Locale.getDefault(),
            "%dx%d @ %.2f Hz",
            mode.physicalWidth,
            mode.physicalHeight,
            mode.refreshRate
        )
        info.add("- Mode ${index + 1}" to modeInfo)
    }

    info.add("Dolby Vision Support" to dolbyVisionSupport)
    info.add("HDR" to hdrSupport)


    return info
}
