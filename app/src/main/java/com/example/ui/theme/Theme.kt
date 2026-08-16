package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

// ZEN (Pastel sage green & tranquil teal)
private val ZenLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF00796B),
    tertiary = Color(0xFF689F38),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White
)
private val ZenDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFFAED581),
    background = Color(0xFF002F13),
    surface = Color(0xFF121212)
)

// CREATIVE (Sunset & coral)
private val CreativeLightColors = lightColorScheme(
    primary = Color(0xFFE64A19),
    secondary = Color(0xFF8E24AA),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFFFBE9E7),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White
)
private val CreativeDarkColors = darkColorScheme(
    primary = Color(0xFFFF8A65),
    secondary = Color(0xFFBA68C8),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF2E0E00),
    surface = Color(0xFF1E1E1E)
)

// VIGILANT (Security high contrast red/black)
private val VigilantLightColors = lightColorScheme(
    primary = Color(0xFFD32F2F),
    secondary = Color(0xFF37474F),
    tertiary = Color(0xFF1976D2),
    background = Color(0xFFECEFF1),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White
)
private val VigilantDarkColors = darkColorScheme(
    primary = Color(0xFFEF5350),
    secondary = Color(0xFF90A4AE),
    tertiary = Color(0xFF64B5F6),
    background = Color(0xFF1F1F1F),
    surface = Color(0xFF121212)
)

