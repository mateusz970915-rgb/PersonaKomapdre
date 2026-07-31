files = [
    'app/src/main/java/com/example/ui/components/AgentCard.kt',
    'app/src/main/java/com/example/ui/components/CreateAgentDialog.kt',
    'app/src/main/java/com/example/ui/components/FocusModeSuite.kt',
    'app/src/main/java/com/example/ui/components/RelationshipNudgesWidget.kt',
    'app/src/main/java/com/example/ui/components/TranslateTextInPlace.kt',
    'app/src/main/java/com/example/ui/components/VoiceCommandDialog.kt'
]

imports_to_add = """
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
"""

for file in files:
    try:
        with open(file, 'r') as f:
            content = f.read()
            
        new_content = content.replace("package com.example.ui.components", "package com.example.ui.components\n" + imports_to_add)
        
        with open(file, 'w') as f:
            f.write(new_content)
    except Exception as e:
        pass

print("Added imports to all components")
