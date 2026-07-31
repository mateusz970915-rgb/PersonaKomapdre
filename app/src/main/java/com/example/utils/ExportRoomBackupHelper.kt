package com.example.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Agent
import com.example.data.AgentDecision
import com.example.data.AgentKnowledgeEdge
import com.example.data.AgentMeshTelemetry
import com.example.data.AgentNegotiationProposal

import com.example.data.ColonyMemory
import com.example.data.Mission
import com.example.data.MissionStateLog
import com.example.data.SubTask
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ExportRoomBackupHelper {

    fun exportFullRoomBackupToJson(
        context: Context,
        agents: List<Agent>,
        missions: List<Mission>,
        subTasks: List<SubTask>,
        decisions: List<AgentDecision>,
        missionLogs: List<MissionStateLog>,
        memories: List<ColonyMemory>,
        negotiations: List<AgentNegotiationProposal> = emptyList(),
        meshTelemetry: List<AgentMeshTelemetry> = emptyList(),
        knowledgeEdges: List<AgentKnowledgeEdge> = emptyList(),
        onComplete: (filePath: String, jsonString: String) -> Unit

    ) {
        try {
            val root = JSONObject()
            root.put("backupVersion", "1.0")
            root.put("exportTimestamp", System.currentTimeMillis())
            root.put("appName", "PersonaMesh")

            // 1. Agents Data
            val agentsArray = JSONArray()
            agents.forEach { agent ->
                val obj = JSONObject()
                obj.put("id", agent.id)
                obj.put("name", agent.name)
                obj.put("type", agent.type)
                obj.put("role", agent.role)
                obj.put("permissions", agent.permissions)
                obj.put("traits", agent.traits)
                obj.put("systemPrompt", agent.systemPrompt)
                obj.put("status", agent.status)
                obj.put("autonomyLevel", agent.autonomyLevel)
                obj.put("iconName", agent.iconName)
                obj.put("statusNotes", agent.statusNotes)
                agentsArray.put(obj)
            }
            root.put("agents", agentsArray)

            // 2. Missions Data
            val missionsArray = JSONArray()
            missions.forEach { m ->
                val mObj = JSONObject()
                mObj.put("id", m.id)
                mObj.put("goal", m.goal)
                mObj.put("status", m.status)
                mObj.put("timestamp", m.timestamp)
                missionsArray.put(mObj)
            }
            root.put("missions", missionsArray)

            // 3. Subtasks
            val subtasksArray = JSONArray()
            subTasks.forEach { st ->
                val stObj = JSONObject()
                stObj.put("id", st.id)
                stObj.put("missionId", st.missionId)
                stObj.put("assignedAgent", st.assignedAgent)
                stObj.put("description", st.description)
                stObj.put("status", st.status)
                stObj.put("timestamp", st.timestamp)
                stObj.put("completedAt", st.completedAt)
                subtasksArray.put(stObj)
            }
            root.put("subTasks", subtasksArray)

            // 4. Decisions History
            val decisionsArray = JSONArray()
            decisions.forEach { d ->
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("agentName", d.agentName)
                dObj.put("actionDescription", d.actionDescription)
                dObj.put("dataUsed", d.dataUsed)
                dObj.put("confidenceLevel", d.confidenceLevel)
                dObj.put("dissentingOpinions", d.dissentingOpinions)
                dObj.put("timestamp", d.timestamp)
                decisionsArray.put(dObj)
            }
            root.put("decisions", decisionsArray)

            // 5. Mission State Logs
            val logsArray = JSONArray()
            missionLogs.forEach { log ->
                val lObj = JSONObject()
                lObj.put("id", log.id)
                lObj.put("missionId", log.missionId)
                lObj.put("agentName", log.agentName)
                lObj.put("previousState", log.previousState)
                lObj.put("newState", log.newState)
                lObj.put("message", log.message)
                lObj.put("timestamp", log.timestamp)
                logsArray.put(lObj)
            }
            root.put("missionLogs", logsArray)

            // 6. Colony Memories
            val memoriesArray = JSONArray()
            memories.take(100).forEach { mem ->
                val memObj = JSONObject()
                memObj.put("id", mem.id)
                memObj.put("content", mem.content)
                memObj.put("timestamp", mem.timestamp)
                memoriesArray.put(memObj)
            }
            root.put("memories", memoriesArray)

            // 7. Agent Negotiations
            val negotiationsArray = JSONArray()
            negotiations.forEach { neg ->
                val negObj = JSONObject()
                negObj.put("id", neg.id)
                negObj.put("missionId", neg.missionId)
                negObj.put("proposerAgent", neg.proposerAgent)
                negObj.put("targetAgent", neg.targetAgent)
                negObj.put("proposedAction", neg.proposedAction)
                negObj.put("counterProposal", neg.counterProposal)
                negObj.put("status", neg.status)
                negObj.put("conflictTopic", neg.conflictTopic)
                negObj.put("timestamp", neg.timestamp)
                negotiationsArray.put(negObj)
            }
            root.put("negotiations", negotiationsArray)

            // 8. Agent Mesh Telemetry
            val telemetryArray = JSONArray()
            meshTelemetry.forEach { tel ->
                val telObj = JSONObject()
                telObj.put("id", tel.id)
                telObj.put("agentId", tel.agentId)
                telObj.put("agentName", tel.agentName)
                telObj.put("latencyMs", tel.latencyMs)
                telObj.put("cpuLoadPct", tel.cpuLoadPct)
                telObj.put("memoryUsageMb", tel.memoryUsageMb)
                telObj.put("activeConnectionsCount", tel.activeConnectionsCount)
                telObj.put("healthStatus", tel.healthStatus)
                telObj.put("timestamp", tel.timestamp)
                telemetryArray.put(telObj)
            }
            root.put("meshTelemetry", telemetryArray)

            // 9. Knowledge Graph Edges
            val edgesArray = JSONArray()
            knowledgeEdges.forEach { edge ->
                val edgeObj = JSONObject()
                edgeObj.put("id", edge.id)
                edgeObj.put("sourceLabel", edge.sourceLabel)
                edgeObj.put("sourceType", edge.sourceType)
                edgeObj.put("targetLabel", edge.targetLabel)
                edgeObj.put("targetType", edge.targetType)
                edgeObj.put("relationType", edge.relationType)
                edgeObj.put("weight", edge.weight)
                edgeObj.put("creatorAgent", edge.creatorAgent)
                edgeObj.put("timestamp", edge.timestamp)
                edgesArray.put(edgeObj)
            }
            root.put("knowledgeEdges", edgesArray)

            val jsonOutput = root.toString(4)


            val fileName = "colony_full_backup_${System.currentTimeMillis()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonOutput)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "PersonaMesh Full Colony Backup JSON")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Room Backup JSON"))

            Toast.makeText(context, "Exported full backup to JSON!", Toast.LENGTH_SHORT).show()
            onComplete(file.absolutePath, jsonOutput)
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
