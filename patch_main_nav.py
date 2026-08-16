import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the persona_mesh composable
old_composable = """                    composable("persona_mesh") {
                        com.example.ui.PersonaMeshScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }"""

new_composable = """                    composable("persona_mesh") {
                        com.example.ui.PersonaMeshScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { navController.navigate("persona_settings") },
                            onBack = { navController.popBackStack() }
                        )
                    }"""

content = content.replace(old_composable, new_composable)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
