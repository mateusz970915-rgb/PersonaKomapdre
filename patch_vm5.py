with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    text = f.read()

if text.endswith("}\n}\n}") or text.endswith("}\n}\n}\n"):
    text = text.rsplit("}", 1)[0]

with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.write(text)
