@file:Suppress("MagicNumber")

package com.nativecoders.brand

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Logo “Native Coders” con efecto glitch (opción 1C) en Compose.
 * - Degradado neon en “NATIVE"
 * - “CODERS” en blanco
 * - Capas RGB sutiles (cian/magenta)
 * - Scanlines, barras glitch y cursor
 */
@Composable
fun NativeCodersGlitchLogo(
    modifier: Modifier = Modifier,
    darkBackground: Color = Color(0xFF0B0F14),
    corner: Dp = 24.dp,
    enableBackground: Boolean = true,
    intensity: Float = 0.55f,
) {
    // Tamaño base equivalente al SVG 1200x340
    val aspectRatio = 1200f / 340f

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .then(
                if (enableBackground)
                    Modifier.background(darkBackground, RoundedCornerShape(corner))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animaciones
        val infinite = rememberInfiniteTransition()
        val jitterA by infinite.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1600
                    0f at 0
                    0.5f at 530
                    ( -0.5f ) at 1060
                    0f at 1600
                }, repeatMode = RepeatMode.Restart
            )
        )
        val jitterB by infinite.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1600
                    0f at 0
                    ( -0.5f ) at 430
                    0.5f at 980
                    0f at 1600
                }, repeatMode = RepeatMode.Restart
            )
        )
        val splitC by infinite.animateFloat(
            initialValue = -1f, targetValue = -2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing)
            )
        )
        val splitM by infinite.animateFloat(
            initialValue = 1f, targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing)
            )
        )
        val cursorAlpha by infinite.animateFloat(
            initialValue = 1f, targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at 0
                    0f at 500
                    1f at 1000
                }
            )
        )
        val bar1 by infinite.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing)
            )
        )
        val bar2 by infinite.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, delayMillis = 180, easing = LinearEasing)
            )
        )
        val bar3 by infinite.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, delayMillis = 360, easing = LinearEasing)
            )
        )

        // Paleta
        val mint = Color(0xFF7CF9D2)
        val cyan = Color(0xFF3DE1F7)
        val vio = Color(0xFF9B6BFF)
        val white = Color.White
        val ink = Color(0xFF071018) // “stroke” oscuro

        // Degradado principal de “NATIVE”
        val neon = Brush.linearGradient(listOf(mint, cyan, vio))

        // Tipografías
        val headlineFamily = FontFamily.SansSerif

        // Dimensiones relativas al contenedor
        val density = LocalDensity.current
        val px = with(density) { 1.dp.toPx() }

        // Offsets suaves (jitter y split)
        val jitterPxA = (0.5f * intensity) * px
        val jitterPxB = (0.5f * intensity) * px
        val splitCPx = (splitC * intensity) * px
        val splitMPx = (splitM * intensity) * px

        // Layout general: dejamos padding interno similar a SVG (x≈90)
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)) {
            // Scanlines muy sutiles
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.Center)
                    .drawWithContent {
                        drawContent()
                        val step = 40f
                        val lineH = 2f
                        val lines = (size.height / step).roundToInt()
                        val scanAlpha = 0.08f
                        for (i in 1..lines) {
                            val y = i * step
                            drawRect(
                                color = Color.White.copy(alpha = scanAlpha),
                                topLeft = Offset(0f, y - lineH / 2),
                                size = Size(size.width, lineH)
                            )
                        }
                    }
            ) {}

            // Texto principal y capas RGB
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                val nativeText = "NATIVE"
                val codersText = "CODERS"

                @Composable
                fun GlitchText(
                    text: String,
                    sizeSp: Float,
                    letterSpacing: Float,
                    fill: Brush,
                    shadowColor: Color,
                    jitter: Float
                ) {
                    val style = androidx.compose.ui.text.TextStyle(
                        fontFamily = headlineFamily,
                        fontSize = sizeSp.sp,
                        letterSpacing = letterSpacing.sp,
                        shadow = Shadow(
                            color = shadowColor,
                            blurRadius = 2.5f,
                            offset = Offset(0f, 0f)
                        )
                    )
                    val rich = remember(text) {
                        buildAnnotatedString {
                            withStyle(SpanStyle(brush = fill)) { append(text) }
                        }
                    }
                    Text(
                        text = rich,
                        style = style,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.offset(x = jitter.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.Center, // cambiado de Top a Center para evitar overflow superior
                    horizontalAlignment = Alignment.Start
                ) {
                    // Capas RGB desplazadas (detrás)
                    Box {
                        Text(
                            text = nativeText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = headlineFamily,
                                fontSize = 130.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF00F0FF).copy(alpha = 0.5f),
                                shadow = Shadow(ink, blurRadius = 1.5f)
                            ),
                            modifier = Modifier
                                .offset(x = with(density) { (splitCPx / px).dp })
                        )
                        Text(
                            text = nativeText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = headlineFamily,
                                fontSize = 130.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFFFF3B81).copy(alpha = 0.45f),
                                shadow = Shadow(ink, blurRadius = 1.5f)
                            ),
                            modifier = Modifier
                                .offset(x = with(density) { (splitMPx / px).dp })
                        )
                        GlitchText(
                            text = nativeText,
                            sizeSp = 130f,
                            letterSpacing = 0.5f,
                            fill = neon,
                            shadowColor = ink,
                            jitter = jitterA * (jitterPxA / px)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // Grupo: CODERS (abajo)
                    Box {
                        Text(
                            text = codersText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = headlineFamily,
                                fontSize = 126.sp,
                                letterSpacing = 0.3.sp,
                                color = Color(0xFF00F0FF).copy(alpha = 0.5f),
                                shadow = Shadow(ink, blurRadius = 1.5f)
                            ),
                            modifier = Modifier
                                .offset(x = with(density) { (splitCPx / px).dp })
                        )
                        Text(
                            text = codersText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = headlineFamily,
                                fontSize = 126.sp,
                                letterSpacing = 0.3.sp,
                                color = Color(0xFFFF3B81).copy(alpha = 0.45f),
                                shadow = Shadow(ink, blurRadius = 1.5f)
                            ),
                            modifier = Modifier
                                .offset(x = with(density) { (splitMPx / px).dp })
                        )
                        // Capa principal blanca
                        Text(
                            text = codersText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = headlineFamily,
                                fontSize = 126.sp,
                                letterSpacing = 0.3.sp,
                                color = white,
                                shadow = Shadow(ink, blurRadius = 2.5f)
                            ),
                            modifier = Modifier
                                .offset(x = (jitterB * (jitterPxB / px)).dp)
                        )
                    }
                }
            }

            // Barras glitch (derecha)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                val w = this@BoxWithConstraints.maxWidth
                val h = this@BoxWithConstraints.maxHeight

                val barLeft = w * 0.72f
                Box(
                    Modifier
                        .offset(x = barLeft, y = h * 0.27f)
                        .height(8.dp)
                        .width((60.dp * (if (bar1 < 0.4f) bar1 / 0.4f else (1f - bar1)) ).coerceAtLeast(0.dp))
                        .background(cyan, RoundedCornerShape(2.dp))
                )
                Box(
                    Modifier
                        .offset(x = w * 0.75f, y = h * 0.58f)
                        .height(8.dp)
                        .width((44.dp * (if (bar2 < 0.4f) bar2 / 0.4f else (1f - bar2))).coerceAtLeast(0.dp))
                        .background(vio, RoundedCornerShape(2.dp))
                )
                Box(
                    Modifier
                        .offset(x = w * 0.77f, y = h * 0.44f)
                        .height(8.dp)
                        .width((28.dp * (if (bar3 < 0.4f) bar3 / 0.4f else (1f - bar3))).coerceAtLeast(0.dp))
                        .background(cyan, RoundedCornerShape(2.dp))
                )

                // Cursor/underscore
                Box(
                    Modifier
                        .offset(x = w * 0.867f, y = h * 0.635f)
                        .size(width = 18.dp, height = 6.dp)
                        .graphicsLayer { alpha = cursorAlpha }
                        .background(white, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F14)
@Composable
private fun PreviewNativeCodersGlitchLogo() {
    MaterialTheme {
        NativeCodersGlitchLogo(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            enableBackground = true,
            intensity = 0.55f
        )
    }
}
