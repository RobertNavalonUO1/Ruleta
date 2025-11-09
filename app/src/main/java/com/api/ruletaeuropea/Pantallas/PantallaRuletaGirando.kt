package com.api.ruletaeuropea.pantallas

import com.api.ruletaeuropea.App
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.navigation.NavController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.componentes.CoinsDisplay
import com.api.ruletaeuropea.componentes.rememberBallPhysicsState
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.logica.calcularPago
import com.api.ruletaeuropea.logica.evaluarApuesta
import com.api.ruletaeuropea.logica.tipoApuesta
import com.api.ruletaeuropea.logica.construirApuestaCompleta
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.api.ruletaeuropea.data.entity.Ruleta
import com.api.ruletaeuropea.data.entity.Historial
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.systemBarsPadding

// Colores y tamaños comunes (evitar magic numbers)
private val Gold = Color(0xFFFFD700)
private val GlassBg = Color(0x661A1A1A) // ~40% alpha
private val CardShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(28.dp)
private val RedNumbers: Set<Int> = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)

/**
 * Pantalla de giro de la ruleta con animación, física de bola y resultado.
 * Mantiene la firma pública, lógica y flujo de navegación existentes.
 */
@Composable
fun PantallaRuletaGirando(
    navController: NavController,
    jugador: Jugador,
    apuestas: MutableState<List<Apuesta>>,
    onActualizarSaldo: (Int) -> Unit
) {
    var resultado by rememberSaveable { mutableStateOf<Int?>(null) }
    var mostrarResultado by rememberSaveable { mutableStateOf(false) }
    var ballThrown by rememberSaveable { mutableStateOf(false) }
    var showDebug by rememberSaveable { mutableStateOf(true) }

    // No auto-simular, esperar a que el usuario lance la bola
    
    // Reemplazado por Box porque no usamos maxWidth/maxHeight aquí
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Fondo según estado
        val fondoResId = if (mostrarResultado) R.drawable.fondo_1 else R.drawable.fondo
        Image(
            painter = painterResource(id = fondoResId),
            contentDescription = null, // decorativo
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Overlay radial optimizado con drawWithCache: se recalcula solo si cambia size
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * 0.9f
                    val brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        center = center,
                        radius = radius
                    )
                    onDrawBehind { drawRect(brush = brush) }
                }
        )

        // Monedas arriba a la izquierda
        CoinsDisplay(
            cantidad = jugador.NumMonedas,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Contenido con transición
        Crossfade(targetState = mostrarResultado, animationSpec = tween(durationMillis = 500)) { showResult ->
            if (!showResult) {
                GirandoSection(
                    apuestas = apuestas.value,
                    ballThrown = ballThrown,
                    showDebug = showDebug,
                    onThrowBall = { ballThrown = true },
                    onToggleDebug = { showDebug = !showDebug },
                    onBallStopped = { section ->
                        resultado = section
                        mostrarResultado = true
                    }
                )
            } else {
                // Solo mostramos resultado si ya se generó el número
                resultado?.let { numeroGanador ->
                    ResultadoSection(
                        navController = navController,
                        jugador = jugador,
                        apuestas = apuestas,
                        resultado = numeroGanador,
                        onActualizarSaldo = onActualizarSaldo
                    )
                }
            }
        }
    }
}

/**
 * Sección visible mientras la ruleta está girando: animación + bola con física + resumen de apuesta.
 */
