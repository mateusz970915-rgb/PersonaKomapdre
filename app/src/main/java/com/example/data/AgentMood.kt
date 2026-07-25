package com.example.data

data class AgentMoodInfo(
    val emoji: String,
    val moodTitle: String,
    val description: String,
    val loadScore: Int,
    val complexityScore: Int
)

fun calculateAgentMood(
    agent: Agent,
    assignedTasks: List<SubTask> = emptyList()
): AgentMoodInfo {
    val activeTasks = assignedTasks.filter { 
        it.assignedAgent.equals(agent.name, ignoreCase = true) && it.status != "Completed" 
    }
    val taskCount = activeTasks.size
    
    var totalComplexity = 0
    activeTasks.forEach { task ->
        var taskComp = 15
        val desc = task.description.lowercase()
        if (desc.contains("analyze") || desc.contains("multi-agent") || desc.contains("coordinate")) taskComp += 25
        if (desc.contains("security") || desc.contains("policy") || desc.contains("optimize")) taskComp += 20
        if (desc.length > 35) taskComp += 15
        totalComplexity += taskComp
    }
    
    val loadScore = (taskCount * 25).coerceAtMost(100)
    val complexityScore = totalComplexity.coerceAtMost(100)
    val combinedLoad = (loadScore + complexityScore) / 2
    
    val isPausedOrHalted = agent.status.equals("Paused", ignoreCase = true) || 
            agent.status.equals("Halted", ignoreCase = true)
    
    if (isPausedOrHalted) {
        return AgentMoodInfo(
            emoji = "😴",
            moodTitle = "Resting / Standby",
            description = "Agent is currently paused or on standby.",
            loadScore = 0,
            complexityScore = 0
        )
    }
    
    val roleLower = (agent.role + " " + agent.name + " " + agent.type).lowercase()
    
    return when {
        taskCount == 0 -> {
            when {
                roleLower.contains("health") || roleLower.contains("wellness") -> 
                    AgentMoodInfo("🧘", "Zen & Serene", "No active tasks. Maintaining colony health balance.", 0, 0)
                roleLower.contains("security") || roleLower.contains("privacy") -> 
                    AgentMoodInfo("🛡️", "Vigilant", "Monitoring colony boundaries and privacy policies.", 5, 10)
                roleLower.contains("finance") || roleLower.contains("budget") || roleLower.contains("wealth") -> 
                    AgentMoodInfo("📊", "Calculating", "Analyzing colony resource allocations & yield.", 5, 10)
                roleLower.contains("study") || roleLower.contains("research") -> 
                    AgentMoodInfo("📚", "Thoughtful", "Reviewing colony memory logs & knowledge base.", 5, 10)
                else -> 
                    AgentMoodInfo("😌", "Chill & Ready", "Idle with zero pending tasks.", 0, 0)
            }
        }
        taskCount in 1..2 && combinedLoad < 55 -> {
            when {
                roleLower.contains("executive") || roleLower.contains("work") || roleLower.contains("leader") -> 
                    AgentMoodInfo("🚀", "Ambitious & Driven", "Executing $taskCount active strategic subtask(s).", loadScore, complexityScore)
                roleLower.contains("creator") || roleLower.contains("art") || roleLower.contains("design") -> 
                    AgentMoodInfo("🎨", "Creative Flow", "Crafting innovative solutions in steady flow state.", loadScore, complexityScore)
                else -> 
                    AgentMoodInfo("⚡", "In Flow State", "Handling $taskCount active task(s) with focus.", loadScore, complexityScore)
            }
        }
        taskCount in 3..4 || combinedLoad in 55..80 -> {
            AgentMoodInfo(
                emoji = "🤯",
                moodTitle = "High Workload",
                description = "Balancing $taskCount active tasks under high complexity.",
                loadScore = loadScore,
                complexityScore = complexityScore
            )
        }
        else -> {
            AgentMoodInfo(
                emoji = "🔥",
                moodTitle = "Overloaded",
                description = "Critical workload! $taskCount heavy tasks in active execution.",
                loadScore = loadScore,
                complexityScore = complexityScore
            )
        }
    }
}
