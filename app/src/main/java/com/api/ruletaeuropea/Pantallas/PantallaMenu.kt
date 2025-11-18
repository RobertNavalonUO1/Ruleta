package com.api.ruletaeuropea.pantallas

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.google.accompanist.flowlayout.FlowRow




@SuppressLint("UnusedBoxWithConstraintsScope")
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
    // Eliminado limitesIndex y velocidadIndex (selección Bajo/Estándar/Alto y lenta/normal/rápida)
    val showReglas = rememberSaveable { mutableStateOf(false) }

    // Helpers de experiencia (mismas fórmulas que en la ruleta)
    fun expNecesaria(nivel: Int): Int = 100 + (nivel - 1) * 50
    val expActual = jugador.value.ExpActual
    val nivelActual = jugador.value.Nivel
    val expNivel = expNecesaria(nivelActual)
    val progresoExp = (expActual.toFloat() / expNivel.toFloat()).coerceIn(0f, 1f)

    // Eliminado LaunchedEffect que mostraba snackbar al cambiar preferencias de límites/velocidad

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
            val maxH = maxHeight
            val isCompactH = maxH < 520.dp

            // ElevatedCard responsive sin scroll
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = if (isPortrait) 20.dp else 28.dp)
                    .fillMaxWidth(if (isPortrait) 0.96f else 0.90f)
                    .heightIn(min = 0.dp, max = maxHeight * 0.95f)
                    .verticalScroll(rememberScrollState()),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                val innerPad = if (isPortrait && !isCompactH) 20.dp else 16.dp


                if (isPortrait && !isCompactH) {
                    //Modo vertical
                    Column(
                        modifier = Modifier
                            .padding(all = innerPad),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                            nivel = nivelActual,
                            progreso = progresoExp,
                            onRecargar = {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                scope.launch { snackbarHostState.showSnackbar("Abre tienda (pendiente)") }
                            }
                        )
                        Spacer(modifier = Modifier.height(5.dp))

                        // Accesos rápidos y últimos resultados
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
                        Spacer(modifier = Modifier.height(5.dp))

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
                    //Modo horizontal o compacto
                    Row(
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        //Columna Izquierda
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
                                nivel = nivelActual,
                                progreso = progresoExp,
                                onRecargar = {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    scope.launch { snackbarHostState.showSnackbar("Abre tienda (pendiente)") }
                                }
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

                        }
                        //Columna Derecha:
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
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
                        //.align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
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
        // Barra de EXP con etiqueta
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nivel $nivel", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                Text(
                    text = "${(progresoClamped * 100).toInt()}% EXP",
                    color = dorado.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progresoClamped },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = dorado,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun AccesosRapidos(
    onPrivada: () -> Unit,
    onPractica: () -> Unit,
    onMulti: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        OutlinedButton(onClick = onPrivada) { Text("Mesa privada") }
        OutlinedButton(onClick = onPractica) { Text("Modo práctica") }
        OutlinedButton(onClick = onMulti) { Text("Multijugador") }
    }
}

@Composable
private fun UltimosResultados(nums: List<Int>) {
    val rojos = setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
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
                // Quitado detalle de "0 en verde" y la línea de La Partage / En Prison
                Text("37 casillas (0–36).")
                Text("Pagos:")
                Text("• Pleno 35:1\n• Caballo 17:1\n• Calle 11:1\n• Esquina 8:1\n• Línea 5:1\n• Docena/Columna 2:1\n• Par/Impar, Rojo/Negro, Falta/Pasa 1:1")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = dorado) }
        }
    )
}
