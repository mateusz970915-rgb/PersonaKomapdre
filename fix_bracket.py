import re

file_path = "app/src/main/java/com/example/ui/DashboardScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

# Add one more } before @Composable fun CreateAgentDialog
content = re.sub(r'(?=\@Composable\s+fun CreateAgentDialog)', '}\n', content)

with open(file_path, 'w') as f:
    f.write(content)
