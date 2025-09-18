package com.flandolf.deviceinfo

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCameraFront
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Camera(
    val id: String,
    val properties: List<Pair<String, String>>
)

@Composable
fun CameraInfoTab() {
    val ctx = LocalContext.current
    val cameras = remember { gatherCameraInfoGrouped(ctx) }

    if (cameras.isEmpty()) {
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No cameras detected",
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
            items(cameras) { camera ->
                CameraCard(camera = camera)
            }
        }
    }
}

@Composable
fun CameraCard(camera: Camera) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Camera header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (camera.properties.any { it.first == "Direction" && it.second == "Front" })
                        Icons.Default.PhotoCameraFront else Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Camera ${camera.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))

                // Direction badge
                camera.properties.find { it.first == "Direction" }?.let { direction ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (direction.second) {
                            "Front" -> MaterialTheme.colorScheme.secondaryContainer
                            "Back" -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = direction.second,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = when (direction.second) {
                                "Front" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "Back" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera properties
            camera.properties.filter { it.first != "Direction" }.forEach { (label, value) ->
                PropertyRow(label = label, value = value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun gatherCameraInfoGrouped(ctx: Context): List<Camera> {
    return try {
        val cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        val cameras = mutableListOf<Camera>()

        for (cameraId in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val properties = mutableListOf<Pair<String, String>>()

            // Lens facing
            val lensFacing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
            val lensFacingStr = when (lensFacing) {
                android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> "Back"
                android.hardware.camera2.CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                else -> "Unknown"
            }
            properties.add("Direction" to lensFacingStr)

            // Supported hardware level
            val hardwareLevel = characteristics.get(android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            val hardwareLevelStr = when (hardwareLevel) {
                android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
                else -> "Unknown"
            }
            properties.add("Hardware Level" to hardwareLevelStr)

            // Flash info
            val flashAvailable = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
            properties.add("Flash" to if (flashAvailable == true) "Available" else "Not available")

            // Sensor orientation
            val sensorOrientation = characteristics.get(android.hardware.camera2.CameraCharacteristics.SENSOR_ORIENTATION)
            properties.add("Sensor Orientation" to "${sensorOrientation ?: "N/A"}°")

            // Focal lengths
            val focalLengths = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            if (focalLengths != null && focalLengths.isNotEmpty()) {
                properties.add("Focal Length" to "${focalLengths.first()}mm")
            }

            // Auto-focus
            val afModes = characteristics.get(android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            val hasAutofocus = afModes?.any { it != android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_OFF } ?: false
            properties.add("Autofocus" to if (hasAutofocus) "Yes" else "No")

            // Digital zoom
            val maxZoom = characteristics.get(android.hardware.camera2.CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
            properties.add("Max Zoom" to "${maxZoom ?: "1.0"}x")

            // Optical stabilization
            val stabilizationModes = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            val hasOIS = stabilizationModes?.isNotEmpty() ?: false
            properties.add("OIS" to if (hasOIS) "Yes" else "No")

            cameras.add(Camera(cameraId, properties))
        }

        cameras
    } catch (e: Exception) {
        emptyList()
    }
}