// DEEP FOCUS (Slate minimal)
private val DeepFocusLightColors = lightColorScheme(
    primary = Color(0xFF1A237E),
    secondary = Color(0xFF455A64),
    tertiary = Color(0xFF0D47A1),
    background = Color(0xFFECEFF1),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White
)
private val DeepFocusDarkColors = darkColorScheme(
    primary = Color(0xFF5C6BC0),
    secondary = Color(0xFF78909C),
    tertiary = Color(0xFF42A5F5),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  themeMode: String = "Default",
  dominantMood: String? = null,
  content: @Composable () -> Unit,
) {
  val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  val isNight = hour < 6 || hour >= 18

  val targetColorScheme: ColorScheme = when (themeMode) {
      "Zen" -> if (darkTheme) ZenDarkColors else ZenLightColors
      "Creative" -> if (darkTheme) CreativeDarkColors else CreativeLightColors
      "Vigilant" -> if (darkTheme) VigilantDarkColors else VigilantLightColors
      "DeepFocus" -> if (darkTheme) DeepFocusDarkColors else DeepFocusLightColors
      "TimeOfDay" -> {
          if (isNight) {
              darkColorScheme(
                  primary = Color(0xFF9FA8DA),
                  secondary = Color(0xFF80DEEA),
                  tertiary = Color(0xFFB39DDB),
                  background = Color(0xFF0D0E15),
                  surface = Color(0xFF12131F)
              )
          } else {
              lightColorScheme(
                  primary = Color(0xFFFF6F00),
                  secondary = Color(0xFF00B0FF),
                  tertiary = Color(0xFF4CAF50),
                  background = Color(0xFFFFFDE7),
                  surface = Color(0xFFFFFFFF)
              )
          }
      }
      "AgentMood" -> {
          when (dominantMood) {
              "🧘", "Zen & Serene" -> if (darkTheme) ZenDarkColors else ZenLightColors
              "🎨", "Creative Flow" -> if (darkTheme) CreativeDarkColors else CreativeLightColors
              "🛡️", "Vigilant" -> if (darkTheme) VigilantDarkColors else VigilantLightColors
              "🤯", "High Workload", "🔥", "Overloaded" -> if (darkTheme) VigilantDarkColors else VigilantLightColors
              "😴", "Resting / Standby" -> if (darkTheme) DeepFocusDarkColors else DeepFocusLightColors
              else -> {
                  if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                      val context = LocalContext.current
                      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                  } else {
                      if (darkTheme) DarkColorScheme else LightColorScheme
                  }
              }
          }
      }
      else -> {
          if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              val context = LocalContext.current
              if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
          } else {
              if (darkTheme) DarkColorScheme else LightColorScheme
          }
      }
  }

  val primary by animateColorAsState(targetValue = targetColorScheme.primary, animationSpec = tween(500), label = "primary")
  val onPrimary by animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = tween(500), label = "onPrimary")
  val primaryContainer by animateColorAsState(targetValue = targetColorScheme.primaryContainer, animationSpec = tween(500), label = "primaryContainer")
  val onPrimaryContainer by animateColorAsState(targetValue = targetColorScheme.onPrimaryContainer, animationSpec = tween(500), label = "onPrimaryContainer")
  val secondary by animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = tween(500), label = "secondary")
  val onSecondary by animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = tween(500), label = "onSecondary")
  val secondaryContainer by animateColorAsState(targetValue = targetColorScheme.secondaryContainer, animationSpec = tween(500), label = "secondaryContainer")
  val onSecondaryContainer by animateColorAsState(targetValue = targetColorScheme.onSecondaryContainer, animationSpec = tween(500), label = "onSecondaryContainer")
  val tertiary by animateColorAsState(targetValue = targetColorScheme.tertiary, animationSpec = tween(500), label = "tertiary")
  val onTertiary by animateColorAsState(targetValue = targetColorScheme.onTertiary, animationSpec = tween(500), label = "onTertiary")
  val tertiaryContainer by animateColorAsState(targetValue = targetColorScheme.tertiaryContainer, animationSpec = tween(500), label = "tertiaryContainer")
  val onTertiaryContainer by animateColorAsState(targetValue = targetColorScheme.onTertiaryContainer, animationSpec = tween(500), label = "onTertiaryContainer")
  val error by animateColorAsState(targetValue = targetColorScheme.error, animationSpec = tween(500), label = "error")
  val onError by animateColorAsState(targetValue = targetColorScheme.onError, animationSpec = tween(500), label = "onError")
  val errorContainer by animateColorAsState(targetValue = targetColorScheme.errorContainer, animationSpec = tween(500), label = "errorContainer")
  val onErrorContainer by animateColorAsState(targetValue = targetColorScheme.onErrorContainer, animationSpec = tween(500), label = "onErrorContainer")
  val background by animateColorAsState(targetValue = targetColorScheme.background, animationSpec = tween(500), label = "background")
  val onBackground by animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = tween(500), label = "onBackground")
  val surface by animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = tween(500), label = "surface")
  val onSurface by animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = tween(500), label = "onSurface")
  val surfaceVariant by animateColorAsState(targetValue = targetColorScheme.surfaceVariant, animationSpec = tween(500), label = "surfaceVariant")
  val onSurfaceVariant by animateColorAsState(targetValue = targetColorScheme.onSurfaceVariant, animationSpec = tween(500), label = "onSurfaceVariant")
  val outline by animateColorAsState(targetValue = targetColorScheme.outline, animationSpec = tween(500), label = "outline")
  val outlineVariant by animateColorAsState(targetValue = targetColorScheme.outlineVariant, animationSpec = tween(500), label = "outlineVariant")
  val scrim by animateColorAsState(targetValue = targetColorScheme.scrim, animationSpec = tween(500), label = "scrim")
  val inverseSurface by animateColorAsState(targetValue = targetColorScheme.inverseSurface, animationSpec = tween(500), label = "inverseSurface")
  val inverseOnSurface by animateColorAsState(targetValue = targetColorScheme.inverseOnSurface, animationSpec = tween(500), label = "inverseOnSurface")
  val inversePrimary by animateColorAsState(targetValue = targetColorScheme.inversePrimary, animationSpec = tween(500), label = "inversePrimary")

  val animatedColorScheme = ColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      secondary = secondary,
      onSecondary = onSecondary,
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = onSecondaryContainer,
      tertiary = tertiary,
      onTertiary = onTertiary,
      tertiaryContainer = tertiaryContainer,
      onTertiaryContainer = onTertiaryContainer,
      error = error,
      onError = onError,
      errorContainer = errorContainer,
      onErrorContainer = onErrorContainer,
      background = background,
      onBackground = onBackground,
      surface = surface,
      onSurface = onSurface,
      surfaceVariant = surfaceVariant,
      onSurfaceVariant = onSurfaceVariant,
      outline = outline,
      outlineVariant = outlineVariant,
      scrim = scrim,
      inverseSurface = inverseSurface,
      inverseOnSurface = inverseOnSurface,
      inversePrimary = inversePrimary,
      surfaceTint = primary,
      surfaceBright = targetColorScheme.surfaceBright,
      surfaceDim = targetColorScheme.surfaceDim,
      surfaceContainer = targetColorScheme.surfaceContainer,
      surfaceContainerHigh = targetColorScheme.surfaceContainerHigh,
      surfaceContainerHighest = targetColorScheme.surfaceContainerHighest,
      surfaceContainerLow = targetColorScheme.surfaceContainerLow,
      surfaceContainerLowest = targetColorScheme.surfaceContainerLowest
  )

  MaterialTheme(colorScheme = animatedColorScheme, typography = Typography, content = content)
}
