package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataAccessRequest
import com.example.security.AppPermissionInfo
import com.example.security.LocalEncryptedVault
import com.example.security.PermissionScannerAgent
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
    val context = LocalContext.current
    val dataAccessRequests by viewModel.dataAccessRequests.collectAsState()
    
    // Multi-tab selection: 0 = LOGS, 1 = PERMISSIONS, 2 = VAULT
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, VIOLATIONS, PENDING

    // Permission Scanner States
    var scannedApps by remember { mutableStateOf<List<AppPermissionInfo>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    // Encrypted Vault States
    var vaultKeys by remember { mutableStateOf(LocalEncryptedVault.getAllSecretsKeys(context)) }
    var newSecretKey by remember { mutableStateOf("") }
    var newSecretValue by remember { mutableStateOf("") }
    var decryptedSecretKey by remember { mutableStateOf<String?>(null) }
    var decryptedSecretValue by remember { mutableStateOf<String?>(null) }

    val filteredRequests = when (selectedFilterTab) {
        "VIOLATIONS" -> dataAccessRequests.filter { it.isPolicyViolation && !it.requiresUserApproval }
        "PENDING" -> dataAccessRequests.filter { it.requiresUserApproval && it.approvalStatus == "Pending" }
        else -> dataAccessRequests
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prywatność i Bezpieczeństwo") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("privacy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = {
                                viewModel.runDiagnosticAudit()
                            },
                            modifier = Modifier.testTag("privacy_sweep_button")
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = "Run Audit")
                        }
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
            // Material 3 Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Logi Dostępów", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Skaner Uprawnień", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Zaszyfrowany Sejf", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: ORIGINAL PRIVACY DIAGNOSTICS LOGS
                    Column(modifier = Modifier.fillMaxSize()) {
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
                                    text = "• Skanowanie Lokalnie: Odczyt listy zainstalowanych aplikacji z systemowego OS PackageManager.\n• Dwustopniowa autoryzacja: System wymaga potwierdzenia przed realizacją operacji wysokiego ryzyka.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.insertTestApprovalRequest()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("trigger_test_approval_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wygeneruj testowe żądanie autoryzacji")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedFilterTab == "ALL",
                                onClick = { selectedFilterTab = "ALL" },
                                label = { Text("Wszystkie (${dataAccessRequests.size})") },
                                modifier = Modifier.testTag("privacy_filter_all")
                            )
                            FilterChip(
                                selected = selectedFilterTab == "PENDING",
                                onClick = { selectedFilterTab = "PENDING" },
                                label = { Text("Autoryzacje (${dataAccessRequests.count { it.requiresUserApproval && it.approvalStatus == "Pending" }})") },
                                modifier = Modifier.testTag("privacy_filter_pending")
                            )
                            FilterChip(
                                selected = selectedFilterTab == "VIOLATIONS",
                                onClick = { selectedFilterTab = "VIOLATIONS" },
                                label = { Text("Naruszenia (${dataAccessRequests.count { it.isPolicyViolation && !it.requiresUserApproval }})") },
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
                                    text = when (selectedFilterTab) {
                                        "PENDING" -> "Brak oczekujących żądań dwustopniowej autoryzacji."
                                        "VIOLATIONS" -> "Brak zarejestrowanych naruszeń polityki bezpieczeństwa."
                                        else -> "Brak logów dostępu do danych."
                                    },
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
                                    DataAccessCard(
                                        request = request,
                                        onApprove = { id ->
                                            viewModel.updateDataAccessApproval(id, "Approved")
                                        },
                                        onDeny = { id ->
                                            viewModel.updateDataAccessApproval(id, "Denied")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: REAL-TIME PERMISSION SCANNER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Skaner Uprawnień Systemowych",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analizuje uprawnienia przyznane aplikacjom na urządzeniu pod kątem prywatności.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = {
                                isScanning = true
                                scannedApps = PermissionScannerAgent.scanDevicePermissions(context)
                                isScanning = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("trigger_permission_scan_btn")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Radar, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Skanuj aplikacje na urządzeniu")
                            }
                        }

                        if (scannedApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Kliknij przycisk powyżej, aby wykonać pełne skanowanie systemu.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(scannedApps) { app ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                app.riskScore >= 50 -> MaterialTheme.colorScheme.errorContainer
                                                app.riskScore >= 25 -> MaterialTheme.colorScheme.tertiaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.appName,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = app.packageName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Surface(
                                                    color = when {
                                                        app.riskScore >= 50 -> MaterialTheme.colorScheme.error
                                                        app.riskScore >= 25 -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.outline
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "Ryzyko: ${app.riskScore}%",
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Przyznane uprawnienia wrażliwe:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                app.grantedDangerousPermissions.forEach { perm ->
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text(perm, fontSize = 10.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: LOCAL-FIRST ENCRYPTED VAULT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Lokalny Szyfrowany Sejf",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Magazyn kluczy i haseł szyfrowany kluczem sprzętowym Android KeyStore. Szyfrowanie odbywa się w 100% offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Add secret inputs
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Dodaj nowy wpis do sejfu", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newSecretKey,
                                    onValueChange = { newSecretKey = it },
                                    label = { Text("Alias / Nazwa (np. token_gemini)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newSecretValue,
                                    onValueChange = { newSecretValue = it },
                                    label = { Text("Wartość (Sekret)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (newSecretKey.isNotBlank() && newSecretValue.isNotBlank()) {
                                            LocalEncryptedVault.saveSecret(context, newSecretKey, newSecretValue)
                                            vaultKeys = LocalEncryptedVault.getAllSecretsKeys(context)
                                            newSecretKey = ""
                                            newSecretValue = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("vault_save_btn"),
                                    enabled = newSecretKey.isNotBlank() && newSecretValue.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zaszyfruj i Zapisz")
                                }
                            }
                        }

                        Text("Zapisane Klucze w Sejfie:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (vaultKeys.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sejf jest pusty. Dodaj pierwszy sekret.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(vaultKeys) { key ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = key, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (decryptedSecretKey == key && decryptedSecretValue != null) {
                                                    Text(
                                                        text = decryptedSecretValue!!,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "••••••••••••",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        if (decryptedSecretKey == key) {
                                                            decryptedSecretKey = null
                                                            decryptedSecretValue = null
                                                        } else {
                                                            decryptedSecretKey = key
                                                            decryptedSecretValue = LocalEncryptedVault.getSecret(context, key)
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (decryptedSecretKey == key) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Odszyfruj"
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        LocalEncryptedVault.deleteSecret(context, key)
                                                        vaultKeys = LocalEncryptedVault.getAllSecretsKeys(context)
                                                        if (decryptedSecretKey == key) {
                                                            decryptedSecretKey = null
                                                            decryptedSecretValue = null
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataAccessCard(
    request: DataAccessRequest,
    onApprove: (Int) -> Unit,
    onDeny: (Int) -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = dateFormat.format(Date(request.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("data_access_card_${request.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (request.requiresUserApproval && request.approvalStatus == "Pending") {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
            } else if (request.isPolicyViolation) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
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
                text = "Zasób: ${request.dataType}",
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

            if (request.requiresUserApproval) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status Autoryzacji:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (request.approvalStatus) {
                                "Pending" -> "OCZEKUJE NA ZGODĘ"
                                "Approved" -> "ZATWIERDZONO"
                                "Denied" -> "ODRZUCONO"
                                else -> request.approvalStatus
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (request.approvalStatus) {
                                "Pending" -> MaterialTheme.colorScheme.tertiary
                                "Approved" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    if (request.approvalStatus == "Pending") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(request.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("approve_btn_${request.id}")
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zezwól", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = { onDeny(request.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("deny_btn_${request.id}")
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Odmów", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
