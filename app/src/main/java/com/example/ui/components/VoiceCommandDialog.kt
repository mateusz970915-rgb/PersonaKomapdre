package com.example.ui.components
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.example.data.*
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoiceCommandDialog(
    viewModel: ColonyViewModel,
    onDismiss: () -> Unit,
    onNavigateToTaskBoard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAgentBuilder: () -> Unit,
    onNavigateToSmartFinance: () -> Unit,
    onNavigateToSleepOptimizer: () -> Unit
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsState()
    val voiceText by viewModel.voiceText.collectAsState()
    val voiceError by viewModel.voiceError.collectAsState()
    val lastVoiceAction by viewModel.lastVoiceAction.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceRecognition(context)
        } else {
            android.widget.Toast.makeText(context, "Speech recognition requires audio permission.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.startVoiceRecognition(context)
        } else {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(lastVoiceAction) {
        lastVoiceAction?.let { action ->
            when (action) {
                "NAVIGATE_TASKS" -> {
                    onNavigateToTaskBoard()
                    viewModel.clearVoiceState()
                    onDismiss()
                }
                "NAVIGATE_SETTINGS" -> {
                    onNavigateToSettings()
                    viewModel.clearVoiceState()
                    onDismiss()
                }
                "NAVIGATE_BUILDER" -> {
                    onNavigateToAgentBuilder()
                    viewModel.clearVoiceState()
                    onDismiss()
                }
                "NAVIGATE_FINANCE" -> {
                    onNavigateToSmartFinance()
                    viewModel.clearVoiceState()
                    onDismiss()
                }
                "NAVIGATE_SLEEP" -> {
                    onNavigateToSleepOptimizer()
                    viewModel.clearVoiceState()
                    onDismiss()
                }
                "TOGGLE_FOCUS" -> {
                    viewModel.clearVoiceState()
                    onDismiss()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.stopVoiceRecognition()
            viewModel.clearVoiceState()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text("Asystent Głosowy")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Wypowiedz komendę głosową np. 'Zadania', 'Ustawienia', 'Nowy agent', 'Skupienie', 'Finanse' lub 'Sen'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isListening) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = if (voiceText.isBlank()) "Słucham..." else voiceText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                voiceError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (lastVoiceAction == "UNKNOWN_COMMAND") {
                    Text(
                        text = "Nie rozpoznano komendy. Spróbuj ponownie.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isListening) {
                        viewModel.stopVoiceRecognition()
                    } else {
                        viewModel.startVoiceRecognition(context)
                    }
                },
                modifier = Modifier.testTag("voice_dialog_mic_toggle")
            ) {
                Text(if (isListening) "Zatrzymaj" else "Mów ponownie")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.stopVoiceRecognition()
                    viewModel.clearVoiceState()
                    onDismiss()
                },
                modifier = Modifier.testTag("voice_dialog_cancel")
            ) {
                Text("Anuluj")
            }
        }
    )
}
