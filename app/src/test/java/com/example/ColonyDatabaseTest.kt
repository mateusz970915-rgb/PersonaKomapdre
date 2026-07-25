package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ColonyMemory
import com.example.data.RuleConnectionEntity
import com.example.data.RuleNodeEntity
import com.example.data.SubTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ColonyDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `test Rule Nodes and Connections Persistence`() = runBlocking {
        val dao = db.colonyDao()
        
        val node1 = RuleNodeEntity("node_test_1", 100f, 200f, "TRIGGER", "Battery < 15%")
        val node2 = RuleNodeEntity("node_test_2", 500f, 200f, "ACTION", "Enable Power Saver Agent")
        dao.insertRuleNode(node1)
        dao.insertRuleNode(node2)
        
        val connection = RuleConnectionEntity(fromId = "node_test_1", toId = "node_test_2")
        dao.insertRuleConnection(connection)

        val savedNodes = dao.getAllRuleNodes().first()
        val savedConnections = dao.getAllRuleConnections().first()

        assertEquals(2, savedNodes.size)
        assertEquals(1, savedConnections.size)
        assertEquals("Battery < 15%", savedNodes.find { it.id == "node_test_1" }?.text)
        assertEquals("node_test_1", savedConnections.first().fromId)
    }

    @Test
    fun `test Colony Memory Persistence`() = runBlocking {
        val dao = db.colonyDao()
        val memory = ColonyMemory(content = "User prefers focus mode during work hours")
        dao.insertMemory(memory)

        val memories = dao.getMemories().first()
        assertEquals(1, memories.size)
        assertEquals("User prefers focus mode during work hours", memories.first().content)
    }

    @Test
    fun `test Completed SubTasks Aggregation`() = runBlocking {
        val dao = db.colonyDao()
        val missionId = dao.insertMission(com.example.data.Mission(id = 1, goal = "Test Mission")).toInt()
        dao.insertSubTask(SubTask(missionId = missionId, assignedAgent = "Work Agent", description = "Review code", status = "Completed"))
        dao.insertSubTask(SubTask(missionId = missionId, assignedAgent = "Health Agent", description = "Hydrate", status = "Pending"))

        val completed = dao.getCompletedSubTasks().first()
        assertEquals(1, completed.size)
        assertEquals("Review code", completed.first().description)
    }

    @Test
    fun `test Mission Cascade Delete Removes Associated SubTasks`() = runBlocking {
        val dao = db.colonyDao()
        val missionId = dao.insertMission(com.example.data.Mission(id = 10, goal = "Temporary Mission")).toInt()
        dao.insertSubTask(SubTask(missionId = missionId, assignedAgent = "Agent 1", description = "Task A"))
        dao.insertSubTask(SubTask(missionId = missionId, assignedAgent = "Agent 2", description = "Task B"))

        val beforeDelete = dao.getSubTasksForMission(missionId).first()
        assertEquals(2, beforeDelete.size)

        dao.deleteMission(missionId)

        val afterDelete = dao.getSubTasksForMission(missionId).first()
        assertEquals(0, afterDelete.size)
    }
}
