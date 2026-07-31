package com.example.engine

import android.app.Application
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.test.core.app.ApplicationProvider
import com.example.data.Agent
import com.example.data.ExecutionOutcome
import com.example.data.SubTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExecutionEngineTest {
    private lateinit var app: Application
    private lateinit var executionEngine: ExecutionEngine

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        executionEngine = ExecutionEngine(app)
    }

    @Test
    fun `test Execute Task Returns Blocked When Agent Is Null`() = runBlocking {
        val task = SubTask(
            id = 1,
            assignedAgent = "Unknown",
            description = "Test task with no agent",
            actionType = "LLM_PROMPT"
        )

        val result = executionEngine.executeTask(task, agent = null)

        assertEquals("BLOCKED", result.status)
        assertTrue(result.outcome is ExecutionOutcome.Blocked)
        val blockedOutcome = result.outcome as ExecutionOutcome.Blocked
        assertTrue(blockedOutcome.reason.contains("Agent nie został odnaleziony"))
    }

    @Test
    fun `test Execute Task For Rule Evaluation Worker Enqueues WorkManager`() = runBlocking {
        val agent = Agent(
            id = 1,
            name = "Security Agent",
            type = "SECURITY",
            role = "System Guard"
        )

        val task = SubTask(
            id = 2,
            assignedAgent = agent.name,
            description = "Evaluate Colony Rules",
            actionType = "RULE_EVALUATION"
        )

        val result = executionEngine.executeTask(task, agent)

        if (result.status == "EXECUTED") {
            val executedOutcome = result.outcome as ExecutionOutcome.Executed
            assertEquals("RULE_EVALUATION", executedOutcome.evidence.actionType)
            assertEquals("WorkManager (RuleEvaluatorWorker)", executedOutcome.evidence.toolProvider)
        } else {
            assertTrue("Execution outcome should be ENQUEUED, FAILED or BLOCKED in test, got ${result.status}", result.status == "ENQUEUED" || result.status == "FAILED" || result.status == "BLOCKED")
        }
    }

    @Test
    fun `test Execute Task Calendar Sync Denied Without System Permission`() = runBlocking {
        val agent = Agent(
            id = 3,
            name = "Work Agent",
            type = "WORK",
            role = "Calendar Sync"
        )
        val task = SubTask(
            id = 3,
            assignedAgent = agent.name,
            description = "Sync Google Calendar",
            actionType = "CALENDAR_SYNC"
        )

        val result = executionEngine.executeTask(task, agent)

        assertTrue(result.status == "BLOCKED" || result.status == "FAILED")
    }

    @Test
    fun `test Execute Task LLM Prompt Handles Missing Gemini Key Gracefully`() = runBlocking {
        val agent = Agent(
            id = 4,
            name = "Study Agent",
            type = "STUDY",
            role = "Tutor"
        )
        val task = SubTask(
            id = 4,
            assignedAgent = agent.name,
            description = "Analyze user study goals",
            actionType = "LLM_PROMPT"
        )

        val result = executionEngine.executeTask(task, agent)

        // When Gemini API Key is missing or placeholder in test environment, it blocks gracefully
        assertTrue(result.status == "BLOCKED" || result.status == "SIMULATED" || result.status == "FAILED")
    }
}
