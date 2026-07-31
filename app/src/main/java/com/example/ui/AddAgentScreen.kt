package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
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
    var name by remember { mutableStateOf("") }
    var personaDescription by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Agent") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Agent Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personaDescription,
                onValueChange = { personaDescription = it },
                label = { Text("Persona Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = { Text("Avatar URL (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addAgent(
                            name = name.trim(),
                            type = "CUSTOM",
                            role = "Assistant",
                            permissions = "Basic",
                            personaDescription = personaDescription.trim(),
                            avatarUrl = avatarUrl.trim()
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Agent")
            }
        }
    }
}