@Composable
private fun GirandoSection(
    apuestas: List<Apuesta>,
    ballThrown: Boolean,
    showDebug: Boolean,
    onThrowBall: () -> Unit,
    onToggleDebug: () -> Unit,
    onBallStopped: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Guardar valores de constraints antes de entrar al RowScope
        val boxMaxWidth = maxWidth
        val wheelSize = boxMaxWidth * 0.55f
        val panelMinWidth = 280.dp
        val panelWidth = maxOf(panelMinWidth, boxMaxWidth - wheelSize - 32.dp)

        // Calcular el centro de la ruleta en píxeles
        val density = LocalDensity.current
        val wheelSizePx = with(density) { wheelSize.toPx() }
        val wheelCenter = remember(wheelSizePx) {
            // Posición aproximada del centro de la ruleta en la pantalla
            Offset(wheelSizePx / 2, wheelSizePx / 2)
        }

        // Estado de física de la bola
        val ballPhysics = rememberBallPhysicsState(
            wheelRadius = wheelSize / 2,
            wheelCenter = wheelCenter,
            onBallStopped = onBallStopped
        )

        // Lanzar la bola cuando se marca ballThrown
        LaunchedEffect(ballThrown) {
            if (ballThrown && !ballPhysics.isAnimating) {
                ballPhysics.throwBall()
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contenedor de la ruleta con la bola superpuesta
                Box(
                    modifier = Modifier.size(wheelSize),
                    contentAlignment = Alignment.Center
                ) {
                    // Animación de ruleta girando
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("ruleta_animada.json"))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = progress,
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { contentDescription = "Ruleta girando" } // TODO(i18n)
                    )

                    // Dibujar la bola si se ha lanzado
                    if (ballThrown) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    // Dibujar la bola
                                    drawCircle(
                                        color = Color.White,
                                        radius = 12f,
                                        center = ballPhysics.ballState.position
                                    )
                                    drawCircle(
                                        color = Color.Black,
                                        radius = 12f,
                                        center = ballPhysics.ballState.position,
                                        style = Stroke(width = 2f)
                                    )

                                    // Dibujar línea desde el centro a la bola (para debug)
                                    if (showDebug) {
                                        drawLine(
                                            color = Color.Green,
                                            start = wheelCenter,
                                            end = ballPhysics.ballState.position,
                                            strokeWidth = 2f
                                        )
                                        
                                        // Dibujar círculo de límite
                                        drawCircle(
                                            color = Color.Red.copy(alpha = 0.3f),
                                            radius = wheelSizePx / 2 * 0.85f,
                                            center = wheelCenter,
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                }
                        )
                    }
                }

                // Tarjeta de resumen de apuesta
                BetSummaryCard(
                    apuestas = apuestas,
                    modifier = Modifier.width(panelWidth)
                )
            }

            // Controles inferiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para lanzar la bola
                Button(
                    onClick = onThrowBall,
                    enabled = !ballThrown,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = if (ballThrown) "Ball thrown" else "Throw Ball",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón para toggle debug
                OutlinedButton(
                    onClick = onToggleDebug,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (showDebug) Gold else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showDebug) Gold else Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (showDebug) "Debug ON" else "Debug OFF",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Panel de debug
            if (showDebug && ballThrown) {
                Spacer(modifier = Modifier.height(8.dp))
                DebugPanel(debugInfo = ballPhysics.debugInfo)
            }
        }
    }
}

/**
 * Panel de debug que muestra información de la física de la bola
 */
