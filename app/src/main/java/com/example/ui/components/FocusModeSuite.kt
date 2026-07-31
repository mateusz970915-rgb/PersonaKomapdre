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
fun FocusModeSuite(
    viewModel: ColonyViewModel,
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var secondsPassed by remember { mutableStateOf(0) }
    var isBreathingIn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsPassed++
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            isBreathingIn = !isBreathingIn
        }
    }

    val breathingScale by animateFloatAsState(
        targetValue = if (isBreathingIn) 1.5f else 0.8f,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        label = "breathing_scale"
    )

    val breathingText = if (isBreathingIn) "Wdech..." else "Wydech..."

    val formattedTime = remember(secondsPassed) {
        val mins = secondsPassed / 60
        val secs = secondsPassed % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .padding(padding)
            .testTag("focus_mode_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Głębokie Skupienie",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Wszystkie powiadomienia i rozpraszacze zostały wyciszone. Poświęć ten czas na głęboką, nieprzerwaną pracę.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(scaleX = breathingScale, scaleY = breathingScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(scaleX = breathingScale * 0.8f, scaleY = breathingScale * 0.8f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = breathingText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.updateFocusModeActive(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("exit_focus_mode_btn")
            ) {
                Text(
                    text = "Zakończ sesję skupienia",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
