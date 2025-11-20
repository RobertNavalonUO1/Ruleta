package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.data.entity.Jugador
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.api.ruletaeuropea.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun PantallaMenu(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val dorado = Color(0xFFFFD700)
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Helpers de experiencia para la tarjeta de saldo
    fun expNecesaria(nivel: Int): Int = 100 + (nivel - 1) * 50
    val nivelActual = jugador.value.Nivel
    val expNivel = expNecesaria(nivelActual)
    val progresoExp = (jugador.value.ExpActual.toFloat() / expNivel.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isCompactH = maxHeight < 520.dp
            val rightScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Columna izquierda: saludo + botones menú
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactH) 18.dp else 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hola, ${jugador.value.NombreJugador}",
                        color = dorado,
                        fontSize = if (isCompactH) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = if (isCompactH) 4.dp else 8.dp)
                    )
                    MenuButtons(
                        onPlay = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate("apuestas")
                        },
                        onRanking = { navController.navigate("ranking") },
                        onHistory = { navController.navigate("historial") },
                        dense = isCompactH
                    )
                    Spacer(Modifier.weight(1f))
                }
                // Columna derecha: tarjeta de saldo (scroll solo en pantallas muy pequeñas si crece más contenido futuro)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (isCompactH) Modifier.verticalScroll(rightScrollState) else Modifier),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SaldoCard(
                        jugador = jugador.value,
                        nivelActual = nivelActual,
                        progreso = progresoExp,
                        dorado = dorado,
                        compact = isCompactH,
                        onRecargar = {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            scope.launch { snackbarHostState.showSnackbar("Abre tienda (pendiente)") }
                        }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        OutlinedButton(
            onClick = {
                jugador.value = Jugador(NombreJugador = "Guest", NumMonedas = 1000)
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
            border = BorderStroke(1.dp, dorado),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 35.dp)
                .height(48.dp)
        ) { Text("Exit") }
    }
}

@Composable
private fun SaldoCard(
    jugador: Jugador,
    nivelActual: Int,
    progreso: Float,
    dorado: Color,
    compact: Boolean,
    onRecargar: () -> Unit
) {
    val shapeSaldo = RoundedCornerShape(18.dp)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = shapeSaldo,
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(if (compact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Saldo", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    Text(
                        "€ ${jugador.NumMonedas.coerceAtLeast(0)}",
                        color = dorado,
                        fontSize = if (compact) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Nivel $nivelActual", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onRecargar,
                    border = BorderStroke(1.dp, dorado),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
                    modifier = Modifier.height(if (compact) 36.dp else 40.dp)
                ) { Text("Recargar") }
            }
            // Barra de experiencia
            val p = progreso.coerceIn(0f, 1f)
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EXP", color = Color.White.copy(alpha = 0.70f), fontSize = 12.sp)
                    Text("${(p * 100).toInt()}%", color = dorado.copy(alpha = 0.95f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 6.dp else 8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = dorado,
                    trackColor = Color.White.copy(alpha = 0.18f)
                )
            }
        }
    }
}

@Composable
private fun MenuButtons(
    onPlay: () -> Unit,
    onRanking: () -> Unit,
    onHistory: () -> Unit,
    dense: Boolean = false
) {
    val dorado = Color(0xFFFFD700)
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val h = if (dense) 48.dp else 52.dp
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(h),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("Jugar", fontWeight = FontWeight.Bold) }
        Button(
            onClick = onRanking,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(h),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("Ranking", fontWeight = FontWeight.Bold) }
        Button(
            onClick = onHistory,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(h),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black)
        ) { Text("Historial", fontWeight = FontWeight.Bold) }
    }
}
