import sys

def trace_braces(file_path):
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
        
        # We print lines where depth goes to 0 (top level functions ending)
        if depth == 0 and '}' in line:
            print(f"Top level closed at line {i+1}: {line}")
            
trace_braces("app/src/main/java/com/example/ui/DashboardScreen.kt")
