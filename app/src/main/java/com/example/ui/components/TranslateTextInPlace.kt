package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.example.data.*
import com.example.viewmodel.ColonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TranslateTextInPlace(
    originalText: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    modifier: Modifier = Modifier
) {
    var isTranslated by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf(originalText) }
    var isTranslating by remember { mutableStateOf(false) }

    LaunchedEffect(originalText) {
        if (!isTranslated) {
            displayedText = originalText
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = displayedText,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        IconButton(
            onClick = {
                if (isTranslated) {
                    displayedText = originalText
                    isTranslated = false
                } else {
                    isTranslating = true
                    com.example.utils.LocalTranslator.translateOffline(originalText) { result, _ ->
                        displayedText = result
                        isTranslated = true
                        isTranslating = false
                    }
                }
            },
            modifier = Modifier
                .size(24.dp)
                .testTag("translate_btn_${originalText.hashCode()}"),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (isTranslated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        ) {
            if (isTranslating) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
            } else {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Translate text offline",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
