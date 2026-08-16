package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast
import androidx.compose.ui.graphics.Color

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.IconToggleButton
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy

import androidx.compose.ui.unit.dp
import com.example.data.InteractionRecord
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentInteractionsScreen(
    viewModel: ColonyViewModel,
    modifier: Modifier = Modifier
) {
    val allInteractions by viewModel.allInteractions.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val jsonArray = JSONArray()
                        allInteractions.forEach { interaction ->
                            val obj = JSONObject().apply {
                                put("id", interaction.id)
                                put("agentName", interaction.agentName)
                                put("timestamp", interaction.timestamp)
                                put("snippet", interaction.snippet)
                                put("modelUsed", interaction.modelUsed)
                                put("totalTokens", interaction.totalTokens)
                                put("latencyMs", interaction.latencyMs)
                                put("tag", interaction.tag)
                            }
                            jsonArray.put(obj)
                        }
                        outputStream.write(jsonArray.toString(4).toByteArray())
                        Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    val chartEntryModel = remember(allInteractions) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        val agentInteractions = allInteractions.groupBy { it.agentName }
        if (agentInteractions.isEmpty()) {
            return@remember com.patrykandpatrick.vico.core.entry.entryModelOf(
                (0..6).map { com.patrykandpatrick.vico.core.entry.FloatEntry(it.toFloat(), 0f) }
            )
        }
        
        val seriesList = agentInteractions.map { (_, interactions) ->
            val counts = Array<Float>(7) { 0f }
            interactions.forEach { interaction ->
                val daysAgo = if (interaction.timestamp >= todayStart) {
                    0
                } else {
                    val diffMillis = todayStart - interaction.timestamp
                    (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
                }
                if (daysAgo in 0..6) {
                    counts[6 - daysAgo] = counts[6 - daysAgo] + 1f
                }
            }
            counts.mapIndexed { index, value -> com.patrykandpatrick.vico.core.entry.FloatEntry(index.toFloat(), value) }
        }
        com.patrykandpatrick.vico.core.entry.entryModelOf(*seriesList.toTypedArray())
    }

    var selectedInteraction by remember { mutableStateOf<InteractionRecord?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var filterFavorites by remember { mutableStateOf(false) }
    
    val interactions = remember(allInteractions, searchQuery, filterFavorites) {
        val filtered = if (filterFavorites) allInteractions.filter { it.isFavorite } else allInteractions
        if (searchQuery.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.agentName.contains(searchQuery, ignoreCase = true) || 
                it.snippet.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (selectedInteraction != null) {
        InteractionDetailScreen(
            interaction = selectedInteraction!!,
            onBack = { selectedInteraction = null },
            viewModel = viewModel
        )
    } else {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Interaction History") },
                actions = {
                    IconToggleButton(
                        checked = filterFavorites,
                        onCheckedChange = { filterFavorites = it }
                    ) {
                        Icon(
                            imageVector = if (filterFavorites) Icons.Default.Star else Icons.Default.FilterList,
                            contentDescription = "Filter Favorites",
                            tint = if (filterFavorites) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { 
                        exportLauncher.launch("interactions_export.json")
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export History")
                    }
                    IconButton(onClick = { 
                        scope.launch { viewModel.interactionLogger.clearInteractions() }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by agent or content...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            
            // Interaction Frequency Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Interaction Frequency (Last 7 Days)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Chart(
                        chart = com.patrykandpatrick.vico.compose.chart.line.lineChart(),
                        model = chartEntryModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }

            if (interactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (allInteractions.isEmpty()) "No recent interactions" else "No matching interactions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(interactions) { interaction ->
                        InteractionCard(
                            interaction = interaction,
                            onClick = { selectedInteraction = interaction },
                            onFavoriteToggle = { isFav -> scope.launch { viewModel.interactionLogger.toggleInteractionFavorite(interaction.id, isFav) } }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun InteractionCard(
    interaction: InteractionRecord,
    onClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(interaction.timestamp))
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconToggleButton(
                        checked = interaction.isFavorite,
                        onCheckedChange = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (interaction.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (interaction.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = interaction.agentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (interaction.tag.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val tagColor = when (interaction.tag.lowercase()) {
                            "work" -> Color(0xFF1976D2)
                            "personal" -> Color(0xFF388E3C)
                            "ideas" -> Color(0xFFF57C00)
                            else -> MaterialTheme.colorScheme.secondary
                        }
                        Surface(
                            color = tagColor.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = interaction.tag,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = tagColor
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(interaction.snippet))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy snippet",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = interaction.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionDetailScreen(
    interaction: InteractionRecord,
    onBack: () -> Unit,
    viewModel: ColonyViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var showTagDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    val dateString = dateFormat.format(Date(interaction.timestamp))
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = interaction.agentName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showTagDialog = true }) {
                    Icon(Icons.Default.Label, contentDescription = "Assign Tag")
                }
            }
            if (interaction.tag.isNotEmpty()) {
                val tagColor = when (interaction.tag.lowercase()) {
                    "work" -> Color(0xFF1976D2)
                    "personal" -> Color(0xFF388E3C)
                    "ideas" -> Color(0xFFF57C00)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Surface(
                    color = tagColor.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = interaction.tag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = tagColor
                    )
                }
            }


        if (showTagDialog) {
            val predefinedTags = listOf("Work", "Personal", "Ideas", "None")
            AlertDialog(
                onDismissRequest = { showTagDialog = false },
                title = { Text("Assign Tag") },
                text = {
                    Column {
                        predefinedTags.forEach { tag ->
                            TextButton(onClick = {
                                coroutineScope.launch {
                                    viewModel.interactionLogger.updateInteractionTag(interaction.id, if (tag == "None") "" else tag)
                                    onBack() // Or update the local state if needed, but going back is easier for now to refresh
                                }
                                showTagDialog = false
                            }) {
                                Text(tag)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

            HorizontalDivider()

            // Metadata Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataItem(
                    label = "Model",
                    value = interaction.modelUsed,
                    modifier = Modifier.weight(1f)
                )
                MetadataItem(
                    label = "Tokens",
                    value = "${interaction.totalTokens}",
                    modifier = Modifier.weight(1f)
                )
                MetadataItem(
                    label = "Latency",
                    value = "${interaction.latencyMs} ms",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            // Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Snippet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(interaction.snippet)) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy snippet")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = interaction.snippet,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetadataItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
