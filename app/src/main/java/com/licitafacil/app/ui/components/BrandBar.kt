package com.licitafacil.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Soft
import com.licitafacil.app.ui.theme.Yellow

@Composable
fun RadarIcon(size: Dp, stroke: Float = 2.6f) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val strokeW = stroke * density
        val path1 = Path().apply {
            moveTo(s * 0.17f, s * 0.17f)
            quadraticTo(s * 0.17f, s * 0.83f, s * 0.83f, s * 0.83f)
        }
        val path2 = Path().apply {
            moveTo(s * 0.17f, s * 0.42f)
            quadraticTo(s * 0.17f, s * 0.83f, s * 0.58f, s * 0.83f)
        }
        drawPath(path1, color = Ink, style = Stroke(width = strokeW, cap = StrokeCap.Round))
        drawPath(path2, color = Ink, style = Stroke(width = strokeW, cap = StrokeCap.Round))
        drawCircle(color = Ink, radius = s * 0.075f, center = Offset(s * 0.21f, s * 0.79f))
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, box: Dp = 28.dp, textSize: Int = 16) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(box).background(Yellow, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            RadarIcon(size = box * 0.62f, stroke = if (box >= 32.dp) 2.6f else 2.4f)
        }
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = textSize.sp,
                        fontFamily = Montserrat,
                        letterSpacing = (-0.015).em,
                    ),
                ) { append("Licita") }
                withStyle(
                    SpanStyle(
                        color = Yellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = textSize.sp,
                        fontFamily = Montserrat,
                        letterSpacing = (-0.015).em,
                    ),
                ) { append("Facil") }
            },
        )
    }
}

@Composable
fun PixelRadarDecor() {
    val cells = listOf(
        0, 0, 0, 0, 0, 0, 1, 2,
        0, 0, 0, 0, 0, 1, 0, 2,
        0, 0, 0, 0, 1, 0, 1, 2,
        0, 0, 0, 1, 0, 0, 2, 1,
        0, 0, 1, 0, 1, 0, 0, 2,
    )
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        cells.chunked(8).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                row.forEach { cell ->
                    val color = when (cell) {
                        1 -> Color(0xFF374044)
                        2 -> Yellow
                        else -> Color.Transparent
                    }
                    Box(Modifier.size(10.dp).background(color))
                }
            }
        }
    }
}

@Composable
fun AppTopBar(
    onConfig: (() -> Unit)? = null,
    onConta: (() -> Unit)? = null,
    initials: String = "EU",
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().background(Ink).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BrandMark()
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onConfig != null) {
                    Row(
                        Modifier.height(44.dp).clickable(onClick = onConfig).padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = Yellow, modifier = Modifier.size(18.dp))
                        Text("Configurar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = Montserrat)
                    }
                }
                if (onConta != null) {
                    Box(Modifier.size(44.dp).clickable(onClick = onConta), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(34.dp).background(Yellow, CircleShape), contentAlignment = Alignment.Center) {
                            Text(initials, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, fontFamily = Montserrat)
                        }
                    }
                }
                trailing?.invoke()
            }
        }
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Ink).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Montserrat)
        }
    }
}

@Composable
fun MonitorStrip(summary: String, onAlterar: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "MONITORANDO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Dim,
                    letterSpacing = 1.5.sp,
                    fontFamily = Montserrat,
                )
                Text(
                    summary.ifBlank { "Sem filtros" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = Montserrat,
                    color = Ink,
                )
            }
            Text(
                "Alterar",
                modifier = Modifier.clickable(onClick = onAlterar).padding(vertical = 12.dp, horizontal = 4.dp),
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = Montserrat,
                style = TextStyle(textDecoration = TextDecoration.Underline),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, leading: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.height(24.dp).background(bg, CircleShape).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        leading?.invoke()
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat, maxLines = 1)
    }
}
