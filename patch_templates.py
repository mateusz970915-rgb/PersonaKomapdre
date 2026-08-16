import re

with open('app/src/main/java/com/example/ui/PersonaMeshScreen.kt', 'r') as f:
    content = f.read()

old_block = """    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var personality by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New PersonaAgent") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField("""

new_block = """    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var personality by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }

        val templates = listOf(
            Triple("Creative", "Imaginative and Unconventional", "Ideation and Brainstorming"),
            Triple("Analytical", "Logical and Detail-Oriented", "Data Analysis and Logic"),
            Triple("Practical", "Pragmatic and Efficient", "Task Execution and Planning")
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New PersonaAgent") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick Templates:", style = MaterialTheme.typography.labelMedium)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(templates) { (tName, tPers, tRole) ->
                            OutlinedButton(
                                onClick = {
                                    name = tName
                                    personality = tPers
                                    role = tRole
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(tName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    OutlinedTextField("""

content = content.replace(old_block, new_block)

with open('app/src/main/java/com/example/ui/PersonaMeshScreen.kt', 'w') as f:
    f.write(content)
