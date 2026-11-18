package com.tecsup.centrotareas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulOscuro,
    error = Rojo,
    errorContainer = RojoClaro,
    onErrorContainer = Color(0xFF410002)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulNoche,
    onPrimary = AzulOscuro,
    primaryContainer = AzulOscuro,
    onPrimaryContainer = AzulClaro
)

@Composable
fun CentroTareasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        content = content
    )
}
