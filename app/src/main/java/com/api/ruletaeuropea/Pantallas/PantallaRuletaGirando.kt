package com.api.ruletaeuropea.pantallas

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavHostController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRuletaGirando(
    navController: NavHostController,
    jugador: Jugador,
    apuestas: MutableState<List<Apuesta>>,
    onActualizarSaldo: (Int) -> Unit,
    onActualizarJugador: (Jugador) -> Unit
) {
    val ctx = LocalContext.current
    val navScope = rememberCoroutineScope()
    var layout by remember { mutableStateOf<RouletteLayoutJson?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var rawJson by remember { mutableStateOf<String?>(null) }

    val physics = remember(rawJson) { rawJson?.let { runCatching { com.api.ruletaeuropea.fisica.RoulettePhysics(JSONObject(it)) }.getOrNull() } }
    var rolling by remember { mutableStateOf(false) }

    var rotorAngleDegState by remember { mutableStateOf(0f) }
    var ballPosState by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var ballZState by remember { mutableStateOf<Float>(0f) }

    // Historial de números ganadores (más reciente primero)
    val lastResults = remember { mutableStateListOf<Int>() }

    // Resultado y overlay
    var winnerNumber by remember { mutableStateOf<Int?>(null) }
    var winAmount by remember { mutableStateOf(0) }
    var totalBet by remember { mutableStateOf(0) }
    var netResult by remember { mutableStateOf(0) }
    var showResultOverlay by remember { mutableStateOf(false) }

    // Experiencia y nivel
    var expGanada by remember { mutableStateOf(0) }
    var nivelAntes by remember { mutableStateOf(jugador.Nivel) }
    var nivelDespues by remember { mutableStateOf(jugador.Nivel) }

    LaunchedEffect(Unit) {
        runCatching { ctx.readTextFromRaw(R.raw.ruleta_layout_extended) }
            .onSuccess { txt ->
                rawJson = txt
                runCatching { parseRouletteLayout(txt) }
                    .onSuccess { layout = it; errorMsg = null }
                    .onFailure { e -> errorMsg = "No se pudo leer el layout: ${e.message}" }
            }
            .onFailure { e -> errorMsg = "No se pudo abrir el layout: ${e.message}" }
    }

    // Lanzar automáticamente la ruleta al entrar en la pantalla (viene desde botón Spin)
    LaunchedEffect(physics, layout) {
        val engine = physics ?: return@LaunchedEffect
        if (layout == null) return@LaunchedEffect
        // Configurar y lanzar una única tirada si aún no está rodando
        if (!rolling) {
            setFieldIfExists(engine, "ballMinDegInit", 1200)
            setFieldIfExists(engine, "ballMaxDegInit", 2200)
            setFieldIfExists(engine, "rotorMinDegInit", 300)
            setFieldIfExists(engine, "rotorMaxDegInit", 500)
            setFieldIfExists(engine, "speedMultiplier", 1.0f)
            setFieldIfExists(engine, "timeScale", 1.0f)
            callMethodIfExists(engine, "recomputeDerived")
            engine.reset()
            engine.launchBall()
            rolling = true
            // reset overlay
            showResultOverlay = false
            winnerNumber = null
            winAmount = 0
            totalBet = 0
            netResult = 0
        }
    }

    // Bucle físico básico
    LaunchedEffect(rolling, physics) {
        val engine = physics ?: return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            if (!rolling) break
            engine.updatePhysics()
            rotorAngleDegState = engine.rouletteAngleDeg
            ballPosState = engine.ballPosition()
            ballZState = engine.ballAltitude()
            if (!engine.rolling) {
                rolling = false
                // calcular resultado visual y mostrar overlay
                val numeroMotor = engine.resultPocketNumber()
                val ly = layout
                val numeroVisual = ly?.let { computeVisualWinner(it, engine.ballPosition(), engine.rouletteAngleDeg) }
                val numero = numeroVisual ?: numeroMotor
                val win = calculateWinForNumber(apuestas.value, numero)
                val apostado = apuestas.value.sumOf { it.valorMoneda }
                winnerNumber = numero
                winAmount = win
                totalBet = apostado
                netResult = win - apostado

                // ---- Lógica de experiencia y nivel ----
                val expBase = 50
                val expPorMoneda = 1
                val expRonda = expBase + (apostado * expPorMoneda)
                expGanada = expRonda

                var nuevoNivel = jugador.Nivel
                var nuevaExp = jugador.ExpActual + expRonda
                var monedasBonus = 0

                fun expNecesaria(nivel: Int): Int = 100 + (nivel - 1) * 50
                fun bonusMonedasPorNivel(nivel: Int): Int = 500 + (nivel - 1) * 200

                // Subir de nivel mientras supere el umbral
                while (nuevaExp >= expNecesaria(nuevoNivel)) {
                    nuevaExp -= expNecesaria(nuevoNivel)
                    nuevoNivel += 1
                    monedasBonus += bonusMonedasPorNivel(nuevoNivel)
                }

                nivelAntes = jugador.Nivel
                nivelDespues = nuevoNivel

                // Aplicar bonus de nivel al saldo
                if (monedasBonus > 0) {
                    onActualizarSaldo(monedasBonus)
                }

                // Aplicar ganancias/pérdidas de la tirada al saldo
                if (win > 0) onActualizarSaldo(win)

                // Actualizar el Jugador completo (nivel y exp) hacia fuera
                val jugadorActualizado = jugador.copy(
                    Nivel = nuevoNivel,
                    ExpActual = nuevaExp
                )
                onActualizarJugador(jugadorActualizado)

                showResultOverlay = true
                // actualizar historial (máx 15)
                lastResults.add(0, numero)
                if (lastResults.size > 15) lastResults.removeAt(lastResults.size - 1)
                // aplicar ganancias al finalizar con el resultado visual
                if (win > 0) onActualizarSaldo(win)

                // tras un pequeño delay para disfrutar del overlay, navegar a la pantalla de resultados
                // y limpiar las apuestas actuales
                navScope.launch {
                    delay(1500)
                    val net = win - apostado
                    val route = "resultados/$numero/$apostado/$win/$net/$expRonda/$nivelAntes/$nivelDespues"
                    // opcional: limpiar apuestas para la siguiente ronda
                    apuestas.value = emptyList()
                    navController.navigate(route) {
                        popUpTo("apuestas") { inclusive = false }
                    }
                }
            }
        }
    }

    // Ocultar overlay si hubiera quedado visible demasiado tiempo (seguro adicional)
    LaunchedEffect(showResultOverlay) {
        if (showResultOverlay) {
            delay(4000)
            showResultOverlay = false
        }
    }

    // Parámetros esenciales
    val wMinDeg by rememberSaveable { mutableStateOf(1200f) }
    val wMaxDeg by rememberSaveable { mutableStateOf(2200f) }
    val rotorMinDeg by rememberSaveable { mutableStateOf(300f) }
    val rotorMaxDeg by rememberSaveable { mutableStateOf(500f) }
    val speedMul by rememberSaveable { mutableStateOf(1.0f) }
    val baseAngularFriction by rememberSaveable { mutableStateOf(0.9988f) }
    val airRes by rememberSaveable { mutableStateOf(0.00030f) }

    // Aplicación de parámetros al motor
    LaunchedEffect(physics, wMinDeg, wMaxDeg, rotorMinDeg, rotorMaxDeg, speedMul, baseAngularFriction, airRes) {
        val p = physics ?: return@LaunchedEffect
        setFieldIfExists(p, "ballMinDegInit", wMinDeg.toInt())
        setFieldIfExists(p, "ballMaxDegInit", wMaxDeg.toInt())
        setFieldIfExists(p, "wMinDegInit", wMinDeg.toInt())
        setFieldIfExists(p, "wMaxDegInit", wMaxDeg.toInt())
        setFieldIfExists(p, "rotorMinDegInit", rotorMinDeg.toInt())
        setFieldIfExists(p, "rotorMaxDegInit", rotorMaxDeg.toInt())
        setFieldIfExists(p, "speedMultiplier", speedMul)
        setFieldIfExists(p, "timeScale", speedMul)
        runCatching { p.cfg = p.cfg.copy(baseAngularFriction = baseAngularFriction, airRes = airRes) }
        callMethodIfExists(p, "recomputeDerived")
    }

    val amber = Color(0xFFFFC107)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Ruleta", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0B0B),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = { Text("💰 ${jugador.NumMonedas}", modifier = Modifier.padding(end = 12.dp), color = Color(0xFFF1F1F1)) }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.fillMaxSize().padding(pad)) {
            // Fondo temático de casino
            CasinoBackground(modifier = Modifier.matchParentSize())

            // Contenido principal en tres columnas: historial, ruleta, apuestas
            Row(modifier = Modifier.fillMaxSize()) {
                // Panel izquierdo de últimos números
                ResultsSidebar(
                    modifier = Modifier
                        .width(88.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    results = lastResults
                )

                // Zona de ruleta y estados de carga/errores
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    } else if (layout == null) {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = amber, strokeWidth = 3.dp)
                            Spacer(Modifier.height(14.dp))
                            Text("Cargando ruleta...", color = Color.White.copy(alpha = 0.9f))
                        }
                    } else {
                        RouletteCanvas(
                            layout = layout!!,
                            rotorAngleDeg = rotorAngleDegState,
                            ballImagePx = ballPosState,
                            testBallPos = null,
                            onSelectPocket = {},
                            onSelectAnnulus = {},
                            manualRotationEnabled = false,
                            onManualRotateDelta = {},
                            onTapImage = { _, _ -> },
                            ballZ = ballZState
                        )
                    }
                }

                // Panel lateral de apuestas
                BetsSidebar(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    apuestas = apuestas.value
                )
            }

            // Overlay animado de resultado
            AnimatedVisibility(
                visible = showResultOverlay && winnerNumber != null,
                enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.98f, animationSpec = tween(250)),
                modifier = Modifier.fillMaxSize()
            ) {
                val num = winnerNumber ?: 0
                val colorNum = numberColor(num)
                WinnerOverlay(
                    number = num,
                    color = colorNum,
                    winAmount = winAmount,
                    totalBet = totalBet,
                    net = netResult,
                    onDismiss = { showResultOverlay = false }
                )
            }
        }
    }
}

