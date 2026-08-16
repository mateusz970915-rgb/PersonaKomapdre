import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    text = f.read()

start_index = text.find('// ModalBottomSheet for Agent Interaction History')
if start_index == -1:
    print("Not found")
    exit(1)

brace_count = 0
found_brace = False
end_index = -1

for i in range(start_index, len(text)):
    if text[i] == '{':
        brace_count += 1
        found_brace = True
    elif text[i] == '}':
        brace_count -= 1
        
    if found_brace and brace_count == 0:
        end_index = i
        break

if end_index != -1:
    print(text[start_index:end_index+1])
