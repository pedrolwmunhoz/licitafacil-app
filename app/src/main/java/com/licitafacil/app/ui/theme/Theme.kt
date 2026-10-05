package com.licitafacil.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.licitafacil.app.R

val Ink = Color(0xFF1D1D1B)
val Yellow = Color(0xFFFFCC00)
val Muted = Color(0xFF374044)
val Soft = Color(0xFFDAE0E3)
val Panel = Color(0xFFF4F5F6)
val Line = Color(0xFFA7B3B7)
val Danger = Color(0xFF9E3229)
val DangerBg = Color(0xFFFBE7E5)
val DangerBorder = Color(0xFFC2453B)
val WarnBg = Color(0xFFFFF4CC)
val WarnInk = Color(0xFF5C4700)
val BluePill = Color(0xFFD8E5F5)
val BluePillInk = Color(0xFF2A3B54)
val GreenPill = Color(0xFFE3F4EC)
val GreenPillInk = Color(0xFF17694A)
val Dim = Color(0xFF656564)
val Skeleton = Color(0xFFE3E8EA)

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
)

private val Scheme = lightColorScheme(
    primary = Ink,
    secondary = Yellow,
    background = Panel,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    error = Danger,
)

private val Type = Typography(
    bodyLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Dim),
    titleLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink),
    labelLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink),
)

@Composable
fun LicitaFacilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