// Helpers
private fun Context.readTextFromUri(uri: Uri): String {
    contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "No se pudo abrir el archivo" }
        return input.bufferedReader(Charsets.UTF_8).readText()
    }
}
private fun Context.readTextFromRaw(resId: Int): String {
    resources.openRawResource(resId).use { input -> return input.bufferedReader(Charsets.UTF_8).readText() }
}
private fun setFieldIfExists(target: Any, fieldName: String, value: Any) {
    runCatching {
        val f = target::class.java.getDeclaredField(fieldName)
        f.isAccessible = true
        val v = when (f.type) {
            Int::class.javaPrimitiveType, Int::class.java -> (value as Number).toInt()
            Float::class.javaPrimitiveType, Float::class.java -> (value as Number).toFloat()
            Double::class.javaPrimitiveType, Double::class.java -> (value as Number).toDouble()
            Long::class.javaPrimitiveType, Long::class.java -> (value as Number).toLong()
            Boolean::class.javaPrimitiveType, Boolean::class.java -> value as Boolean
            else -> value
        }
        f.set(target, v)
    }
}
private fun callMethodIfExists(target: Any, methodName: String) {
    runCatching { val m = target::class.java.getDeclaredMethod(methodName); m.isAccessible = true; m.invoke(target) }
}

