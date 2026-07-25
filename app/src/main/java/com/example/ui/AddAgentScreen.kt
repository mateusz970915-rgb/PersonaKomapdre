package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.ColonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgentScreen(
    viewModel: ColonyViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Agent") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AddAgentForm(
                onAddAgent = { name, specialty ->
                    viewModel.addAgent(
                        name = name,
                        type = specialty,
                        role = "$specialty Specialist",
                        permissions = "Basic",
                        iconName = "person"
                    )
                    onBack()
                }
            )
        }
    }
}

@Composable
fun AddAgentForm(
    onAddAgent: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Agent Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = specialty,
            onValueChange = { specialty = it },
            label = { Text("Specialty") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (name.isNotBlank() && specialty.isNotBlank()) {
                    onAddAgent(name.trim(), specialty.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && specialty.isNotBlank()
        ) {
            Text("Create Agent")
        }
    }
}
