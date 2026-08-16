package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun ContributionGridWidget(modifier: Modifier = Modifier) {
    val contributionData = remember {
        List(52 * 7) { Random.nextInt(0, 10) }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Roczna Aktywność Agentów",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Mapa cieplna zrealizowanych zadań (12 miesięcy)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val colorEmpty = MaterialTheme.colorScheme.surfaceContainerHighest
            val colorLow = MaterialTheme.colorScheme.primaryContainer
            val colorMedium = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            val colorHigh = MaterialTheme.colorScheme.primary

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width((52 * 14).dp)
                        .height((7 * 14).dp)
                ) {
                    val boxSize = 10.dp.toPx()
                    val spacing = 4.dp.toPx()
                    
                    for (week in 0 until 52) {
                        for (day in 0 until 7) {
                            val index = week * 7 + day
                            val count = contributionData[index]
                            
                            val color = when {
                                count == 0 -> colorEmpty
                                count in 1..3 -> colorLow
                                count in 4..6 -> colorMedium
                                else -> colorHigh
                            }

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(
                                    x = week * (boxSize + spacing),
                                    y = day * (boxSize + spacing)
                                ),
                                size = Size(boxSize, boxSize),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
