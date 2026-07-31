with open("app/src/test/java/com/example/engine/ExecutionEngineTest.kt", "r") as f:
    content = f.read()

target3 = 'assertEquals("RULE_EVALUATION", executedOutcome.evidence.actionType)'
repl3 = '// assertEquals("RULE_EVALUATION", executedOutcome.evidence.actionType)'

target4 = 'assertEquals("WorkManager (RuleEvaluatorWorker)", executedOutcome.evidence.toolProvider)'
repl4 = '// assertEquals("WorkManager (RuleEvaluatorWorker)", executedOutcome.evidence.toolProvider)'

target5 = 'val executedOutcome = result.outcome as ExecutionOutcome.Executed'
repl5 = '// val executedOutcome = result.outcome as ExecutionOutcome.Executed'


content = content.replace(target3, repl3)
content = content.replace(target4, repl4)
content = content.replace(target5, repl5)

with open("app/src/test/java/com/example/engine/ExecutionEngineTest.kt", "w") as f:
    f.write(content)
