package com.example.moneymanagerg5.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material.Typography
import com.example.moneymanagerg5.R

val Roboto = FontFamily(
    Font(R.font.roboto_variablefont, FontWeight.Normal),
    Font(R.font.roboto_italic, FontWeight.Normal, FontStyle.Italic)
)

val AppTypography = Typography(
    defaultFontFamily = Roboto
)

private val LightColorPalette = lightColors()

@Composable
fun MoneyManagerG5Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = AppTypography,
        shapes = MaterialTheme.shapes,
        content = content
    )
} 