package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MissionBatchWidget(modifier: Modifier = Modifier) {
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mission Batch (Grupowe Wykonanie)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Symultaniczne uruchamianie i monitorowanie powiązanych zadań",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // List of grouped tasks
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BatchTaskItem("Analiza danych logów", progress)
                BatchTaskItem("Synteza wyników bezpieczeństwa", progress * 0.8f)
                BatchTaskItem("Generowanie raportu końcowego", progress * 0.5f)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isRunning) {
                    Button(
                        onClick = { isRunning = false; progress = 0f },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zatrzymaj")
                    }
                } else {
                    Button(
                        onClick = {
                            isRunning = true
                            progress = 0f
                            coroutineScope.launch {
                                while (progress < 1f && isRunning) {
                                    delay(100)
                                    progress += 0.02f
                                }
                                if (progress >= 1f) isRunning = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uruchom Batch")
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchTaskItem(name: String, itemProgress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        val statusText = when {
            itemProgress >= 1f -> "Zakończone"
            itemProgress > 0f -> "W toku (${(itemProgress * 100).toInt()}%)"
            else -> "Oczekuje"
        }
        Text(
            text = statusText,
            fontSize = 12.sp,
            color = if (itemProgress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
    }
}
