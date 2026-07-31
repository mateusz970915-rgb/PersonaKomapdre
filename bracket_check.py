import sys

def check_brackets(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    stack = []
    line_num = 1
    for char in content:
        if char == '\n':
            line_num += 1
        elif char == '{':
            stack.append(line_num)
        elif char == '}':
            if not stack:
                print(f"Extra closing bracket '}}' at line {line_num}")
            else:
                stack.pop()
    
    if stack:
        print(f"Unclosed opening bracket '{{' found at lines: {stack}")
    else:
        print("Brackets are balanced!")

check_brackets("app/src/main/java/com/example/ui/DashboardScreen.kt")
