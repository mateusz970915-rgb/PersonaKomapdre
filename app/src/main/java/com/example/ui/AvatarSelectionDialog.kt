package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Agent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AvatarSelectionDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    val presetAvatars = listOf(
        "https://robohash.org/${agent.name}_1?set=set1",
        "https://robohash.org/${agent.name}_2?set=set2",
        "https://robohash.org/${agent.name}_3?set=set3",
        "https://robohash.org/${agent.name}_4?set=set4",
        "https://api.dicebear.com/7.x/bottts/png?seed=${agent.name}",
        "https://api.dicebear.com/7.x/adventurer/png?seed=${agent.name}",
        "https://api.dicebear.com/7.x/micah/png?seed=${agent.name}",
        "https://api.dicebear.com/7.x/notionists/png?seed=${agent.name}"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agent Avatar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generate or select an avatar for ${agent.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isGenerating = true
                            delay(1500) // Simulate Imagen API call delay
                            val randomSeed = System.currentTimeMillis()
                            val setNum = (System.currentTimeMillis() % 4 + 1).toInt()
                            val generatedUrl = "https://api.dicebear.com/7.x/bottts/png?seed=${agent.name}_${agent.role}_$randomSeed"
                            onAvatarSelected(generatedUrl)
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generowanie z Imagen 3...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wygeneruj Ikonę (Imagen)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(presetAvatars) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Avatar Option",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onAvatarSelected(url) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onAvatarSelected("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Avatar")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
