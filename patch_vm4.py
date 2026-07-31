with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("            }\n        }\n    \n    fun updateHasSeenWalkthrough", "            }\n        }\n    }\n\n    fun updateHasSeenWalkthrough")

with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.write(text)
