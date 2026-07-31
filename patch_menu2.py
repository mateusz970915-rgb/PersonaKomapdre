import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

target = r'(imageVector = Icons\.Default\.Delete,\s*contentDescription = null,\s*tint = MaterialTheme\.colorScheme\.error\s*\)\s*\}\s*\)\s*)(\}\s*Column\()'
replacement = r'\1DropdownMenuItem(text={Text("Export Agent")},onClick={onExport();showMenu=false},leadingIcon={Icon(imageVector=Icons.Default.Share,contentDescription=null)})\n            \2'

new_content = re.sub(target, replacement, content, count=1)
if new_content == content:
    print("Warning: could not find target for DropdownMenuItem")
else:
    with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
        f.write(new_content)

