package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.compose.runtime.Composable
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import androidx.glance.layout.Alignment
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import android.content.Intent
import android.content.ComponentName

class ColonyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.colonyDao()
        
        val agents = dao.getAllAgents().first()
        val activeAgents = agents.count { it.status == "Active" || it.status == "Busy" }
        
        val subTasks = dao.getAllSubTasks().first()
        val totalTasks = subTasks.size
        val completedTasks = subTasks.count { it.status == "Completed" }
        val completionPercentage = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
        
        val messages = dao.getCouncilMessages().first()
        val lastMessage = messages.lastOrNull { it.role == "system" }?.content ?: "No critical alerts."
        val missions = dao.getMissions().first().filter { it.status == "Active" }
        val activeMissionStr = missions.firstOrNull()?.let { "${it.goal}: ${it.status}" } ?: "No active missions"

        provideContent {
            WidgetContent(lastMessage = lastMessage, activeMissionStr = activeMissionStr, activeAgents = activeAgents, completionPercentage = completionPercentage)
        }
    }

    @Composable
    private fun WidgetContent(lastMessage: String, activeMissionStr: String, activeAgents: Int, completionPercentage: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFE3E2E6))
                .clickable(actionStartActivity(Intent().apply { component = ComponentName("com.aistudio.personamesh.jshkpq", "com.example.MainActivity") }))
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Colony Status",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Active Agents: $activeAgents",
                style = TextStyle(fontSize = 14.sp),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Tasks Completed: $completionPercentage%",
                style = TextStyle(fontSize = 14.sp),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Mission: $activeMissionStr",
                style = TextStyle(fontSize = 14.sp),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Alert: $lastMessage",
                style = TextStyle(
                    fontSize = 14.sp, 
                    color = ColorProvider(Color(0xFFB3261E)) // Material Red Error Color
                )
            )
        }
    }
}

class ColonyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ColonyWidget()
}
