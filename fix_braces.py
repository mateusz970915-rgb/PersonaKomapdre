import re
with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(
"""        }
}}}}
data class ActivityEntry(""",
"""        }
}

data class ActivityEntry("""
)
with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(text)
