package com.example.data

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ColonyDao

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(AppDatabase.MIGRATION_10_11)
            .build()
        dao = db.colonyDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test Agent Role Insertion and Queries`() = runBlocking {
        val agent = Agent(
            name = "Health Guardian",
            type = "HEALTH",
            role = "Monitor vitals and sleep schedule",
            permissions = "Sensors & Notifications"
        )
        dao.insertAgent(agent)

        val agents = dao.getAllAgents().first()
        assertEquals(1, agents.size)
        assertEquals("Health Guardian", agents[0].name)
        assertEquals("HEALTH", agents[0].type)
        assertEquals("Monitor vitals and sleep schedule", agents[0].role)
    }

    @Test
    fun `test Mission and SubTask Cascade Deletion Foreign Key Constraint`() = runBlocking {
        val missionId = dao.insertMission(
            Mission(goal = "Optimize Colony Health & Productivity", status = "Active")
        ).toInt()

        val subTask1 = SubTask(
            missionId = missionId,
            assignedAgent = "Health Guardian",
            description = "Analyze sleep data",
            actionType = "RULE_EVALUATION"
        )
        val subTask2 = SubTask(
            missionId = missionId,
            assignedAgent = "Work Agent",
            description = "Calendar sync",
            actionType = "CALENDAR_SYNC"
        )

        dao.insertSubTask(subTask1)
        dao.insertSubTask(subTask2)

        val initialMissions = dao.getMissions().first()
        assertEquals(1, initialMissions.size)

        val subTasksForMission = dao.getSubTasksForMission(missionId).first()
        assertEquals(2, subTasksForMission.size)

        // Delete parent mission with CASCADE trigger on sub_tasks foreign key
        dao.deleteMission(missionId)

        val remainingMissions = dao.getMissions().first()
        assertEquals(0, remainingMissions.size)

        val remainingSubTasks = dao.getAllSubTasks().first()
        assertEquals(0, remainingSubTasks.size)
    }
}
