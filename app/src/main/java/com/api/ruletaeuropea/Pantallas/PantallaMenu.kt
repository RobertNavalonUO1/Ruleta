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
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@Composable
fun PantallaMenu(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val dorado = Color(0xFFFFD700)
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Estados persistentes
    val limitesIndex = rememberSaveable { mutableStateOf(1) } // Estándar por defecto
    val velocidadIndex = rememberSaveable { mutableStateOf(1) } // Normal por defecto
    val isDark = rememberSaveable { mutableStateOf(false) }
    val showReglas = rememberSaveable { mutableStateOf(false) }

    // Mostrar snackbar solo al cambiar segmentos
    LaunchedEffect(limitesIndex.value, velocidadIndex.value) {
        snackbarHostState.showSnackbar("Preferencia guardada")
    }

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

        // Detección de orientación/altura disponible
        val cfg = LocalConfiguration.current
        val isPortrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val maxW = maxWidth
            val maxH = maxHeight
            val isCompactH = maxH < 520.dp

            // ElevatedCard responsive sin scroll
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = if (isPortrait) 20.dp else 28.dp)
                    .fillMaxWidth(if (isPortrait) 0.96f else 0.90f)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                val innerPad = if (isPortrait && !isCompactH) 20.dp else 16.dp

                Box { // Para anclar snackbar host
                    if (isPortrait && !isCompactH) {
                        // Una columna (portrait alto suficiente)
                        Column(
                            modifier = Modifier
                                .padding(all = innerPad),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hola, ${jugador.value.NombreJugador}",
                                color = dorado,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            CardSaldo(
                                saldo = jugador.value.NumMonedas,
                                nivel = 1,
                                progreso = 0.35f,
                                onRecargar = {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar("Abre tienda (pendiente)") }
                                }
                            )

                            PillsSection()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showReglas.value = true }) { Text("Reglas", color = dorado) }
                                ToggleTema(isDark = isDark.value) { isDark.value = !isDark.value }
                            }

                            Segment(
                                items = listOf("Bajo", "Estándar", "Alto"),
                                selected = limitesIndex.value,
                                onSelect = { idx ->
                                    limitesIndex.value = idx
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                dense = true
                            )
                            Segment(
                                items = listOf("Lenta", "Normal", "Rápida"),
                                selected = velocidadIndex.value,
                                onSelect = { idx ->
                                    velocidadIndex.value = idx
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                dense = true
                            )

                            AccesosRapidos(
                                onPrivada = {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar("Creando mesa privada…") }
                                },
                                onPractica = {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar("Entrando en modo práctica…") }
                                },
                                onMulti = {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar("Buscando partidas multijugador…") }
                                }
                            )

                            UltimosResultados(nums = listOf(14, 0, 29, 3, 21, 7, 17, 32, 1, 9))

                            MenuButtons(
                                onPlay = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate("apuestas")
                                },
                                onRanking = { navController.navigate("ranking") },
                                onHistory = { navController.navigate("historial") },
                                dense = false
                            )
                        }
                    } else {
                        // Dos columnas (landscape o altura compacta)
                        Row(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Hola, ${jugador.value.NombreJugador}",
                                    color = dorado,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                CardSaldo(
                                    saldo = jugador.value.NumMonedas,
                                    nivel = 1,
                                    progreso = 0.35f,
                                    onRecargar = {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch { snackbarHostState.showSnackbar("Abre tienda (pendiente)") }
                                    }
                                )
                                PillsSection()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showReglas.value = true }) { Text("Reglas", color = dorado) }
                                    ToggleTema(isDark = isDark.value) { isDark.value = !isDark.value }
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Segment(
                                    items = listOf("Bajo", "Estándar", "Alto"),
                                    selected = limitesIndex.value,
                                    onSelect = { idx ->
                                        limitesIndex.value = idx
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    dense = true
                                )
                                Segment(
                                    items = listOf("Lenta", "Normal", "Rápida"),
                                    selected = velocidadIndex.value,
                                    onSelect = { idx ->
                                        velocidadIndex.value = idx
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    dense = true
                                )
                                AccesosRapidos(
                                    onPrivada = {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch { snackbarHostState.showSnackbar("Creando mesa privada…") }
                                    },
                                    onPractica = {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch { snackbarHostState.showSnackbar("Entrando en modo práctica…") }
                                    },
                                    onMulti = {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch { snackbarHostState.showSnackbar("Buscando partidas multijugador…") }
                                    }
                                )
                                UltimosResultados(nums = listOf(14, 0, 29, 3, 21, 7, 17, 32, 1, 9))
                                MenuButtons(
                                    onPlay = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        navController.navigate("apuestas")
                                    },
                                    onRanking = { navController.navigate("ranking") },
                                    onHistory = { navController.navigate("historial") },
                                    dense = true
                                )
                            }
                        }
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
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
        ) { Text("Exit") }

        // Diálogo de reglas
        DialogReglas(show = showReglas.value) { showReglas.value = false }
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

