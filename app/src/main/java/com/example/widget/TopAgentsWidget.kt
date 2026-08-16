package com.example.widget

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first

class TopAgentsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.colonyDao()
        
        // Fetch all agents and their interactions to find the most active
        val agents = dao.getAllAgents().first()
        // Wait, InteractionRecord is not in ColonyDao or AppDatabase. It's in AgentInteractionLogger which uses DataStore.
        // We might just show the first 3 agents or agents sorted by something else if we can't get interaction counts easily.
        // Or we can just get the top 3 by some logic.
        // For now, let's just show up to 3 Active agents.
        
        val topAgents = agents.filter { it.status == "Active" }.take(3).ifEmpty { agents.take(3) }

        provideContent {
            WidgetContent(topAgents)
        }
    }

    @Composable
    private fun WidgetContent(topAgents: List<com.example.data.Agent>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFE3E2E6)))
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Top Persona Agents",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = GlanceModifier.padding(bottom = 12.dp)
            )

            if (topAgents.isEmpty()) {
                Text(
                    text = "No agents available.",
                    style = TextStyle(fontSize = 14.sp)
                )
            } else {
                topAgents.forEach { agent ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = agent.name,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        
                        Text(
                            text = "TRIGGER",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFF6750A4))
                            ),
                            modifier = GlanceModifier
                                .clickable(actionStartActivity(Intent().apply {
                                    component = ComponentName("com.aistudio.personamesh.jshkpq", "com.example.MainActivity")
                                    putExtra("trigger_agent_id", agent.id)
                                }))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .background(ColorProvider(Color(0xFFEADDFF)))
                        )
                    }
                }
            }
        }
    }
}

class TopAgentsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TopAgentsWidget()
}
