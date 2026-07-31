import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

old_sort = """        when (sortBy) {
            "Name" -> result.sortedBy { it.name.lowercase() }
            "Role" -> result.sortedBy { it.type.lowercase() }
            "Status" -> result.sortedBy { it.status.lowercase() }
            else -> result
        }"""

new_sort = """        when (sortBy) {
            "Name", "name" -> result.sortedBy { it.name.lowercase() }
            "Role" -> result.sortedBy { it.type.lowercase() }
            "Status" -> result.sortedBy { it.status.lowercase() }
            "last_active" -> result.sortedByDescending { it.lastActiveTimestamp }
            else -> result
        }"""

content = content.replace(old_sort, new_sort)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
