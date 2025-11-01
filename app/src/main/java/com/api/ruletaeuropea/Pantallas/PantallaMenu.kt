package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.data.entity.Jugador
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight


@Composable
fun PantallaMenu(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val background = Brush.radialGradient(
        colors = listOf(Color(0xFF151515), Color(0xFF0E0E0E)),
        center = Offset(0.3f, 0.3f),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, ${jugador.value.NombreJugador}",
                color = Color(0xFFFFE97F),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            MenuButtons(
                onPlay = { navController.navigate("apuestas") },
                onRanking = { navController.navigate("ranking") },
                onHistory = { navController.navigate("historial") },
                onSettings = { navController.navigate("ajustes") },
                onExit = {
                    jugador.value = Jugador(NombreJugador = "Invitado", NumMonedas = 1000)
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun MenuButtons(
    onPlay: () -> Unit,
    onRanking: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    val buttonModifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 6.dp)
        .height(60.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoldButton("Jugar", onPlay, buttonModifier)
        GoldButton("Ranking", onRanking, buttonModifier)
        GoldButton("Historial", onHistory, buttonModifier)
        GoldButton("Ajustes", onSettings, buttonModifier)
        GoldButton("Salir", onExit, buttonModifier)
    }
}

@Composable
private fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "press-scale")
    val haptics = LocalHapticFeedback.current
    val animatedElevation by animateDpAsState(targetValue = if (pressed) 6.dp else 12.dp)

    val shimmerX = remember { Animatable(-120f) }

    LaunchedEffect(Unit) {
        while (true) {
            shimmerX.animateTo(
                targetValue = 720f,
                animationSpec = tween(2400, easing = LinearEasing)
            )
            shimmerX.snapTo(-120f)
        }
    }

    val fillGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300))
    )

    val borderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFF2B2), Color(0xFFFFC107)),
        start = Offset(0f, 0f),
        end = Offset(300f, 120f)
    )

    BoxWithConstraints(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = fillGradient)
            .border(2.dp, borderGradient, RoundedCornerShape(16.dp))
            .shadow(animatedElevation, RoundedCornerShape(16.dp), clip = false)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val xDp = with(density) {
            val maxPx = maxWidth.toPx()                      // maxWidth en pixels
            val valuePx = shimmerX.value.coerceIn(0f, maxPx) // coerceIn en pixels
            valuePx.toDp()                                  // convertir a Dp
        }

        // Texto del botón
        Text(
            text = text,
            color = Color(0xFF1A1A1A),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Shimmer
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(90.dp)
                    .offset(x = xDp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.24f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 200f)
                        )
                    )
            )
        }
    }
}

