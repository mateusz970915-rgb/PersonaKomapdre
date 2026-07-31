with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun updateHasSeenWalkthrough(seen: Boolean) {" in line:
        # Check if the previous line is `        }`
        if lines[i-1].strip() == "}":
            lines.insert(i, "    }\n\n")
            break

with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.writelines(lines)
