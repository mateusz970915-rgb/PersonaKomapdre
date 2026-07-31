import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

# find val agentActivityLog = remember(selectedAgentForActivity, decisions, subTasksList) {
old_code = "val agentActivityLog = remember(selectedAgentForActivity, decisions, subTasksList) {"
new_code = "val agentActivityLog = remember<List<ActivityEntry>>(selectedAgentForActivity, decisions, subTasksList) {"

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
