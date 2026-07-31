with open("app/build.gradle.kts", "r") as f:
    content = f.read()

target = "isMinifyEnabled = false"
repl = "isMinifyEnabled = true"

content = content.replace(target, repl)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
