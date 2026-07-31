package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FinanceTransaction
import com.example.data.Subscription
import com.example.viewmodel.ColonyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAgentScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.financeTransactions.collectAsStateWithLifecycle()
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Rejestr, 1 = Subskrypcje & Kill Switch
    
    var csvInput by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    var manualTitle by remember { mutableStateOf("") }
    var manualAmount by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("Food") }
    
    val categories = listOf("Food", "Rent", "Utilities", "Salary", "Entertainment", "Other")
    
    // Statistics Calculations
    val totalExpenses = transactions.filter { it.category != "Salary" }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.category == "Salary" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    val categoryTotals = remember(transactions) {
        transactions.filter { it.category != "Salary" }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }
    
    val formattedBalance = remember(netBalance) {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.format(netBalance)
    }

    val formattedIncome = remember(totalIncome) {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.format(totalIncome)
    }

    val formattedExpenses = remember(totalExpenses) {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.format(totalExpenses)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Finance Agent", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("finance_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeTab == 0) {
                        IconButton(
                            onClick = { viewModel.clearFinanceTransactions() },
                            modifier = Modifier.testTag("clear_finance_btn")
                        ) {
                            Icon(Icons.Default.ClearAll, contentDescription = "Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj transakcję", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.testTag("tab_transactions"),
                    text = { Text("Rejestr Transakcji", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("tab_subscriptions"),
                    text = { Text("Subskrypcje & Kill Switch", fontWeight = FontWeight.Bold) }
                )
            }

            if (activeTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overview Cards
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    netBalance >= 0 -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Bilans Netto",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = when {
                                        netBalance >= 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                                Text(
                                    text = formattedBalance,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        netBalance >= 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onErrorContainer
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Przychody", style = MaterialTheme.typography.bodySmall)
                                        Text(formattedIncome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Wydatki", style = MaterialTheme.typography.bodySmall)
                                        Text(formattedExpenses, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // CSV Parsing Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Import transakcji z pliku CSV / Tekstu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Format: Nazwa,Kwota,Kategoria (np: Zakupy Spożywcze,25.50,Food)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = csvInput,
                                    onValueChange = { csvInput = it },
                                    placeholder = { Text("Skopiuj i wklej zawartość CSV...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("csv_input_field"),
                                    maxLines = 5
                                )
                                Button(
                                    onClick = {
                                        if (csvInput.isNotBlank()) {
                                            viewModel.importCsvTransactions(csvInput)
                                            csvInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("import_csv_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = "Import CSV")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Zaimportuj dane CSV")
                                }
                            }
                        }
                    }

                    // Category Distribution Chart via Custom Canvas
                    if (categoryTotals.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Rozkład wydatków (bez wynagrodzeń)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    val expenseColors = listOf(
                                        Color(0xFFFF5722), // Food
                                        Color(0xFF2196F3), // Rent
                                        Color(0xFF4CAF50), // Utilities
                                        Color(0xFFE91E63), // Entertainment
                                        Color(0xFF9C27B0)  // Other
                                    )

                                    val listSums = categoryTotals.toList().sortedByDescending { it.second }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            var startAngle = -90f
                                            val totalVal = listSums.sumOf { it.second }.toFloat()
                                            if (totalVal > 0) {
                                                listSums.forEachIndexed { index, pair ->
                                                    val sweepAngle = (pair.second.toFloat() / totalVal) * 360f
                                                    drawArc(
                                                        color = expenseColors[index % expenseColors.size],
                                                        startAngle = startAngle,
                                                        sweepAngle = sweepAngle,
                                                        useCenter = false,
                                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                                    )
                                                    startAngle += sweepAngle
                                                }
                                            } else {
                                                drawCircle(
                                                    color = Color.LightGray,
                                                    style = Stroke(width = 20f)
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Wydatki", style = MaterialTheme.typography.labelSmall)
                                            Text(
                                                text = formattedExpenses,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Legend Grid
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listSums.forEachIndexed { index, pair ->
                                            val color = expenseColors[index % expenseColors.size]
                                            val formattedVal = NumberFormat.getCurrencyInstance(Locale.US).format(pair.second)
                                            val percentage = if (totalExpenses > 0) {
                                                (pair.second / totalExpenses * 100).toInt()
                                            } else 0
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = pair.first,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "$percentage% ($formattedVal)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Ledger Transaction list
                    item {
                        Text(
                            text = "Rejestr Transakcji (${transactions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Brak transakcji. Kliknij + lub wklej CSV powyżej.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(transactions) { tx ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (tx.category == "Salary") MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tx.category,
                                                    fontSize = 10.sp,
                                                    color = if (tx.category == "Salary") MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
                                            Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    val prefix = if (tx.category == "Salary") "+" else "-"
                                    val txFormatted = NumberFormat.getCurrencyInstance(Locale.US).format(tx.amount)
                                    Text(
                                        text = "$prefix$txFormatted",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.category == "Salary") Color(0xFF4CAF50) else Color(0xFFFF5722)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // SUBSCRIPTION AUDIT & KILL SWITCH VIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Inteligentny Audytor Subskrypcji",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Analizuje cały rejestr transakcji za pomocą wyrażeń regularnych w poszukiwaniu powtarzalnych opłat SaaS, chmur i rozrywki cyfrowej.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val patterns = listOf(
                                            "netflix", "spotify", "amazon", "github", "figma", "openai", "chatgpt", "adobe",
                                            "microsoft", "google", "cloud", "workspace", "premium", "saas", "hulu", "disney",
                                            "apple", "canva", "notion", "youtube", "hbo"
                                        ).map { Regex(it, RegexOption.IGNORE_CASE) }

                                        val detected = mutableListOf<Subscription>()
                                        transactions.forEach { tx ->
                                            val matches = patterns.any { it.containsMatchIn(tx.title) }
                                            if (matches) {
                                                detected.add(
                                                    Subscription(
                                                        title = tx.title,
                                                        amount = tx.amount,
                                                        category = tx.category,
                                                        frequency = "Monthly",
                                                        isCancelled = false,
                                                        nextBillingDate = System.currentTimeMillis() + (30L * 24 * 3600 * 1000)
                                                    )
                                                )
                                            }
                                        }

                                        if (detected.isNotEmpty()) {
                                            viewModel.insertSubscriptions(detected)
                                            viewModel.insertMemory(
                                                com.example.data.ColonyMemory(
                                                    content = "[Smart Finance Audit] Przeanalizowano transakcje. Wykryto ${detected.size} aktywnych subskrypcji cyklicznych."
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("run_audit_btn")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Uruchom Skaner Subskrypcji", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Wykryte i Monitorowane Usługi (${subscriptions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (subscriptions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.HeartBroken, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Brak wykrytych subskrypcji. Kliknij skaner powyżej.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    } else {
                        items(subscriptions) { sub ->
                            val isCancelled = sub.isCancelled
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sub_card_${sub.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCancelled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                sub.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCancelled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "Częstotliwość: ${sub.frequency} • Koszt: ${NumberFormat.getCurrencyInstance(Locale.US).format(sub.amount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        
                                        // KILL SWITCH SWITCH
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                if (isCancelled) "Zabita (Anulowana)" else "Aktywna",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCancelled) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Switch(
                                                checked = !isCancelled,
                                                onCheckedChange = { active ->
                                                    viewModel.updateSubscriptionCancelled(sub.id, !active)
                                                    viewModel.insertMemory(
                                                        com.example.data.ColonyMemory(
                                                            content = "[Kill Switch] Zmieniono stan subskrypcji '${sub.title}' na: ${if (active) "AKTYWNA" else "ANULOWANA / ZABITA"}."
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.testTag("kill_switch_toggle_${sub.id}"),
                                                thumbContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.PowerSettingsNew,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                                    )
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { viewModel.deleteSubscription(sub.id) },
                                                modifier = Modifier.testTag("delete_sub_btn_${sub.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                    
                                    if (!isCancelled) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        val billingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sub.nextBillingDate))
                                        Text(
                                            "Następna data rozliczenia: $billingDate",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Manual Transaction Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Dodaj transakcję") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Tytuł transakcji") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_tx_title_input")
                        )
                        OutlinedTextField(
                            value = manualAmount,
                            onValueChange = { manualAmount = it },
                            label = { Text("Kwota ($)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_tx_amount_input")
                        )
                        Text("Kategoria:")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = manualCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { manualCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = manualAmount.toDoubleOrNull() ?: 0.0
                            if (manualTitle.isNotBlank() && amt > 0.0) {
                                viewModel.insertFinanceTransaction(
                                    FinanceTransaction(
                                        title = manualTitle,
                                        amount = amt,
                                        category = manualCategory
                                    )
                                )
                                manualTitle = ""
                                manualAmount = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_tx_btn")
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}
