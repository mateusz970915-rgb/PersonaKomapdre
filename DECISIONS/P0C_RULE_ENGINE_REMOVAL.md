# P0-C: Rule Engine Cleanup — Decision Log

**Date:** 2026-07-25  
**Phase:** P0-C (Rule Engine Wiring or Removal)  
**Decision:** REMOVE dead rule engine  

## Findings

**P0-04 from audit report:**
- `RuleEvaluatorWorker` exists as class
- No `WorkManager.enqueue()` call found anywhere
- `executeSubTaskReal()` logs `"Evaluated rules and triggered automated background tasks"` without running anything
- `RULE_EVALUATION` action type assigned but never triggers real work

**Contradiction:**
- UI claims: "rules evaluated, tasks triggered"
- Reality: Dead worker, no WorkManager integration, no effect handlers
- Result: Fake-success message, dead code, misleading system state

## Options Considered

### Option A: Wire the Rule Engine (Real)
**Pros:**
- Feature complete
- Enables rule-based automation

**Cons:**
- Requires: rule interpreter, effect handlers, verification
- Significant implementation effort
- Not yet designed
- Current code is just skeleton

**Decision: NOT NOW** — Incomplete and would duplicate fake-success problem.

### Option B: Remove Dead Code (Truth)
**Pros:**
- Eliminates fake-success messaging
- Removes dead code and dead imports
- Honest about phase 2 deferral
- Simplifies codebase
- Prevents maintenance confusion

**Cons:**
- Feature not available
- Must update UI expectations

**Decision: CHOSEN** — Prevents deception, aligns with truth principle.

## Changes Made

1. **Deprecated RuleEvaluatorWorker**
   - Marked with `@Deprecated` annotation
   - Added comment: "Not wired into WorkManager"
   - Included Phase 2 requirements

2. **Removed fake-success message**
   - Deleted "Evaluated rules and triggered..." log from executeSubTaskReal()
   - Replaced with honest ExecutionOutcome.Simulated if rule action attempted

3. **Removed RULE_EVALUATION action type**
   - Deleted constant from ActionType
   - Commented out with reference to Phase 2
   - Updated docstring

4. **Cleaned up dead imports**
   - Removed unused WorkManager imports from MainActivity
   - Removed RuleEvaluatorWorker imports

5. **Added documentation**
   - RULE_ENGINE_DEPRECATION.md explains decision
   - ActionType.kt docstring clarifies status
   - Code comments reference P0-04 and Agents.md

## Verification

**Gate criteria:**
- ✅ No fake "triggered" messages in logs
- ✅ No WorkManager calls for rules
- ✅ RuleEvaluatorWorker marked deprecated
- ✅ Decision documented for Phase 2 planning
- ✅ Prevents P0-04 false claims

## Phase 2 Plan: Rule Engine Implementation

When implementing rule engine:
1. Design rule execution model (async, cancellable, retryable)
2. Implement rule interpreter (conditions → actions)
3. Create effect handlers (what rules can do)
4. Integrate WorkManager (deferred work scheduling)
5. Add ExecutionOutcome logging (proof of execution)
6. Create comprehensive tests
7. Update UI to show rule status and results

## References

- **Audit Finding:** P0-04 (Rule engine ogłasza działania, których nie uruchamia)
- **Design Principle:** Agents.md (real execution pipelines, not mockups)
- **Architecture:** ExecutionOutcome types prevent fake-success
- **Next Phase:** Phase 2 planning document
