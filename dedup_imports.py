import os
import glob

files = glob.glob('app/src/main/java/com/example/ui/components/*.kt')

for file in files:
    with open(file, 'r') as f:
        lines = f.readlines()
        
    seen = set()
    new_lines = []
    
    for line in lines:
        if line.startswith('import '):
            if line not in seen:
                seen.add(line)
                new_lines.append(line)
        else:
            new_lines.append(line)
            
    with open(file, 'w') as f:
        f.writelines(new_lines)
        
print("Deduplicated imports")