@Composable
private fun DebugPanel(
    debugInfo: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .border(width = 1.dp, color = Color.Green.copy(alpha = 0.5f), shape = CardShape)
            .padding(12.dp)
    ) {
        Text(
            text = "🔍 DEBUG INFO",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = debugInfo,
            fontSize = 11.sp,
            color = Color(0xFF00FF00),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

/**
 * Tarjeta estilo "glass" con el resumen de apuestas y total.
 */
@Composable
private fun BetSummaryCard(
    apuestas: List<Apuesta> = emptyList(),
    modifier: Modifier = Modifier
) {
    val total = remember(apuestas) { apuestas.sumOf { it.valorMoneda } }

    Column(
        modifier = modifier
            .clip(CardShape)
            .background(GlassBg)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f), shape = CardShape)
            .padding(20.dp)
            .semantics { contentDescription = "Resumen de apuesta" } // TODO(i18n)
    ) {
        Text(
            text = "YOUR BET:", // TODO(i18n)
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (apuestas.isEmpty()) {
            Text(
                text = "No bets placed", // TODO(i18n)
                fontSize = 16.sp,
                color = Color(0xFFFFA500)
            )
        } else {
            apuestas.forEachIndexed { index, it ->
                Text(
                    text = "${tipoApuesta(it.numero)}: ${it.valorMoneda}",
                    fontSize = 16.sp,
                    color = Color(0xFFFFA500)
                )
                if (index != apuestas.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "TOTAL: $total C", // TODO(i18n)
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Sección con el resultado: badge del número ganador, detalle y acciones.
 * Ejecuta las escrituras a Room una única vez por número de resultado.
 */
@Composable
private fun ResultadoSection(
    navController: NavController,
    jugador: Jugador,
    apuestas: MutableState<List<Apuesta>>,
    resultado: Int,
    onActualizarSaldo: (Int) -> Unit
) {
    // Calcular ganadoras y pago total (recordado por resultado y apuestas)
    val apuestasGanadoras = remember(resultado, apuestas.value) { apuestas.value.filter { evaluarApuesta(it, resultado) } }
    val pagoTotal = remember(resultado, apuestas.value) { calcularPago(apuestas.value, resultado) }

    // Guardar que ya persistimos este resultado (sobrevive rotación)
    var lastPersistedResult by rememberSaveable { mutableStateOf<Int?>(null) }

    // Escrituras a DB una única vez por resultado (clave: resultado)
    LaunchedEffect(resultado) {
        if (lastPersistedResult == resultado) return@LaunchedEffect

        val pago = pagoTotal
        val nuevoSaldo = jugador.NumMonedas + pago

        withContext(Dispatchers.IO) {
            val daoRuleta = App.database.ruletaDao()
            val daoApuesta = App.database.apuestaDao()
            val daoJugador = App.database.jugadorDao()
            val daoHistorial = App.database.historialDao()

            // Inserta el resultado de la ruleta y obtiene ID
            val idRuleta = daoRuleta.insertar(Ruleta(NumeroGanador = resultado))

            // Actualiza saldo del jugador en BD
            val jugadorActualizado = jugador.copy(NumMonedas = nuevoSaldo)
            daoJugador.actualizar(jugadorActualizado)

            // Guarda cada apuesta y su registro de historial
            apuestas.value.forEach { apuesta ->
                val apuestaCompleta = construirApuestaCompleta(apuesta, jugador, resultado, idRuleta)
                val idApuesta = daoApuesta.insertar(apuestaCompleta)
                val registroHistorial = Historial(
                    NombreJugador = jugador.NombreJugador,
                    NumApuesta = idApuesta,
                    Resultado = resultado.toString(),
                    SaldoDespues = nuevoSaldo
                )
                daoHistorial.insertar(registroHistorial)
            }
        }

        // Actualiza estado en UI (una vez)
        onActualizarSaldo(pago)
        lastPersistedResult = resultado
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ResultBadge(
            numero = resultado,
            modifier = Modifier
                .size(120.dp)
                .semantics { contentDescription = "Resultado: $resultado" } // TODO(i18n)
        )

        // Panel de resultados (glass card)
        Column(
            modifier = Modifier
                .clip(CardShape)
                .background(GlassBg.copy(alpha = 0.8f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f), shape = CardShape)
                .padding(16.dp)
                .widthIn(min = 280.dp, max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pagoTotal > 0) {
                Text(
                    text = "YOU WON", // TODO(i18n)
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            } else {
                Text(
                    text = "YOU LOSE", // TODO(i18n)
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0D0D0)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            apuestasGanadoras.forEach { it ->
                Text(
                    text = "${tipoApuesta(it.numero)}: ${it.valorMoneda}",
                    fontSize = 16.sp,
                    color = Gold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TOTAL: $pagoTotal C", // TODO(i18n)
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        ActionButtons(
            onPlayAgain = {
                apuestas.value = emptyList()
                navController.popBackStack()
            },
            onExit = {
                apuestas.value = emptyList()
                navController.navigate("menu") {
                    popUpTo("menu") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

/**
 * Muestra un badge redondeado con el número ganador y color contextual (rojo/negro/verde).
 */
@Composable
private fun ResultBadge(
    numero: Int,
    modifier: Modifier = Modifier
) {
    val bgColor = when (numero) {
        0 -> Color(0xFF1DB954) // verde más armónico en oscuro
        in RedNumbers -> Color.Red
        else -> Color.Black
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$numero",
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Gold
        )
    }
}

/**
 * Botones de acción: jugar de nuevo (primario dorado) y salir (outlined).
 */
@Composable
private fun ActionButtons(
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
        ) {
            Text(text = "Play again", fontWeight = FontWeight.SemiBold) // TODO(i18n)
        }

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = PillShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Text(text = "Exit", fontWeight = FontWeight.SemiBold) // TODO(i18n)
        }
    }
}

/*
================ Manual Test Checklist (marcar al revisar PR) ================
[ ] Inicio: se muestra animación ~1.5s + 2s, luego Crossfade suave al resultado.
[ ] Sin apuestas: BetSummaryCard muestra "No bets placed" y TOTAL: 0 sin crash.
[ ] Varias apuestas, algunas ganan: listado correcto, pagoTotal coincide con lógica calcularPago.
[ ] Botón Play again: limpia apuestas y hace popBackStack() correctamente.
[ ] Botón Exit: navega a "menu" con popUpTo("menu"){inclusive=true} sin duplicados.
[ ] Rotación: NO se repiten escrituras en Room (guardia lastPersistedResult evita duplicados).
[ ] Accesibilidad: contentDescription presentes en ruleta y badge; contraste AA legible.
[ ] CoinsDisplay permanece en esquina superior con padding.
[ ] Responsividad: en pantallas grandes rueda ≈55% ancho y panel >=280dp; en pequeñas no desborda.
[ ] Colores del badge: verde para 0, rojo/negro según número; texto dorado 64sp.

Resumen de cambios (PR):
1. Refactor a estructura modular con componentes reutilizables (GirandoSection, BetSummaryCard, ResultadoSection, ResultBadge, ActionButtons).
2. Añadido Crossfade (500ms) entre estados y BoxWithConstraints para responsividad.
3. Implementado estilo glass cards con overlay radial y paleta oscuro+dorado.
4. Mejora de accesibilidad: semantics y contentDescription clave.
5. Cálculos y filtrados con remember/rememberSaveable para rendimiento y estado tras rotación.
6. Escrituras a Room consolidadas en LaunchedEffect(resultado) con guardia de idempotencia.
7. Botones Material3 nativos con estilos (sin dependencias ni drawables nuevos).
8. Añadido checklist de pruebas y TODO(i18n) para internacionalización futura.
*/
