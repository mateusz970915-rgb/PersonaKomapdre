import re
import os

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

def extract_function(func_name, lines):
    start_idx = -1
    for i, line in enumerate(lines):
        if re.match(rf'^(@Composable\s+)?(@OptIn.*)?(fun\s+{func_name}\()', line.strip()):
            start_idx = i
            break
            
    if start_idx == -1:
        # Check if there are multiple lines of annotations
        for i, line in enumerate(lines):
            if re.match(rf'^(fun\s+{func_name}\()', line.strip()):
                start_idx = i
                break
                
    if start_idx == -1:
        return None, lines
        
    # Find end of function
    bracket_count = 0
    in_func = False
    end_idx = -1
    
    for i in range(start_idx, len(lines)):
        line = lines[i]
        if '{' in line:
            bracket_count += line.count('{')
            in_func = True
        if '}' in line:
            bracket_count -= line.count('}')
            
        if in_func and bracket_count == 0:
            end_idx = i
            break
            
    if end_idx == -1:
        return None, lines
        
    func_lines = lines[start_idx:end_idx+1]
    
    # Preceding annotations
    while start_idx > 0 and lines[start_idx-1].strip().startswith('@'):
        start_idx -= 1
        func_lines.insert(0, lines[start_idx])
        
    remaining_lines = lines[:start_idx] + lines[end_idx+1:]
    return "".join(func_lines), remaining_lines

funcs_to_extract = ["AgentCard", "CreateAgentDialog", "TranslateTextInPlace", "FocusModeSuite", "VoiceCommandDialog", "RelationshipNudgesWidget"]
os.makedirs('app/src/main/java/com/example/ui/components', exist_ok=True)

for func in funcs_to_extract:
    func_content, lines = extract_function(func, lines)
    if func_content:
        imports = """package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.example.data.*
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
"""
        with open(f'app/src/main/java/com/example/ui/components/{func}.kt', 'w') as out:
            out.write(imports + "\n" + func_content)
        print(f"Extracted {func}")
    else:
        print(f"Failed to extract {func}")

# Add component imports to remaining DashboardScreen
import_stmts = """import com.example.ui.components.AgentCard
import com.example.ui.components.CreateAgentDialog
import com.example.ui.components.TranslateTextInPlace
import com.example.ui.components.FocusModeSuite
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.components.RelationshipNudgesWidget
"""

# Find package decl
for i, line in enumerate(lines):
    if line.startswith('package '):
        lines.insert(i + 1, "\n" + import_stmts)
        break

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.writelines(lines)

print("Updated DashboardScreen.kt")
