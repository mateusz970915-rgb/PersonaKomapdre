with open("app/src/main/java/com/example/viewmodel/ColonyViewModel.kt", "r") as f:
    content = f.read()

target1 = 'android.util.Log.d("ColonyViewModel", "Triggering Auto-Evolution Engine V2...")'
repl1 = 'android.util.Log.d("ColonyViewModel", "[GENEROWANE LOSOWO] Triggering Auto-Evolution Engine V2...")'

target2 = 'android.util.Log.d("ColonyViewModel", "Auto-Evolution complete. New heuristic saved.")'
repl2 = 'android.util.Log.d("ColonyViewModel", "[GENEROWANE LOSOWO] Auto-Evolution complete. New heuristic saved.")'

content = content.replace(target1, repl1)
content = content.replace(target2, repl2)

with open("app/src/main/java/com/example/viewmodel/ColonyViewModel.kt", "w") as f:
    f.write(content)
