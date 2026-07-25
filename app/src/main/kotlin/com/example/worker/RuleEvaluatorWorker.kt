package com.example.worker

import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.Context

/**
 * DEPRECATED: Rule evaluation engine deferred to Phase 2.
 *
 * This worker is NOT wired into WorkManager and will never execute.
 * It was removed to prevent fake-success messaging.
 *
 * The rule engine required:
 * - Rule persistence layer (complete)
 * - Rule interpreter/evaluator (incomplete)
 * - Effect handlers for actions (incomplete)
 * - WorkManager integration (incomplete)
 * - Verification of rule execution (incomplete)
 *
 * Status: Aspirational. Not production-ready.
 *
 * TODO Phase 2:
 * 1. Design rule execution semantics
 * 2. Implement rule interpreter
 * 3. Add effect handlers
 * 4. Wire WorkManager integration
 * 5. Add comprehensive tests
 * 6. Update UI to show rule status
 */
@Deprecated("Rule engine deferred to Phase 2. Not wired into WorkManager.", level = DeprecationLevel.WARNING)
class RuleEvaluatorWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        // Placeholder: This worker is never called.
        // If you see this in logs, rule engine has been implemented.
        return Result.failure()
    }
}
