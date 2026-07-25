package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.ColonyViewModel
import com.example.data.AgentDecision

// Note: This is an enhanced screen representing the "Rada agentów" 
// (Council of Agents) with visualizations of votes and dissenting opinions.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouncilScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val decisions by viewModel.decisions.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val negotiationText = when {
        messages.isNotEmpty() -> {
            val latest = messages.last()
            val sender = if (latest.role == "user") "You" else "Council"
            "Latest message in discussion by $sender: \"${latest.content.take(120)}${if (latest.content.length > 120) "..." else ""}\""
        }
        decisions.isNotEmpty() -> {
            val latest = decisions.first()
            "Latest resolution proposed by ${latest.agentName}: ${latest.actionDescription}"
        }
        else -> {
            "No active negotiations. The colony's agents are working in harmony and awaiting new instructions."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rada Agentów") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Header showing ongoing negotiations summary
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ongoing Negotiations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(negotiationText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateToChat) {
                        Text("Join Negotiation (Chat)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Votes & Dissents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(decisions) { decision ->
                    CouncilVoteCard(decision)
                }
            }
        }
    }
}

@Composable
fun CouncilVoteCard(decision: AgentDecision) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Initiated by: ${decision.agentName}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = decision.actionDescription, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Council Vote: majority approved (${decision.confidenceLevel} confidence)", style = MaterialTheme.typography.labelSmall)
            
            if (decision.dissentingOpinions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Dissenting:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(decision.dissentingOpinions, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
