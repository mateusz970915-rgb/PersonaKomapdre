files_to_fix = [
    'app/src/main/java/com/example/ui/components/RelationshipNudgesWidget.kt',
    'app/src/main/java/com/example/ui/components/VoiceCommandDialog.kt'
]
imports = """import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
"""

for file in files_to_fix:
    with open(file, 'r') as f:
        content = f.read()
    with open(file, 'w') as f:
        f.write(content.replace('package com.example.ui.components\n', 'package com.example.ui.components\n' + imports))
        
print("Fixed imports")
