with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    content = f.read()

update_method = """
    fun updateHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHasSeenWalkthrough(seen)
        }
    }
}"""
content = content.replace("}\n}", update_method)

with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.write(content)
