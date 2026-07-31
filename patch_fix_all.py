import re

# Fix AgentDashboardScreen
with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "r") as f:
    content = f.read()

# Add Share import
if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Share")

# Fix the brace issue
bad_block = """            if (!isSelectionMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Agent", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}"""

good_block = """            if (!isSelectionMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Agent", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}"""

if bad_block in content:
    content = content.replace(bad_block, good_block)

# Wait, the error is at line 492, maybe I should just count braces.
# Actually, the bad block might have different indentation. Let's use regex for the end of AgentCard.

with open("app/src/main/java/com/example/ui/AgentDashboardScreen.kt", "w") as f:
    f.write(content)

# Fix DashboardScreen
with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    dash_content = f.read()

if "import androidx.compose.material.icons.filled.FileDownload" not in dash_content:
    dash_content = dash_content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.FileDownload")

if "import androidx.compose.material.icons.filled.Share" not in dash_content:
    dash_content = dash_content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Share")

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(dash_content)