@Composable
private fun CardSaldo(saldo: Int, nivel: Int, progreso: Float, onRecargar: () -> Unit) {
    val dorado = Color(0xFFFFD700)
    val shape = RoundedCornerShape(16.dp)
    val progresoClamped = progreso.coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Saldo", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("€ ${saldo.coerceAtLeast(0)}", color = dorado, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onRecargar, border = BorderStroke(1.dp, dorado), colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado)) {
                Text("Recargar")
            }
        }
        LinearProgressIndicator(
            progress = { progresoClamped },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = dorado,
            trackColor = Color.White.copy(alpha = 0.15f)
        )
        Text("Nivel $nivel", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun Pill(text: String) {
    val dorado = Color(0xFFFFD700)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, dorado.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillsSection() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Pill("RTP 97.30%")
        Pill("0 único (europea)")
        Pill("La Partage opcional")
    }
}

@Composable
private fun Segment(
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dense: Boolean = false
) {
    val shape = RoundedCornerShape(12.dp)
    val pad = if (dense) 6.dp else 8.dp
    val txtSize = 12.sp
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(Modifier.padding(pad)) {
            items.forEachIndexed { i, label ->
                val on = i == selected
                val bg = if (on) Color(0xFF0F141B) else Color.Transparent
                val txt = if (on) Color.White else Color(0xFF9FB0C2)
                Text(
                    text = label,
                    color = txt,
                    fontSize = txtSize,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(i) }
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = pad)
                )
            }
        }
    }
}

@Composable
private fun AccesosRapidos(
    onPrivada: () -> Unit,
    onPractica: () -> Unit,
    onMulti: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onPrivada) { Text("Mesa privada") }
        OutlinedButton(onClick = onPractica) { Text("Modo práctica") }
        OutlinedButton(onClick = onMulti, modifier = Modifier.weight(1f)) { Text("Multijugador") }
    }
}

@Composable
private fun UltimosResultados(nums: List<Int>) {
    val rojos = setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        nums.take(10).forEach { n ->
            val c = when {
                n == 0 -> Color(0xFF00B341)
                rojos.contains(n) -> Color(0xFFC1121F)
                else -> Color(0xFFBFC7D1)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Text(
                    n.toString(),
                    color = c,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DialogReglas(show: Boolean, onDismiss: () -> Unit) {
    val dorado = Color(0xFFFFD700)
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reglas de la Ruleta Europea", color = dorado, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("37 casillas (0–36). 0 en verde; resto alterna rojo/negro.")
                Text("Pagos:")
                Text("• Pleno 35:1\n• Caballo 17:1\n• Calle 11:1\n• Esquina 8:1\n• Línea 5:1\n• Docena/Columna 2:1\n• Par/Impar, Rojo/Negro, Falta/Pasa 1:1")
                Text("Reglas opcionales: La Partage / En Prison para apuestas 1:1 cuando sale 0.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = dorado) }
        }
    )
}

@Composable
private fun ToggleTema(isDark: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Tema oscuro", color = Color.White.copy(alpha = 0.9f))
        Spacer(Modifier.width(8.dp))
        Switch(checked = isDark, onCheckedChange = { onToggle() })
    }
}
