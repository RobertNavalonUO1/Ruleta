package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
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
import com.api.ruletaeuropea.R
import androidx.compose.foundation.BorderStroke // añadido

@Composable
fun PantallaMenu(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val dorado = Color(0xFFFFD700)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Fondo con la misma imagen de PantallaLogin
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Contenido centrado dentro de una tarjeta translúcida para contraste
        ElevatedCard(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hello, ${jugador.value.NombreJugador}",
                    color = dorado,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))

                // Botones con estilo Material 3 similares a PantallaLogin
                MenuButtons(
                    onPlay = { navController.navigate("apuestas") },
                    onRanking = { navController.navigate("ranking") },
                    onHistory = { navController.navigate("historial") }
                )
            }
        }

        // Botón Salir separado, abajo derecha, como OutlinedButton similar a PantallaLogin
        OutlinedButton(
            onClick = {
                jugador.value = Jugador(NombreJugador = "Guest", NumMonedas = 1000)
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            enabled = true,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
            border = BorderStroke(1.dp, dorado),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 35.dp)
                .height(48.dp)
        ) {
            Text("Exit")
        }
    }
}

@Composable
private fun MenuButtons(
    onPlay: () -> Unit,
    onRanking: () -> Unit,
    onHistory: () -> Unit
) {
    val dorado = Color(0xFFFFD700)
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(56.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("Play", fontWeight = FontWeight.Bold) }

        Button(
            onClick = onRanking,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(56.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("Ranking", fontWeight = FontWeight.Bold) }

        Button(
            onClick = onHistory,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(56.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("History", fontWeight = FontWeight.Bold) }
    }
}
