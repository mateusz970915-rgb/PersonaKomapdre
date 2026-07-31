with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("        fun updateHasSeenWalkthrough", "    }\n\n    fun updateHasSeenWalkthrough")

with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.write(text)
