package com.nub.diary.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nub.diary.ThemeMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nub.diary.R

// Обе палитры полные: в диалогах и состояниях нет стандартных фиолетовых контейнеров.
private val Light = lightColorScheme(
    primary = Color(0xFF92400E), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF6E2CF), onPrimaryContainer = Color(0xFF652B0B),
    secondary = Color(0xFF5D6248), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8EAD9), onSecondaryContainer = Color(0xFF41462F),
    tertiary = Color(0xFF725444), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF1E1D8), onTertiaryContainer = Color(0xFF51392D),
    background = Color(0xFFFAF5EB), onBackground = Color(0xFF302820),
    surface = Color(0xFFFFFCF6), onSurface = Color(0xFF302820),
    surfaceVariant = Color(0xFFF0E8DC), onSurfaceVariant = Color(0xFF66584B),
    surfaceDim = Color(0xFFE5DCCF), surfaceBright = Color(0xFFFFFCF6),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFFFF9F0),
    surfaceContainer = Color(0xFFF5EEE3), surfaceContainerHigh = Color(0xFFF0E8DC),
    surfaceContainerHighest = Color(0xFFEAE0D3),
    outline = Color(0xFF8A7766), outlineVariant = Color(0xFFD8CBBB),
    error = Color(0xFFA12828), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF7A1B1B),
    inverseSurface = Color(0xFF352D26), inverseOnSurface = Color(0xFFFAF0E5),
    inversePrimary = Color(0xFFF0B27A), surfaceTint = Color(0xFF92400E),
    scrim = Color(0xFF000000),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFF0B27A), onPrimary = Color(0xFF3B1C07),
    primaryContainer = Color(0xFF57351F), onPrimaryContainer = Color(0xFFFFDCBE),
    secondary = Color(0xFFC5CBA9), onSecondary = Color(0xFF2B321D),
    secondaryContainer = Color(0xFF383F2C), onSecondaryContainer = Color(0xFFE1E6C7),
    tertiary = Color(0xFFE3BBA7), onTertiary = Color(0xFF412A20),
    tertiaryContainer = Color(0xFF554034), onTertiaryContainer = Color(0xFFFFDBCA),
    background = Color(0xFF191510), onBackground = Color(0xFFF2E7D9),
    surface = Color(0xFF241E18), onSurface = Color(0xFFF2E7D9),
    surfaceVariant = Color(0xFF342B23), onSurfaceVariant = Color(0xFFCDBFAF),
    surfaceDim = Color(0xFF191510), surfaceBright = Color(0xFF40362C),
    surfaceContainerLowest = Color(0xFF130F0C), surfaceContainerLow = Color(0xFF211B16),
    surfaceContainer = Color(0xFF29221B), surfaceContainerHigh = Color(0xFF342B23),
    surfaceContainerHighest = Color(0xFF40362C),
    outline = Color(0xFFA4917D), outlineVariant = Color(0xFF5C4D3E),
    error = Color(0xFFFFB4AB), onError = Color(0xFF601414),
    errorContainer = Color(0xFF702323), onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFF2E7D9), inverseOnSurface = Color(0xFF352D26),
    inversePrimary = Color(0xFF92400E), surfaceTint = Color(0xFFF0B27A),
    scrim = Color(0xFF000000),
)

private val Sans = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_semibold, FontWeight.Medium),
    Font(R.font.noto_sans_semibold, FontWeight.SemiBold),
)
private val Serif = FontFamily(Font(R.font.noto_serif_regular))

private val DiaryTypography = Typography(
    displayLarge = TextStyle(fontFamily = Serif, fontSize = 44.sp, lineHeight = 52.sp),
    displayMedium = TextStyle(fontFamily = Serif, fontSize = 40.sp, lineHeight = 48.sp),
    displaySmall = TextStyle(fontFamily = Serif, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = Serif, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontSize = 22.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
)

@Composable
fun DiaryTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    // Значки в строке состояния и панели навигации должны читаться на выбранном фоне.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = DiaryTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
