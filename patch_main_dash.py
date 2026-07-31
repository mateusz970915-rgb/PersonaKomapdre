import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add onNavigateToAddAgent to DashboardScreen signature
old_sig = """fun DashboardScreen(
    viewModel: ColonyViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToEfficiency: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBehaviors: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSuggested: () -> Unit,
    onNavigateToSettings: () -> Unit,"""

new_sig = """fun DashboardScreen(
    viewModel: ColonyViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToEfficiency: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBehaviors: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSuggested: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddAgent: () -> Unit = {},"""

content = content.replace(old_sig, new_sig)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
