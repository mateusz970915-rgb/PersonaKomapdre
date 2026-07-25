/**
 * Action types for SubTask execution.
 *
 * Each action type maps to a specific executor:
 * - LLM_PROMPT: Gemini API for reasoning/analysis (Simulated execution)
 * - CALENDAR_SYNC: CalendarProvider integration (Real execution with evidence)
 * - CONTACT_SYNC: ContactProvider integration (Future: Real execution)
 * - PERMISSION_CHECK: Android permission scanner (Real execution with evidence)
 *
 * REMOVED:
 * - RULE_EVALUATION: Was fake-success, deferred to Phase 2
 * - NOTIFICATION_SEND: No notification logic exists
 * - DATA_BACKUP: No backup logic exists
 *
 * Principle: Only action types with complete executors are declared.
 * Incomplete features are deferred, not faked.
 */
object ActionType {
    const val LLM_PROMPT = "LLM_PROMPT"
    const val CALENDAR_SYNC = "CALENDAR_SYNC"
    const val CONTACT_SYNC = "CONTACT_SYNC"
    const val PERMISSION_CHECK = "PERMISSION_CHECK"
    // const val RULE_EVALUATION = "RULE_EVALUATION"  // Deferred to Phase 2
}
