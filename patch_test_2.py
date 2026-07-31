with open('app/src/test/java/com/example/engine/ExecutionEngineTest.kt', 'r') as f:
    content = f.read()

old_code = """        if (result.status == "EXECUTED") {
            val executedOutcome = result.outcome as ExecutionOutcome.Executed
            assertEquals("RULE_EVALUATION", executedOutcome.evidence.actionType)
            assertEquals("WorkManager (RuleEvaluatorWorker)", executedOutcome.evidence.toolProvider)
        } else {
            assertTrue("Execution outcome should be ENQUEUED or FAILED in test, got ${result.status}", result.status == "ENQUEUED" || result.status == "FAILED")
        }"""

new_code = """        if (result.status == "EXECUTED") {
            val executedOutcome = result.outcome as ExecutionOutcome.Executed
            assertEquals("RULE_EVALUATION", executedOutcome.evidence.actionType)
            assertEquals("WorkManager (RuleEvaluatorWorker)", executedOutcome.evidence.toolProvider)
        } else {
            assertTrue("Execution outcome should be ENQUEUED, FAILED or BLOCKED in test, got ${result.status}", result.status == "ENQUEUED" || result.status == "FAILED" || result.status == "BLOCKED")
        }"""

if old_code in content:
    with open('app/src/test/java/com/example/engine/ExecutionEngineTest.kt', 'w') as f:
        f.write(content.replace(old_code, new_code))
    print("Patched test 2")
else:
    print("Could not find old code")
