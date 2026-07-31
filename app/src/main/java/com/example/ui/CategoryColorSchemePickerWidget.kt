package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.ColonyViewModel

private val PALETTE_COLORS = listOf(
    "#10B981" to "Emerald",
    "#3B82F6" to "Royal Blue",
    "#8B5CF6" to "Violet",
    "#EF4444" to "Crimson",
    "#F59E0B" to "Amber",
    "#06B6D4" to "Cyan",
    "#6366F1" to "Indigo",
    "#EC4899" to "Pink",
    "#14B8A6" to "Teal",
    "#F97316" to "Coral",
    "#64748B" to "Slate"
)

private val CATEGORIES = listOf(
    "HEALTH", "FINANCE", "WORK", "SECURITY", "CREATIVE", "ANALYTICS", "GOVERNANCE", "GENERAL"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryColorSchemePickerWidget(
    viewModel: ColonyViewModel,
    modifier: Modifier = Modifier
) {
    val categoryColors by viewModel.categoryColors.collectAsState()
    var selectedCategory by remember { mutableStateOf("HEALTH") }

    val activeCategoryColorHex = remember(selectedCategory, categoryColors) {
        categoryColors[selectedCategory.uppercase()] ?: viewModel.getCategoryColorHex(selectedCategory)
    }

    val parsedCategoryColor = remember(activeCategoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(activeCategoryColorHex))
        } catch (e: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, parsedCategoryColor.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_color_scheme_picker_widget")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(parsedCategoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Scheme Picker",
                            tint = parsedCategoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dashboard Color Scheme Picker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Assign unique visual colors to AI Agent Categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = parsedCategoryColor
                ) {
                    Text(
                        text = selectedCategory,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips Row
            Text(
                text = "Select Category:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CATEGORIES) { cat ->
                    val catHex = categoryColors[cat.uppercase()] ?: viewModel.getCategoryColorHex(cat)
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(catHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }

                    FilterChip(
                        selected = selectedCategory.equals(cat, ignoreCase = true),
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                        },
                        modifier = Modifier.testTag("picker_cat_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pick UI Color for '$selectedCategory':",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PALETTE_COLORS.forEach { (hex, name) ->
                    val parsed = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    val isSelected = activeCategoryColorHex.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(parsed)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.updateCategoryColor(selectedCategory, hex)
                            }
                            .testTag("picker_color_dot_$hex"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