// --------- UI de resultado ganador ---------
@Composable
private fun WinnerOverlay(
    number: Int,
    color: Color,
    winAmount: Int,
    totalBet: Int,
    net: Int,
    onDismiss: () -> Unit
) {
    // Fondo con degradado y sutiles rayos
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x33000000), Color(0x99000000), Color(0xCC000000)),
                    radius = 900f
                )
            )
            .clickable { onDismiss() }
    ) {
        // Confeti ligero
        ConfettiLayer()

        // Tarjeta central
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111).copy(alpha = 0.94f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NÚMERO GANADOR", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                // Insignia circular del número
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer { shadowElevation = 12f }
                        .background(color.copy(alpha = 0.95f), shape = MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center
                ) {
                    val ringAlpha by rememberInfiniteFloat(0.65f, 0.95f, 1400)
                    // anillo
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f - 6f
                        drawCircle(color = Color.White.copy(alpha = 0.22f), radius = r)
                        drawCircle(color = Color.White.copy(alpha = ringAlpha), radius = r, style = Stroke(width = 6f))
                    }
                    Text(
                        text = number.toString(),
                        color = if (isBright(color)) Color.Black else Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                val positive = net >= 0
                val label = if (positive) "Ganancia" else "Pérdida"
                val valueTxt = (if (positive) "+" else "-") + kotlin.math.abs(net).toString()
                Text(
                    "$label: $valueTxt",
                    color = if (positive) Color(0xFF69F0AE) else Color(0xFFFF8A80),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Apostado: ${totalBet} • Premio: ${winAmount}",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text("Toca para continuar", color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ConfettiLayer() {
    val colors = listOf(
        Color(0xFFFFC107), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF03A9F4), Color(0xFFE91E63)
    )
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1800, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "t"
    )
    // Dibujamos unos puntos que caen
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w: Float = size.width
        val h: Float = size.height
        val n: Int = 24
        for (i in 0 until n) {
            val phase: Float = i.toFloat() / n.toFloat()
            val yy: Float = h * ((t + phase) % 1f)
            val xx: Float = w * ((((phase * 3.137f) + (t * 0.6f)) % 1f))
            val r: Float = 3f + 4f * (((i % 5).toFloat()) / 4f)
            drawCircle(colors[i % colors.size].copy(alpha = 0.65f), radius = r, center = androidx.compose.ui.geometry.Offset(xx, yy))
        }
    }
}

