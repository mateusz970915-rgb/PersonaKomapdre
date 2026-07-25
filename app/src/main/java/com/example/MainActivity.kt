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
import com.example.service.AgentRestSchedulerService

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
        
    
    // Check and request Usage Stats permission
    val appOps = getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
    } else {
        appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
    }
    if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
        try {
            startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Start background scheduler service
    try {
        val serviceIntent = Intent(this, AgentRestSchedulerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val viewModel: ColonyViewModel = viewModel()
            val isOnboardingNeeded by viewModel.isOnboardingNeeded.collectAsState()
            
            if (isOnboardingNeeded == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                NavHost(
                    navController = navController, 
                    startDestination = if (isOnboardingNeeded == true) "onboarding" else "dashboard"
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
                            onNavigateToAgentDashboard = { navController.navigate("agent_dashboard") }
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
            }
            }
        }
      }
    }
  }
}
