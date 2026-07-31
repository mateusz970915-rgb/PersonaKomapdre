import re

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_block = """                                }
                            },
                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) }
                        )
                    }
                }
            },
                    onTap = {
                        if (isMultiSelectMode) {
                            if (isSelected) {
                                selectedAgents.remove(agent)
                            } else {
                                selectedAgents.add(agent)
                            }
                        } else {
                            selectedAgentForActivity = agent
                        }
                    },
                    onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                    onDelete = { viewModel.deleteAgent(agent.id) }
                )
            }
        }
        }
        }"""

good_block = """                                }
                            },
                            onPauseToggle = { viewModel.toggleAgentStatus(agent) },
                            onDelete = { viewModel.deleteAgent(agent.id) }
                        )
                    }
                }
            }
        }
        }"""

content = content.replace(bad_block, good_block)

with open("app/src/main/java/com/example/ui/DashboardScreen.kt", "w") as f:
    f.write(content)
