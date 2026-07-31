with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
for i, line in enumerate(lines):
    if line.strip() == ") {" and lines[i+1].strip() == "var showMenu by remember { mutableStateOf(false) }":
        start_idx = i
        break

if start_idx != -1:
    end_idx = -1
    bracket_count = 1
    for i in range(start_idx + 1, len(lines)):
        bracket_count += lines[i].count('{')
        bracket_count -= lines[i].count('}')
        if bracket_count == 0:
            end_idx = i
            break
            
    if end_idx != -1:
        body = lines[start_idx:end_idx+1]
        
        with open('app/src/main/java/com/example/ui/components/AgentCard.kt', 'a') as out:
            out.writelines(body)
            
        remaining = lines[:start_idx] + lines[end_idx+1:]
        with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
            f.writelines(remaining)
        print("Fixed AgentCard")
        
