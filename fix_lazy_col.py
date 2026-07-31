import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_col = """            LazyColumn(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),"""

good_col = """            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),"""

content = content.replace(bad_col, good_col)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
