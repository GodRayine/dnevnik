package com.nub.diary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF92400E),
    onPrimary = Color.White,
    secondary = Color(0xFFA16207),
    onSecondary = Color.White,
    background = Color(0xFFFFFBEB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8F3F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFD9C9BE),
    error = Color(0xFFDC2626),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFF0B27A),
    onPrimary = Color(0xFF3B1C07),
    secondary = Color(0xFFE5B84B),
    background = Color(0xFF17130F),
    onBackground = Color(0xFFF1E8E2),
    surface = Color(0xFF211B16),
    onSurface = Color(0xFFF1E8E2),
    surfaceVariant = Color(0xFF2B231D),
    onSurfaceVariant = Color(0xFFC9BDB3),
    outline = Color(0xFF574A40),
    error = Color(0xFFF87171),
)

@Composable
fun DiaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        content = content,
    )
}