@Composable
private fun rememberInfiniteFloat(start: Float, end: Float, durationMs: Int): State<Float> {
    val transition = rememberInfiniteTransition(label = "pulse")
    return transition.animateFloat(
        initialValue = start,
        targetValue = end,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = durationMs, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "pulseVal"
    )
}

private fun numberColor(n: Int): Color {
    if (n == 0) return Color(0xFF00C853) // verde
    val reds = setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)
    return if (reds.contains(n)) Color(0xFFD32F2F) else Color(0xFF212121)
}

// Determina si un color es "claro" usando luminancia relativa sRGB
private fun isBright(c: Color): Boolean {
    val argb = c.toArgb()
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return lum > 0.5f
}

// --- Mapeo ganador visual y cálculo de premio ---
private fun computeVisualWinner(layout: RouletteLayoutJson, ballPos: Pair<Float, Float>, rotorAngleDeg: Float): Int? {
    val (bx, by) = ballPos
    val cx = layout.centerPx.cx
    val cy = layout.centerPx.cy
    val tx = bx - cx
    val ty = cy - by
    val angleWorld = atan2(ty, tx)
    val rotorAngleRad = rotorAngleDeg * PI.toFloat() / 180f
    val angleLocal = angleWorld + rotorAngleRad
    val tau = (2f * PI.toFloat())
    val angleLocalNorm = ((angleLocal + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
    return layout.upper.minByOrNull { pocket ->
        var d = angleLocalNorm - pocket.thetaCenter
        val twoPi = tau
        d = ((d + PI.toFloat()) % twoPi + twoPi) % twoPi - PI.toFloat()
        kotlin.math.abs(d)
    }?.number
}

private fun calculateWinForNumber(apuestas: List<Apuesta>, n: Int): Int {
    return apuestas.filter { it.numero == n }.sumOf { it.valorMoneda * 36 }
}

// =================== Panel lateral de Apuestas ===================
@Composable
private fun BetsSidebar(modifier: Modifier = Modifier, apuestas: List<Apuesta>) {
    // Agrupar por número y sumar valor
    val grouped = remember(apuestas) {
        apuestas.groupBy { it.numero }
            .mapValues { entry -> entry.value.sumOf { it.valorMoneda } }
            .toList()
            .sortedBy { it.first }
    }

    // Fondo con sutil degradado vertical
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF0C0C0C),
                    1f to Color(0xFF101010)
                )
            )
            .padding(vertical = 8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Cabecera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apuestas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Total general
                    val total = grouped.sumOf { it.second }
                    Text(
                        text = "Total: $total",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFFC107)
                    )
                }

                HorizontalDivider(color = Color(0x22FFFFFF))

                if (grouped.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Sin apuestas",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(grouped, key = { it.first }) { (numero, totalNumero) ->
                            BetItem(numero = numero, totalNumero = totalNumero)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BetItem(numero: Int, totalNumero: Int) {
    val colorNum = numberColor(numero)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge del número
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(colorNum),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = if (isBright(colorNum)) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nº $numero",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Total: $totalNumero",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                // Fila de monedas
                Spacer(Modifier.height(6.dp))
                CoinRow(amount = totalNumero)
            }
        }
    }
}

