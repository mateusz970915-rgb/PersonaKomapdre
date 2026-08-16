package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.InputStream
import android.util.Base64

@Composable
fun MultimodalPromptSection(
    attachedImageBase64: String?,
    attachedAudioBase64: String?,
    onImageAttached: (base64: String?, mimeType: String) -> Unit,
    onAudioAttached: (base64: String?, mimeType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    onImageAttached(base64, mimeType)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val mimeType = context.contentResolver.getType(uri) ?: "audio/mp3"
                    onAudioAttached(base64, mimeType)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Załącz Obraz",
                    tint = if (attachedImageBase64 != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { audioPickerLauncher.launch("audio/*") }
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Załącz Nagranie Głosowe",
                    tint = if (attachedAudioBase64 != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Attachments Preview Badge Bar
        AnimatedVisibility(visible = attachedImageBase64 != null || attachedAudioBase64 != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (attachedImageBase64 != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Obraz załączony") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onImageAttached(null, "image/jpeg") },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Usuń obraz")
                            }
                        }
                    )
                }

                if (attachedAudioBase64 != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Nagranie załączone") },
                        leadingIcon = { Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onAudioAttached(null, "audio/mp3") },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Usuń nagranie")
                            }
                        }
                    )
                }
            }
        }
    }
}
