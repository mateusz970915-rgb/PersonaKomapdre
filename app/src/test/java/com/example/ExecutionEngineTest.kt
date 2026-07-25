package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Agent
import com.example.data.ExecutionOutcome
import com.example.data.SubTask
import com.example.engine.ExecutionEngine
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

    private lateinit var context: Context
    private lateinit var executionEngine: ExecutionEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executionEngine = ExecutionEngine(context)
    }

    @Test
    fun `test Calendar Sync Task Produces Real Executed Outcome When Permission Granted`() = runBlocking {
        val shadowApp = org.robolectric.Shadows.shadowOf(context as android.app.Application)
        shadowApp.grantPermissions(android.Manifest.permission.READ_CALENDAR)

        val agent = Agent(name = "Calendar Manager", type = "Work", role = "Calendar Sync", status = "Active")
        val task = SubTask(missionId = null, assignedAgent = agent.name, description = "Sync calendar", actionType = "CALENDAR_SYNC")

        val result = executionEngine.executeTask(task, agent)

        assertEquals("EXECUTED", result.status)
        assertTrue("Execution outcome must be Executed", result.outcome is ExecutionOutcome.Executed)
    }

    @Test
    fun `test Calendar Sync Task Blocked When Permission Denied`() = runBlocking {
        val agent = Agent(name = "Calendar Manager", type = "Work", role = "Calendar Sync", status = "Active")
        val task = SubTask(missionId = null, assignedAgent = agent.name, description = "Sync calendar", actionType = "CALENDAR_SYNC")

        val result = executionEngine.executeTask(task, agent)

        assertEquals("BLOCKED", result.status)
        assertTrue("Execution outcome must be Blocked", result.outcome is ExecutionOutcome.Blocked)
    }

    @Test
    fun `test Rule Evaluation Task Produces Real Executed Outcome`() = runBlocking {
        val agent = Agent(name = "Rule Manager", type = "Automation", role = "Rule Evaluator", status = "Active")
        val task = SubTask(missionId = null, assignedAgent = agent.name, description = "Evaluate rules", actionType = "RULE_EVALUATION")

        val result = executionEngine.executeTask(task, agent)

        assertEquals("EXECUTED", result.status)
        assertTrue("Execution outcome must be Executed", result.outcome is ExecutionOutcome.Executed)
    }

    @Test
    fun `test LLM Task Produces Simulated Outcome or Blocked if No API Key`() = runBlocking {
        val agent = Agent(name = "Creative Agent", type = "Creative", role = "Ideation", status = "Active")
        val task = SubTask(missionId = null, assignedAgent = agent.name, description = "Brainstorm ideas", actionType = "LLM_PROMPT")

        val result = executionEngine.executeTask(task, agent)

        // Verifies distinct status boundary (Simulation != Execution)
        assertTrue(
            "Status must be SIMULATED or BLOCKED, never EXECUTED for plain LLM prompts",
            result.status == "SIMULATED" || result.status == "BLOCKED"
        )
        assertTrue(
            "Outcome must be Simulated or Blocked",
            result.outcome is ExecutionOutcome.Simulated || result.outcome is ExecutionOutcome.Blocked
        )
    }

    @Test
    fun `test Missing Agent Produces Blocked Outcome`() = runBlocking {
        val task = SubTask(missionId = null, assignedAgent = "Ghost Agent", description = "Unknown task")

        val result = executionEngine.executeTask(task, null)

        assertEquals("BLOCKED", result.status)
        assertTrue("Outcome must be Blocked", result.outcome is ExecutionOutcome.Blocked)
    }
}
