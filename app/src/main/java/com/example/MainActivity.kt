package com.example

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.RuleEvaluatorWorker
import com.example.worker.DailySummaryWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.navDeepLink
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.example.ui.CouncilChatScreen
import com.example.viewmodel.ChatViewModel
import com.example.ui.DashboardScreen
import com.example.ui.MissionsScreen
import com.example.ui.DecisionPanelScreen
import com.example.ui.CouncilScreen
import com.example.ui.AgentEfficiencyScreen
import com.example.ui.BadgesScreen
import com.example.ui.PrivacyDiagnosticScreen
import com.example.ui.OnboardingScreen
import com.example.ui.RuleEditorScreen
import com.example.ui.SuggestedAgentsScreen
import com.example.ui.SettingsScreen
import com.example.ui.InterAgentChatScreen
import com.example.ui.TaskBoardScreen
import com.example.ui.ActiveAgentsScreen
import com.example.ui.ColonyProgressionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ColonyViewModel

import android.content.Intent
import android.os.Build

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Request runtime permissions
    val permissions = mutableListOf(
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.READ_CONTACTS
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }
    // Permissions requested on demand, 101)

        val dailySummaryRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailySummaryWork",
            ExistingPeriodicWorkPolicy.KEEP,
            dailySummaryRequest
        )
        
    
    // Usage stats mode check without forced system activity redirect on startup
    val appOps = getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
    } else {
        appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
    }


    // Start background scheduler worker
    try {
        val restSchedulerRequest = PeriodicWorkRequestBuilder<com.example.worker.AgentRestSchedulerWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AgentRestSchedulerWork",
            ExistingPeriodicWorkPolicy.KEEP,
            restSchedulerRequest
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
        val viewModel: ColonyViewModel = viewModel()
        val preferences by viewModel.agentPreferencesState.collectAsState()
        val agents by viewModel.agents.collectAsState()
        val subTasks by viewModel.subTasks.collectAsState()
        
        val dominantMood = androidx.compose.runtime.remember(agents, subTasks) {
            val activeAgent = agents.firstOrNull { it.status == "Active" } ?: agents.firstOrNull()
            if (activeAgent != null) {
                com.example.data.calculateAgentMood(activeAgent, subTasks).moodTitle
            } else null
        }

        MyApplicationTheme(
            themeMode = preferences.themeMode,
            dominantMood = dominantMood
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                val isOnboardingNeeded by viewModel.isOnboardingNeeded.collectAsState()
            
            // Handle shared intent
            androidx.compose.runtime.LaunchedEffect(intent) {
                if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                        viewModel.setSharedWebText(sharedText)
                        navController.navigate("edde_console")
                    }
                }
            }
            
            if (isOnboardingNeeded == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                NavHost(
                    navController = navController, 
                    startDestination = if (isOnboardingNeeded == true) "onboarding" else "dashboard",
                    enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)) },
                    exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(animationSpec = tween(300)) },
                    popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(animationSpec = tween(300)) },
                    popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(300)) }
                ) {
                    composable("onboarding") {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onOnboardingComplete = { 
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        "dashboard",
                        deepLinks = listOf(navDeepLink { uriPattern = "colony://dashboard" })
                    ) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToChat = { navController.navigate("chat") },
                            onNavigateToMissions = { navController.navigate("missions") },
                            onNavigateToDecisions = { navController.navigate("decisions") },
                            onNavigateToCouncil = { navController.navigate("council") },
                            onNavigateToEfficiency = { navController.navigate("efficiency") },
                            onNavigateToPrivacy = { navController.navigate("privacy") },
                            onNavigateToBehaviors = { navController.navigate("behaviors") },
                            onNavigateToMarket = { navController.navigate("market") },
                            onNavigateToSuggested = { navController.navigate("suggested") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToBadges = { navController.navigate("badges") },
                            onNavigateToInterAgentChat = { navController.navigate("inter_agent_chat") },
                            onNavigateToTaskBoard = { navController.navigate("task_board") },
                            onNavigateToProgression = { navController.navigate("progression") },
                            onNavigateToPersonaColony = { navController.navigate("persona_colony") },
                            onNavigateToActiveAgents = { navController.navigate("active_agents") },
                            onNavigateToAgentDashboard = { navController.navigate("agent_dashboard") },
                            onNavigateToEvolution = { navController.navigate("evolution") },
                            onNavigateToEddeConsole = { navController.navigate("edde_console") },
                            onNavigateToSmartFinance = { navController.navigate("smart_finance") },
                            onNavigateToCalendarIntel = { navController.navigate("calendar_intel") },
                            onNavigateToWebAnalyzer = { navController.navigate("web_analyzer") },
                            onNavigateToKnowledgeGraph = { navController.navigate("knowledge_graph") },
                            onNavigateToAgentBuilder = { navController.navigate("agent_builder") },
                            onNavigateToStudy = { navController.navigate("study") },
                            onNavigateToSleepOptimizer = { navController.navigate("sleep_recovery_optimizer") },
                            onNavigateToPhase5 = { navController.navigate("phase5_evolution") },
                            onNavigateToAddAgent = { navController.navigate("add_agent") }
                        )
                    }
                    
                    composable("agent_dashboard") {
                        com.example.ui.AgentDashboardScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("add_agent") {
                        com.example.ui.AddAgentScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("active_agents") {
                        ActiveAgentsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToTaskBoard = { navController.navigate("task_board") },
                            onNavigateToMissions = { navController.navigate("missions") }
                        )
                    }
                    composable("persona_colony") {
                        com.example.ui.PersonaColonyScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("inter_agent_chat") {
                        InterAgentChatScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("task_board") {
                        TaskBoardScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("progression") {
                        ColonyProgressionScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("badges") {
                        BadgesScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("suggested") {
                        SuggestedAgentsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                composable("chat") {
                    val chatViewModel: ChatViewModel = viewModel()
                    CouncilChatScreen(
                        chatViewModel = chatViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("missions") {
                    MissionsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("decisions") {
                    DecisionPanelScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("council") {
                    CouncilScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToChat = { navController.navigate("chat") }
                    )
                }
                composable("efficiency") {
                    AgentEfficiencyScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("privacy") {
                    PrivacyDiagnosticScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("behaviors") {
                    RuleEditorScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("market") {
                    com.example.ui.ImportAgentScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToDashboard = { navController.popBackStack() }
                    )
                }
                composable("evolution") {
                    com.example.ui.AgentSelfEvolutionScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("edde_console") {
                    com.example.ui.EddeConsoleScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("smart_finance") {
                    com.example.ui.FinanceAgentScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("calendar_intel") {
                    com.example.ui.CalendarIntelScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("web_analyzer") {
                    com.example.ui.WebContentAnalyzerScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("knowledge_graph") {
                    com.example.ui.KnowledgeGraphScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("agent_builder") {
                    com.example.ui.AgentBuilderScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("study") {
                    com.example.ui.StudyScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("sleep_recovery_optimizer") {
                    com.example.ui.SleepRecoveryOptimizerScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("phase5_evolution") {
                    com.example.ui.Phase5EvolutionScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            }
        }
      }
    }
  }
}