@Composable
private fun CoinRow(amount: Int) {
    val breakdown = remember(amount) { coinBreakdown(amount) }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        breakdown.forEach { (denom, count) ->
            CoinIcon(denom = denom, count = count)
        }
    }
}

@Composable
private fun CoinIcon(denom: Int, count: Int) {
    if (count <= 0) return
    val resId = when (denom) {
        1 -> R.drawable.coin1
        5 -> R.drawable.coin5
        10 -> R.drawable.coin10
        20 -> R.drawable.coin20
        50 -> R.drawable.coin50
        100 -> R.drawable.coin100
        else -> R.drawable.coin1
    }
    Box(contentAlignment = Alignment.TopEnd) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Moneda de $denom",
            modifier = Modifier.size(26.dp)
        )
        // Badge con cantidad
        if (count > 1) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFF222222))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "x$count",
                    color = Color(0xFFFFC107),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun coinBreakdown(amount: Int): List<Pair<Int, Int>> {
    if (amount <= 0) return emptyList()
    val denoms = intArrayOf(100, 50, 20, 10, 5, 1)
    var rest = amount
    val out = mutableListOf<Pair<Int, Int>>()
    for (d in denoms) {
        val c = rest / d
        if (c > 0) out += d to c
        rest %= d
        if (rest == 0) break
    }
    return out
}

// =================== Panel lateral de Últimos Números ===================
@Composable
private fun ResultsSidebar(modifier: Modifier = Modifier, results: List<Int>) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF0C0C0C),
                    1f to Color(0xFF101010)
                )
            )
            .padding(vertical = 8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimos",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = Color(0x22FFFFFF))

                if (results.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("—", color = Color.White.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { n ->
                            ResultBadge(number = n)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultBadge(number: Int) {
    val color = numberColor(number)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (isBright(color)) Color.Black else Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Nº $number", color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
    }
}

// =================== Fondo Casino ===================
@Composable
private fun CasinoBackground(modifier: Modifier = Modifier) {
    // Capa base: color de tapete verde
    Box(modifier = modifier.background(Color(0xFF155E2C))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.5f)

            // Highlight radial suave (centro más luminoso)
            fun radialHighlight(cx: Float, cy: Float, radius: Float, color: Color, steps: Int = 6) {
                for (i in steps downTo 1) {
                    val r = radius * (i / steps.toFloat())
                    val a = (0.14f * (i / steps.toFloat())).coerceAtLeast(0.02f)
                    drawCircle(color.copy(alpha = a), radius = r, center = Offset(cx, cy))
                }
            }
            radialHighlight(center.x, center.y, radius = min(w, h) * 0.60f, color = Color(0xFF1DB954))

            // Rayado diagonal muy sutil para simular fibra del tapete
            val step = 26f
            val strokeW = 1f
            val stripe = Color.White.copy(alpha = 0.025f)
            var x = -h
            while (x < w + h) {
                drawLine(
                    color = stripe,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset((x - h).toFloat(), h),
                    strokeWidth = strokeW
                )
                x += step.toInt()
            }

            // Pequeñas motas (speckle) discretas
            val dotColor = Color.White.copy(alpha = 0.018f)
            val cols = 22
            val rows = 14
            for (ci in 0 until cols) {
                for (ri in 0 until rows) {
                    val seed = (ci * 73856093) xor (ri * 19349663)
                    val rx = ((seed % 1000) / 1000f)
                    val ry = (((seed / 1000) % 1000) / 1000f)
                    val px = (ci + rx) * (w / cols)
                    val py = (ri + ry) * (h / rows)
                    drawCircle(dotColor, radius = 0.9f, center = Offset(px, py))
                }
            }
        }
    }
}
