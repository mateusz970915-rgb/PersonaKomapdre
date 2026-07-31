import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add Export DropdownMenuItem
target = r"""                    leadingIcon = \{
                        Icon\(
                            imageVector = Icons\.Default\.Delete,
                            contentDescription = null,
                            tint = MaterialTheme\.colorScheme\.error
                        \)
                    \}
                \)
            \}
            Column\("""
            
replacement = """                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export Agent") },
                    onClick = {
                        onExport()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null
                        )
                    }
                )
            }
            Column("""

new_content = re.sub(target, replacement, content, count=1)
if new_content == content:
    print("Warning: could not find target for DropdownMenuItem")
else:
    with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
        f.write(new_content)

