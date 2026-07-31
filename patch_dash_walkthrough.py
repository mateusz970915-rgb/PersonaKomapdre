with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

target = """        if (showExportDialog) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current"""

replacement = """        if (!preferences.hasSeenWalkthrough) {
            WalkthroughOverlay(
                onDismiss = {
                    viewModel.updateHasSeenWalkthrough(true)
                }
            )
        }
        
        if (showExportDialog) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Warning: could not find target for WalkthroughOverlay in DashboardScreen")

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
