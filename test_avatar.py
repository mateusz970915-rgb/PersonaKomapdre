import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

# count occurrences of fun AgentCard
print(content.count("fun AgentCard"))
