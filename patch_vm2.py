with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "r") as f:
    content = f.read()

bad = """            } catch (e: Exception) {
                android.util.Log.e("BaseAgentViewModel", "Error toggling system DND filter", e)
            }
        }
        fun updateHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHasSeenWalkthrough(seen)
        }
    }
}"""

good = """            } catch (e: Exception) {
                android.util.Log.e("BaseAgentViewModel", "Error toggling system DND filter", e)
            }
        }
    }

    fun updateHasSeenWalkthrough(seen: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHasSeenWalkthrough(seen)
        }
    }
}"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/viewmodel/BaseAgentViewModel.kt", "w") as f:
    f.write(content)
