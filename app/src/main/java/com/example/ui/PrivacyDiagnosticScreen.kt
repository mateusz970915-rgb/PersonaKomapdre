package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DataAccessRequest
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDiagnosticScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val dataAccessRequests by viewModel.dataAccessRequests.collectAsState()
    var filterViolationsOnly by remember { mutableStateOf(false) }

    val filteredRequests = if (filterViolationsOnly) {
        dataAccessRequests.filter { it.isPolicyViolation }
    } else {
        dataAccessRequests
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("privacy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.runDiagnosticAudit()
                        },
                        modifier = Modifier.testTag("privacy_sweep_button")
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = "Run Audit")
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
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audyt Prywatności i Dostępów",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Skanowanie Lokalnie: Odczyt listy zainstalowanych aplikacji z systemowego OS PackageManager.\n• Audyt Chmurowy (Gemini AI): Uwaga — podczas uruchomienia audytu (przycisk tarczy) lista Twoich zainstalowanych aplikacji oraz ich uprawnień zostanie wysłana do chmury (Gemini) w celu analizy zagrożeń bezpieczeństwa przez model AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !filterViolationsOnly,
                    onClick = { filterViolationsOnly = false },
                    label = { Text("All Logs (${dataAccessRequests.size})") },
                    modifier = Modifier.testTag("privacy_filter_all")
                )
                FilterChip(
                    selected = filterViolationsOnly,
                    onClick = { filterViolationsOnly = true },
                    label = { Text("Violations Only (${dataAccessRequests.count { it.isPolicyViolation }})") },
                    modifier = Modifier.testTag("privacy_filter_violations")
                )
            }

            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (filterViolationsOnly) "No policy violations recorded." else "No data access requests logged.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredRequests) { request ->
                        DataAccessCard(request)
                    }
                }
            }
        }
    }
}

@Composable
fun DataAccessCard(request: DataAccessRequest) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = dateFormat.format(Date(request.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("data_access_card_${request.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (request.isPolicyViolation) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.agentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (request.isPolicyViolation) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (request.isPolicyViolation) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Accessed: ${request.dataType}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (request.isPolicyViolation) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (request.isPolicyViolation) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Violation",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.violationReason ?: "Unknown policy violation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
