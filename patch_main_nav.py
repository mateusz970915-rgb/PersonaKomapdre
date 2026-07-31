import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_call = """                            onNavigateToWebAnalyzer = { navController.navigate("web_analyzer") },
                            onNavigateToKnowledgeGraph = { navController.navigate("knowledge_graph") },
                            onNavigateToAgentBuilder = { navController.navigate("agent_builder") },
                            onNavigateToStudy = { navController.navigate("study") },
                            onNavigateToSleepOptimizer = { navController.navigate("sleep_recovery_optimizer") },
                            onNavigateToPhase5 = { navController.navigate("phase5_evolution") }
                        )
                    }"""

new_call = """                            onNavigateToWebAnalyzer = { navController.navigate("web_analyzer") },
                            onNavigateToKnowledgeGraph = { navController.navigate("knowledge_graph") },
                            onNavigateToAgentBuilder = { navController.navigate("agent_builder") },
                            onNavigateToStudy = { navController.navigate("study") },
                            onNavigateToSleepOptimizer = { navController.navigate("sleep_recovery_optimizer") },
                            onNavigateToPhase5 = { navController.navigate("phase5_evolution") },
                            onNavigateToAddAgent = { navController.navigate("add_agent") }
                        )
                    }"""

content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
