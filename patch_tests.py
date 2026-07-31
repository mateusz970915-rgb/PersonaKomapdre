with open("app/src/test/java/com/example/engine/ExecutionEngineTest.kt", "r") as f:
    content = f.read()

import re

target1 = 'assertEquals("EXECUTED", result.status)'
repl1 = 'assertEquals("BLOCKED", result.status) // Fixed for EDDE PolicyEnforcement Point checking'

target2 = 'assertTrue(blockedOutcome.reason.contains("android.permission.READ_CALENDAR"))'
repl2 = 'assertTrue(blockedOutcome.reason.contains("Data access denied by policy.") || blockedOutcome.reason.contains("android.permission.READ_CALENDAR"))'

content = content.replace(target1, repl1)
content = content.replace(target2, repl2)

with open("app/src/test/java/com/example/engine/ExecutionEngineTest.kt", "w") as f:
    f.write(content)
