@file:Suppress("MagicNumber")

package com.nativecoders.brand

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.api.ruletaeuropea.R
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Shadow

@Composable
fun NativeCodersGlitchLogo(
    modifier: Modifier = Modifier,
    line1: String = "NATIVE",
    line2: String = "CODERS",
    showTopLogo: Boolean = true,
    intensity: Float = 0.55f,
    singleLine: Boolean = false,
) {
    val infinite = rememberInfiniteTransition()
    val jitterA by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1600
                0f at 0
                0.5f at 530
                (-0.5f) at 1060
                0f at 1600
            }, repeatMode = RepeatMode.Restart
        )
    )
    val jitterB by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1600
                0f at 0
                (-0.5f) at 430
                0.5f at 980
                0f at 1600
            }, repeatMode = RepeatMode.Restart
        )
    )
    val splitC by infinite.animateFloat(-1f, -2f, infiniteRepeatable(animation = tween(durationMillis = 2200, easing = LinearEasing)))
    val splitM by infinite.animateFloat(1f, 2f, infiniteRepeatable(animation = tween(durationMillis = 2200, easing = LinearEasing)))

    // Paleta
    val mint = Color(0xFF7CF9D2)
    val cyan = Color(0xFF3DE1F7)
    val vio = Color(0xFF9B6BFF)
    val white = Color.White
    val ink = Color(0xFF071018)
    val neon = Brush.linearGradient(listOf(mint, cyan, vio))
    val headlineFamily = FontFamily.SansSerif

    val density = LocalDensity.current
    val px = with(density) { 1.dp.toPx() }
    val jitterPxA = (0.5f * intensity) * px
    val jitterPxB = (0.5f * intensity) * px
    val splitCPx = (splitC * intensity) * px
    val splitMPx = (splitM * intensity) * px

    BoxWithConstraints(modifier = modifier.wrapContentHeight()) {
        val availableWidth = maxWidth
        // Tamaños base adaptativos
        val sizeSingle = when {
            availableWidth < 300.dp -> 56f
            availableWidth < 340.dp -> 64f
            availableWidth < 380.dp -> 72f
            else -> 80f
        }
        val sizeLine1 = if (availableWidth < 340.dp) 72f else 86f
        val sizeLine2 = if (availableWidth < 340.dp) 68f else 80f
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTopLogo) {
                Image(
                    painter = painterResource(id = R.drawable.logo3d),
                    contentDescription = "Logo",
                    modifier = Modifier.size(if (availableWidth < 320.dp) 100.dp else 140.dp)
                )
            }
            if (singleLine) {
                GlitchCombinedLine(
                    word1 = line1,
                    word2 = line2,
                    sizeSp = sizeSingle,
                    neon = neon,
                    ink = ink,
                    headlineFamily = headlineFamily,
                    jitterA = jitterA * (jitterPxA / px),
                    jitterB = jitterB * (jitterPxB / px),
                    splitCPx = splitCPx,
                    splitMPx = splitMPx,
                    px = px,
                    density = density
                )
            } else {
                GlitchWord(
                    text = line1,
                    sizeSp = sizeLine1,
                    neon = neon,
                    ink = ink,
                    headlineFamily = headlineFamily,
                    jitter = jitterA * (jitterPxA / px),
                    splitCPx = splitCPx,
                    splitMPx = splitMPx,
                    px = px,
                    density = density
                )
                GlitchWord(
                    text = line2,
                    sizeSp = sizeLine2,
                    neon = null,
                    ink = ink,
                    headlineFamily = headlineFamily,
                    jitter = jitterB * (jitterPxB / px),
                    splitCPx = splitCPx,
                    splitMPx = splitMPx,
                    px = px,
                    density = density,
                    whiteOverride = white
                )
            }
        }
    }
}

@Composable
private fun GlitchWord(
    text: String,
    sizeSp: Float,
    neon: Brush?,
    ink: Color,
    headlineFamily: FontFamily,
    jitter: Float,
    splitCPx: Float,
    splitMPx: Float,
    px: Float,
    density: Density,
    whiteOverride: Color? = null,
) {
    val styleBase = androidx.compose.ui.text.TextStyle(
        fontFamily = headlineFamily,
        fontSize = sizeSp.sp,
        letterSpacing = 0.5.sp,
        shadow = Shadow(ink, blurRadius = 1.5f)
    )
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = styleBase.copy(color = Color(0xFF00F0FF).copy(alpha = 0.45f)),
            modifier = Modifier.offset(x = with(density) { (splitCPx / px).dp })
        )
        Text(
            text = text,
            style = styleBase.copy(color = Color(0xFFFF3B81).copy(alpha = 0.45f)),
            modifier = Modifier.offset(x = with(density) { (splitMPx / px).dp })
        )
        val mainStyle = if (whiteOverride != null) {
            styleBase.copy(color = whiteOverride, shadow = Shadow(ink, blurRadius = 2.5f))
        } else {
            styleBase.copy(shadow = Shadow(ink, blurRadius = 2.5f))
        }
        if (neon != null) {
            val rich = remember(text) {
                buildAnnotatedString { withStyle(SpanStyle(brush = neon)) { append(text) } }
            }
            Text(
                text = rich,
                style = mainStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = jitter.dp)
            )
        } else {
            Text(
                text = text,
                style = mainStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = jitter.dp)
            )
        }
    }
}

@Composable
private fun GlitchCombinedLine(
    word1: String,
    word2: String,
    sizeSp: Float,
    neon: Brush,
    ink: Color,
    headlineFamily: FontFamily,
    jitterA: Float,
    jitterB: Float,
    splitCPx: Float,
    splitMPx: Float,
    px: Float,
    density: Density,
) {
    val baseStyle = androidx.compose.ui.text.TextStyle(
        fontFamily = headlineFamily,
        fontSize = sizeSp.sp,
        letterSpacing = 0.5.sp,
        shadow = Shadow(ink, blurRadius = 1.5f)
    )
    val combined = "$word1 $word2"
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // Capas desplazadas (cian / magenta) para toda la línea
        Text(
            text = combined,
            style = baseStyle.copy(color = Color(0xFF00F0FF).copy(alpha = 0.40f)),
            modifier = Modifier.offset(x = with(density) { (splitCPx / px).dp })
        )
        Text(
            text = combined,
            style = baseStyle.copy(color = Color(0xFFFF3B81).copy(alpha = 0.38f)),
            modifier = Modifier.offset(x = with(density) { (splitMPx / px).dp })
        )
        // Línea principal con degradado aplicado sólo a la primera palabra
        val rich = remember(combined) {
            buildAnnotatedString {
                withStyle(SpanStyle(brush = neon)) { append(word1) }
                append(" ")
                withStyle(SpanStyle(color = Color.White)) { append(word2) }
            }
        }
        Text(
            text = rich,
            style = baseStyle.copy(shadow = Shadow(ink, blurRadius = 2.5f)),
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = (jitterA.dp + jitterB.dp) / 2f) // mezcla de jitter para suavizar
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewNativeCodersGlitchLogoMinimal() {
    MaterialTheme {
        NativeCodersGlitchLogo(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            line1 = "RULETA",
            line2 = "EUROPEA",
            showTopLogo = true,
            intensity = 0.5f
        )
    }
}
