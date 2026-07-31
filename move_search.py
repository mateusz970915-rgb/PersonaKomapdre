import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

search_bar_code = """                    // 1. SEARCH BAR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search agents by name or role...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("search_bar_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

"""

# Let's remove the search bar from its current location
content = content.replace(search_bar_code, "")

# Find where to insert it at the top
insert_target = """                    }
                    
                    Image("""

insert_replacement = """                    }
                    
""" + search_bar_code + """                    Image("""

content = content.replace(insert_target, insert_replacement)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
