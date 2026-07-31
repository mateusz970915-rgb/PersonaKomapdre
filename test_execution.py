with open("app/src/main/java/com/example/engine/ExecutionEngine.kt", "r") as f:
    content = f.read()

import re

# Remove the simulated ExecutionEngine logic for SIMULATED tasks if we need them to be real. 
# But let's check what exactly the user wants with "Likwidacja Fake-Success w ExecutionEngine (P0-1)"

target = 'TaskExecutionResult(\n                        outcome = ExecutionOutcome.Simulated(reply),'
if target in content:
    print("Found simulated response")
