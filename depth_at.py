import sys

def depth_at(file_path, target_line):
    with open(file_path, 'r') as f:
        content = f.read()
    
    lines = content.split('\n')
    depth = 0
    for i, line in enumerate(lines):
        for char in line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
        
        if i + 1 == target_line:
            print(f"Depth after line {target_line}: {depth}")
            break
            
depth_at("app/src/main/java/com/example/ui/DashboardScreen.kt", 1839)
