package com.example.libretadirecciones.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta oscura fija (independiente del tema del sistema): fondo nunca blanco.
private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF9ACFA0),
    onPrimary = Color(0xFF0B2011),
    primaryContainer = Color(0xFF25523A),
    onPrimaryContainer = Color(0xFFC1EAC5),
    secondary = Color(0xFF8FBFD9),
    tertiary = Color(0xFFE0B84A),
    background = Color(0xFF121814),
    onBackground = Color(0xFFE3E8E2),
    surface = Color(0xFF1A211C),
    onSurface = Color(0xFFE3E8E2),
    surfaceVariant = Color(0xFF2A332C),
    onSurfaceVariant = Color(0xFFBFC9C1)
)

@Composable
fun LibretaDireccionesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = EsquemaOscuro, content = content)
}
