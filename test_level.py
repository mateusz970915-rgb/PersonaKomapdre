import sys

def check_top_level(file_path, func_name):
    with open(file_path, 'r') as f:
        content = f.read()

    stack = []
    line_num = 1
    found = False
    for char in content:
        if char == '\n':
            line_num += 1
        elif char == '{':
            stack.append(line_num)
        elif char == '}':
            if stack:
                stack.pop()
        
        if not found and func_name in content.split('\n')[line_num-1]:
            print(f"Function {func_name} at line {line_num} has depth {len(stack)}")
            found = True

check_top_level("app/src/main/java/com/example/ui/DashboardScreen.kt", "fun VoiceCommandDialog")
check_top_level("app/src/main/java/com/example/ui/DashboardScreen.kt", "fun AgentCard")
