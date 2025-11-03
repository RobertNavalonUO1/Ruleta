package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.runtime.LaunchedEffect

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
            .navigationBarsPadding()
    ) {
        // Column principal: solo botones Jugar, Ranking e Historial
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .align(Alignment.Center), // centramos la columna
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello, ${jugador.value.NombreJugador}",
                color = Color(0xFFFFE97F),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            // MenuButtons
            MenuButtons(
                onPlay = { navController.navigate("apuestas") },
                onRanking = { navController.navigate("ranking") },
                onHistory = { navController.navigate("historial") }
            )
        }

        /*  Botón Ajustes separado, abajo derecha
        PlantillaBoton(
            text = "Ajustes",
            onClick = { navController.navigate("ajustes") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 35.dp)
                .width(140.dp)
                .height(60.dp)
        )*/

        // 🔹 Botón Salir separado, abajo derecha, debajo de Ajustes
        PlantillaBoton(
            text = "Exit",
            onClick = {
                jugador.value = Jugador(NombreJugador = "Guest", NumMonedas = 1000)
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 35.dp)
                .width(140.dp)
                .height(60.dp),
            colors = listOf(Color.White, Color(0xFF666666)), // 🔹 nuevo color opcional
            textColor = Color.Black // 🔹 nuevo color de texto opcional
        )
    }
}

@Composable
private fun MenuButtons(
    onPlay: () -> Unit,
    onRanking: () -> Unit,
    onHistory: () -> Unit
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
        GoldButton("Play", onPlay, buttonModifier)
        GoldButton("Ranking", onRanking, buttonModifier)
        GoldButton("History", onHistory, buttonModifier)
        // 🔹 Eliminado GoldButton("Ajustes") y GoldButton("Salir")
    }
}

// 🔹 GoldButton queda igual, no se modifica
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
            val maxPx = maxWidth.toPx()
            val valuePx = shimmerX.value.coerceIn(0f, maxPx)
            valuePx.toDp()
        }

        Text(
            text = text,
            color = Color(0xFF1A1A1A),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

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


