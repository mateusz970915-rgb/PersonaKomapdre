import subprocess
result = subprocess.run(["gradle", ":app:compileDemoDebugKotlin", "--no-daemon"], capture_output=True, text=True)
print(result.stdout)
print(result.stderr)
if result.returncode != 0:
    exit(1)
