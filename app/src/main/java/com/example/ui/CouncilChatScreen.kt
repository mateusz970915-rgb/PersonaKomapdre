package com.example.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CouncilMessage
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouncilChatScreen(
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMode by remember { mutableStateOf("Fast") }
    
    var showModelDropdown by remember { mutableStateOf(false) }
    val geminiModelsList = remember {
        listOf(
            Pair("gemini-3.5-flash", "Google Gemini 3.5 Flash (Szybki i zoptymalizowany)"),
            Pair("gemini-3.1-pro-preview", "Google Gemini 3.1 Pro Preview (Zaawansowane wnioskowanie)"),
            Pair("gemini-3.1-flash-lite-preview", "Google Gemini 3.1 Flash Lite (Lekki i szybki)"),
            Pair("gemini-2.5-flash-image", "Google Gemini 2.5 Flash Image (Multimodalny / Grafika)"),
            Pair("gemini-3.1-flash-image-preview", "Google Gemini 3.1 Flash Image Preview (Wysoka jakość multimodalna)")
        )
    }
    
    val context = LocalContext.current
    val agentPrefs by chatViewModel.agentPreferencesState.collectAsState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Council Assembly")
                        val activeEngineLabel = if (agentPrefs.aiProvider == "openrouter") {
                            "OpenRouter (${agentPrefs.openRouterSelectedModel.substringAfterLast("/")})"
                        } else {
                            val matched = geminiModelsList.find { it.first == agentPrefs.geminiSelectedModel }
                            "Gemini (${matched?.first ?: agentPrefs.geminiSelectedModel})"
                        }
                        Text(
                            text = "Engine: $activeEngineLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { chatViewModel.clearChat() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive Provider & Model Switcher Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wybrany dostawca API:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = agentPrefs.aiProvider == "gemini",
                                    onClick = { chatViewModel.updateAiProvider("gemini") },
                                    label = { Text("Google Gemini") },
                                    modifier = Modifier.testTag("council_provider_gemini_chip")
                                )
                                FilterChip(
                                    selected = agentPrefs.aiProvider == "openrouter",
                                    onClick = { chatViewModel.updateAiProvider("openrouter") },
                                    label = { Text("OpenRouter") },
                                    modifier = Modifier.testTag("council_provider_openrouter_chip")
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Model Selection Dropdown
                    val currentModelName = if (agentPrefs.aiProvider == "openrouter") {
                        agentPrefs.openRouterSelectedModel.substringAfterLast("/")
                    } else {
                        val matched = geminiModelsList.find { it.first == agentPrefs.geminiSelectedModel }
                        matched?.second ?: agentPrefs.geminiSelectedModel
                    }
                    
                    Column {
                        Text(
                            text = "Aktywny model:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            OutlinedButton(
                                onClick = { showModelDropdown = true },
                                modifier = Modifier.fillMaxWidth().testTag("council_select_model_dropdown_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentModelName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (showModelDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown"
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showModelDropdown,
                                onDismissRequest = { showModelDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                if (agentPrefs.aiProvider == "openrouter") {
                                    val openRouterModels by chatViewModel.openRouterFreeModels.collectAsState()
                                    if (openRouterModels.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Brak wolnych modeli (Kliknij by pobrać)") },
                                            onClick = {
                                                chatViewModel.fetchOpenRouterFreeModels()
                                                showModelDropdown = false
                                            }
                                        )
                                    } else {
                                        openRouterModels.forEach { model ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(model.name ?: model.id, fontWeight = FontWeight.Bold)
                                                        Text(model.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                                    }
                                                },
                                                onClick = {
                                                    chatViewModel.updateOpenRouterSelectedModel(model.id)
                                                    showModelDropdown = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    geminiModelsList.forEach { (modelId, modelName) ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(modelName, fontWeight = FontWeight.Bold)
                                                    Text(modelId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            },
                                            onClick = {
                                                chatViewModel.updateGeminiSelectedModel(modelId)
                                                showModelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(message)
                }
            }
            
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    val bitmap = getBitmapFromUri(selectedImageUri, context)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove Image", tint = Color.Red)
                    }
                }
            }

            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        FilterChip(
                            selected = selectedMode == "Fast",
                            onClick = { selectedMode = "Fast" },
                            label = { Text("Fast") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = selectedMode == "Search",
                            onClick = { selectedMode = "Search" },
                            label = { Text("Search") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = selectedMode == "Deep Think",
                            onClick = { selectedMode = "Deep Think" },
                            label = { Text("Deep Think") },
                            leadingIcon = {
                                if (selectedMode == "Deep Think") {
                                    Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.Image, contentDescription = "Attach Image")
                        }
                        
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            placeholder = { Text("Ask the council...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() || selectedImageUri != null) {
                                    val bitmap = getBitmapFromUri(selectedImageUri, context)
                                    chatViewModel.sendMessage(inputText, bitmap, selectedMode)
                                    inputText = ""
                                    selectedImageUri = null
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: CouncilMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = textColor
            )
        }
    }
}

private fun getBitmapFromUri(uri: Uri?, context: android.content.Context): Bitmap? {
    if (uri == null) return null
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.setTargetSampleSize(2) // scale down to save memory
            }
        }
    } catch (e: Exception) {
        null
    }
}
