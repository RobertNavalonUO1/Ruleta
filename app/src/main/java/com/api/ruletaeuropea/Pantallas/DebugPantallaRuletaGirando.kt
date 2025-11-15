package com.api.ruletaeuropea.pantallas

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.entity.Jugador
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pantalla de depuración completa: muestra overlay de sliders, botones adicionales
 * (rotación manual, bola de prueba, guardar parámetros) y anuncio de ganador visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugPantallaRuletaGirando(
    navController: NavHostController,
    jugador: Jugador,
    apuestas: MutableState<List<Apuesta>>,
    onActualizarSaldo: (Int) -> Unit
) {
    val ctx = LocalContext.current
    var layout by remember { mutableStateOf<RouletteLayoutJson?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var rawJson by remember { mutableStateOf<String?>(null) }
    var selectedPocket by remember { mutableStateOf<Pocket?>(null) }
    var selectedAnnulus by remember { mutableStateOf<Annulus?>(null) }

    val physics = remember(rawJson) { rawJson?.let { runCatching { com.api.ruletaeuropea.fisica.RoulettePhysics(JSONObject(it)) }.getOrNull() } }
    var rolling by remember { mutableStateOf(false) }
    var ganador by remember { mutableStateOf<Int?>(null) }
    var ganadorFisica by remember { mutableStateOf<Int?>(null) }

    var rotorAngleDegState by remember { mutableStateOf(0f) }
    var ballPosState by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var ballZState by remember { mutableStateOf<Float>(0f) }

    var manualRotacionEnabled by rememberSaveable { mutableStateOf(false) }
    var manualRotorAngleDeg by rememberSaveable { mutableStateOf(0f) }

    var testBallPos by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var testBallDetectedPocket by remember { mutableStateOf<Pocket?>(null) }
    var testBallAngleDeg by rememberSaveable { mutableStateOf(0f) }
    var showTestBallDialog by remember { mutableStateOf(false) }
    var lastTapImagePos by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showSaveDialog by remember { mutableStateOf<String?>(null) }

    // Carga layout
    LaunchedEffect(Unit) {
        runCatching { ctx.readTextFromRaw(com.api.ruletaeuropea.R.raw.ruleta_layout_extended) }
            .onSuccess { txt ->
                rawJson = txt
                runCatching { parseRouletteLayout(txt) }
                    .onSuccess { layout = it; errorMsg = null }
                    .onFailure { e -> errorMsg = "No se pudo leer el layout: ${e.message}" }
            }
            .onFailure { e -> errorMsg = "No se pudo abrir el layout: ${e.message}" }
    }

    // Bucle física
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
                val numeroMotor = engine.resultPocketNumber()
                ganadorFisica = numeroMotor
                val layoutActual = layout
                val ballFinal = engine.ballPosition()
                val rotorFinal = engine.rouletteAngleDeg
                val numeroVisual = layoutActual?.let { ly ->
                    val (bx, by) = ballFinal
                    val cx = ly.centerPx.cx; val cy = ly.centerPx.cy
                    val tx = bx - cx; val ty = cy - by
                    val angleWorld = atan2(ty, tx)
                    val rotorAngleRad = rotorFinal * PI.toFloat() / 180f
                    val angleLocal = angleWorld + rotorAngleRad
                    val tau = (2f * PI.toFloat())
                    val angleLocalNorm = ((angleLocal + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
                    ly.upper.minByOrNull { pocket ->
                        var d = angleLocalNorm - pocket.thetaCenter
                        d = ((d + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
                        kotlin.math.abs(d)
                    }?.number
                }
                ganador = numeroVisual ?: numeroMotor
                val win = engine.calculateWin(apuestas.value)
                if (win > 0) onActualizarSaldo(win)
            }
        }
    }

    // Estado overlay y parámetros
    var showOverlay by rememberSaveable { mutableStateOf(true) }
    var wMinDeg by rememberSaveable { mutableStateOf(1800f) }
    var wMaxDeg by rememberSaveable { mutableStateOf(3200f) }
    var rotorMinDeg by rememberSaveable { mutableStateOf(600f) }
    var rotorMaxDeg by rememberSaveable { mutableStateOf(900f) }
    var rVRange by rememberSaveable { mutableStateOf(400f) }
    var rVBase by rememberSaveable { mutableStateOf(300f) }
    var speedMul by rememberSaveable { mutableStateOf(2.0f) }
    var baseAngularFriction by rememberSaveable { mutableStateOf(0.9988f) }
    var airRes by rememberSaveable { mutableStateOf(0.00030f) }
    var ballScaleVisual by rememberSaveable { mutableStateOf(1.0f) }
    var boardBounceElastic by rememberSaveable { mutableStateOf(0.15f) }
    var boardBounceFric by rememberSaveable { mutableStateOf(0.8f) }
    var wallBumpWidthDegUi by rememberSaveable { mutableStateOf(0.6f) }
    var wallBumpTorqueUi by rememberSaveable { mutableStateOf(10f) }
    var gravityZUi by rememberSaveable { mutableStateOf(1500f) }
    var ballZScaleKUi by rememberSaveable { mutableStateOf(0.006f) }
    var maxBallZUi by rememberSaveable { mutableStateOf(8f) }

    // Aplicar parámetros al motor
    LaunchedEffect(physics, wMinDeg, wMaxDeg, rotorMinDeg, rotorMaxDeg, rVBase, rVRange, speedMul, baseAngularFriction, airRes, ballScaleVisual, boardBounceElastic, boardBounceFric, wallBumpWidthDegUi, wallBumpTorqueUi, gravityZUi, ballZScaleKUi) {
        val p = physics ?: return@LaunchedEffect
        setFieldIfExists(p, "ballMinDegInit", wMinDeg.toInt())
        setFieldIfExists(p, "ballMaxDegInit", wMaxDeg.toInt())
        setFieldIfExists(p, "wMinDegInit", wMinDeg.toInt())
        setFieldIfExists(p, "wMaxDegInit", wMaxDeg.toInt())
        setFieldIfExists(p, "rotorMinDegInit", rotorMinDeg.toInt())
        setFieldIfExists(p, "rotorMaxDegInit", rotorMaxDeg.toInt())
        setFieldIfExists(p, "rVBaseInit", rVBase)
        setFieldIfExists(p, "rVRangeInit", rVRange)
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
                title = { Text("Ruleta (Debug)", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0B0B),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = { Text("💰 ${jugador.NumMonedas}", modifier = Modifier.padding(end = 12.dp), color = Color(0xFFF1F1F1)) }
            )
        },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallFloatingActionButton(onClick = { showOverlay = !showOverlay }, containerColor = Color(0xFF222222), contentColor = amber, modifier = Modifier.padding(end = 10.dp)) { Text(if (showOverlay) "Ocultar" else "Config", fontSize = 12.sp) }
                SmallFloatingActionButton(onClick = { if (!rolling) manualRotacionEnabled = !manualRotacionEnabled }, containerColor = if (manualRotacionEnabled) amber.copy(alpha = 0.18f) else Color(0xFF222222), contentColor = if (manualRotacionEnabled) amber else Color(0xFFE0E0E0), modifier = Modifier.padding(end = 10.dp)) { Text(if (manualRotacionEnabled) "Rot. ON" else "Rotación", fontSize = 11.sp) }
                SmallFloatingActionButton(onClick = { if (!rolling) { if (manualRotacionEnabled) manualRotorAngleDeg = 0f else rotorAngleDegState = 0f } }, containerColor = Color(0xFF222222), contentColor = amber, modifier = Modifier.padding(end = 10.dp)) { Text("Reset rot", fontSize = 11.sp) }
                SmallFloatingActionButton(onClick = {
                    val ly = layout; val tap = lastTapImagePos
                    if (ly != null && tap != null) {
                        val (xi, yi) = tap
                        val cx = ly.centerPx.cx; val cy = ly.centerPx.cy
                        val tx = xi - cx; val ty = cy - yi
                        val rad = atan2(ty, tx)
                        val r = (ly.ringsPx.track_inner_r + ly.ringsPx.track_outer_r) / 2f
                        val bx = cx + r * kotlin.math.cos(rad)
                        val by = cy - r * kotlin.math.sin(rad)
                        testBallPos = bx to by
                        val rotorDegActual = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState
                        testBallDetectedPocket = ly.upper.minByOrNull { pocket ->
                            val rotorAngleRad = rotorDegActual * PI.toFloat() / 180f
                            val angleWorld = rad
                            val angleLocal = angleWorld + rotorAngleRad
                            val tau = (2f * PI.toFloat())
                            val angleLocalNorm = ((angleLocal + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
                            var d = angleLocalNorm - pocket.thetaCenter
                            d = ((d + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
                            kotlin.math.abs(d)
                        }
                        testBallAngleDeg = ((rad * 180f / PI.toFloat()).let { if (it < 0f) it + 360f else it })
                        showTestBallDialog = true
                    }
                }, containerColor = if (lastTapImagePos != null) amber else Color(0xFF222222), contentColor = if (lastTapImagePos != null) Color.Black else Color(0xFFE0E0E0), modifier = Modifier.padding(end = 10.dp)) { Text("Bola tap", fontSize = 11.sp) }
                FloatingActionButton(onClick = {
                    physics?.let {
                        setFieldIfExists(it, "ballMinDegInit", wMinDeg.toInt())
                        setFieldIfExists(it, "ballMaxDegInit", wMaxDeg.toInt())
                        setFieldIfExists(it, "wMinDegInit", wMinDeg.toInt())
                        setFieldIfExists(it, "wMaxDegInit", wMaxDeg.toInt())
                        setFieldIfExists(it, "rotorMinDegInit", rotorMinDeg.toInt())
                        setFieldIfExists(it, "rotorMaxDegInit", rotorMaxDeg.toInt())
                        setFieldIfExists(it, "rVBaseInit", rVBase)
                        setFieldIfExists(it, "rVRangeInit", rVRange)
                        setFieldIfExists(it, "speedMultiplier", speedMul)
                        setFieldIfExists(it, "timeScale", speedMul)
                        runCatching { it.cfg = it.cfg.copy(baseAngularFriction = baseAngularFriction, airRes = airRes) }
                        callMethodIfExists(it, "recomputeDerived")
                        it.reset(); it.launchBall(); ganador = null; rolling = true
                        selectedPocket = null; selectedAnnulus = null; manualRotacionEnabled = false
                    }
                }, containerColor = if (rolling) Color(0xFF303030) else amber, contentColor = if (rolling) Color(0xFFBDBDBD) else Color.Black) { Text(if (rolling) "Girando…" else "Lanzar bola") }
            }
        }
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).background(Color.Black)) {
            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                if (layout == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = amber, strokeWidth = 3.dp)
                        Spacer(Modifier.height(14.dp))
                        Text("Cargando ruleta...", color = Color.White.copy(alpha = 0.9f))
                    }
                } else {
                    RouletteCanvas(
                        layout = layout!!,
                        rotorAngleDeg = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState,
                        ballImagePx = ballPosState,
                        testBallPos = testBallPos,
                        onSelectPocket = { pk -> selectedPocket = pk; selectedAnnulus = null },
                        onSelectAnnulus = { ann -> selectedPocket = null; selectedAnnulus = ann },
                        manualRotationEnabled = manualRotacionEnabled && !rolling,
                        onManualRotateDelta = { deltaDeg ->
                            manualRotorAngleDeg = (manualRotorAngleDeg + deltaDeg) % 360f
                            if (manualRotorAngleDeg < 0f) manualRotorAngleDeg += 360f
                        },
                        onTapImage = { xi, yi -> lastTapImagePos = xi to yi },
                        ballZ = ballZState
                    )
                }

                // Anuncio ganador / selección (solo debug)
                val labelText: String? = when {
                    ganador != null -> {
                        val gVis = ganador; val gFis = ganadorFisica
                        if (gVis != null && gFis != null && gVis != gFis) "Ganador visual: $gVis (motor: $gFis)" else "Ganador: $gVis"
                    }
                    selectedPocket != null -> {
                        val pk = selectedPocket!!
                        val prefix = when {
                            layout?.upper?.any { it === pk } == true -> "S"
                            layout?.middle?.any { it === pk } == true -> "M"
                            else -> "INF"
                        }
                        val idx = pk.index_clockwise + 1
                        "%s%02d: %s • idx %d".format(prefix, idx, if (pk.number >= 0) pk.number else "—", idx)
                    }
                    selectedAnnulus == Annulus.OUTER_RING -> "Círculo exterior"
                    selectedAnnulus == Annulus.INNER_HUB -> "Círculo interior"
                    else -> null
                }
                val showLabel = labelText != null
                val alpha by animateFloatAsState(targetValue = if (showLabel) 1f else 0f, animationSpec = tween(220, easing = LinearOutSlowInEasing), label = "labelAlpha")
                val offsetY by animateDpAsState(targetValue = if (showLabel) 0.dp else (-12).dp, animationSpec = tween(220, easing = LinearOutSlowInEasing), label = "labelOffset")
                if (alpha > 0.01f) ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.94f)), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp), modifier = Modifier.align(if (ganador != null) Alignment.BottomCenter else Alignment.TopCenter).padding(if (ganador != null) 16.dp else 12.dp).graphicsLayer { this.alpha = alpha; this.translationY = if (ganador != null) -offsetY.toPx() else offsetY.toPx() }) { Text(labelText ?: "", modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp), color = if (ganador != null) amber else Color.White) }

                // Overlay debug
                if (showOverlay) {
                    ConfigOverlayBetter(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        amber = amber,
                        ballMin = wMinDeg,
                        ballMax = wMaxDeg,
                        onBallMinChange = { wMinDeg = min(it, wMaxDeg) },
                        onBallMaxChange = { wMaxDeg = max(it, wMinDeg) },
                        rotorMin = rotorMinDeg,
                        rotorMax = rotorMaxDeg,
                        onRotorMinChange = { rotorMinDeg = min(it, rotorMaxDeg) },
                        onRotorMaxChange = { rotorMaxDeg = max(it, rotorMinDeg) },
                        rVBase = rVBase,
                        rVRange = rVRange,
                        onRVBaseChange = { rVBase = it },
                        onRVRangeChange = { rVRange = it },
                        speedMul = speedMul,
                        onSpeedMulChange = { speedMul = it },
                        baseAngularFriction = baseAngularFriction,
                        airRes = airRes,
                        onBaseFricChange = { baseAngularFriction = it.coerceIn(0.95f, 0.9999f) },
                        onAirResChange = { airRes = it },
                        ballScale = ballScaleVisual,
                        onBallScaleChange = { ballScaleVisual = it },
                        boardBounceElastic = boardBounceElastic,
                        onBoardBounceElasticChange = { boardBounceElastic = it },
                        boardBounceFric = boardBounceFric,
                        onBoardBounceFricChange = { boardBounceFric = it },
                        wallBumpWidthDeg = wallBumpWidthDegUi,
                        onWallBumpWidthChange = { wallBumpWidthDegUi = it },
                        wallBumpTorque = wallBumpTorqueUi,
                        onWallBumpTorqueChange = { wallBumpTorqueUi = it },
                        gravityZ = gravityZUi,
                        onGravityZChange = { gravityZUi = it },
                        ballZScaleK = ballZScaleKUi,
                        onBallZScaleKChange = { ballZScaleKUi = it },
                        onReset = {
                            wMinDeg = 1800f; wMaxDeg = 3200f; rotorMinDeg = 600f; rotorMaxDeg = 900f
                            rVBase = 300f; rVRange = 400f; speedMul = 2.0f; baseAngularFriction = 0.9988f; airRes = 0.00030f
                            maxBallZUi = 8f
                        },
                        onSave = {
                            val ts = System.currentTimeMillis()
                            val fileName = "ruleta_params_${ts}.txt"
                            val text = buildString {
                                appendLine("# Parámetros ruleta (debug ${ts})")
                                appendLine("ballMinDeg=${wMinDeg}")
                                appendLine("ballMaxDeg=${wMaxDeg}")
                                appendLine("rotorMinDeg=${rotorMinDeg}")
                                appendLine("rotorMaxDeg=${rotorMaxDeg}")
                                appendLine("rVBase=${rVBase}")
                                appendLine("rVRange=${rVRange}")
                                appendLine("speedMul=${speedMul}")
                                appendLine("baseAngularFriction=${baseAngularFriction}")
                                appendLine("airRes=${airRes}")
                                appendLine("ballScaleVisual=${ballScaleVisual}")
                                appendLine("boardBounceElastic=${boardBounceElastic}")
                                appendLine("boardBounceFric=${boardBounceFric}")
                                appendLine("wallBumpWidthDeg=${wallBumpWidthDegUi}")
                                appendLine("wallBumpTorque=${wallBumpTorqueUi}")
                                appendLine("gravityZ=${gravityZUi}")
                                appendLine("ballZScaleK=${ballZScaleKUi}")
                                appendLine("maxBallZUi=${maxBallZUi}")
                                appendLine("manualRotorAngleDeg=${manualRotorAngleDeg}")
                                appendLine("rotorAngleDegState=${rotorAngleDegState}")
                                appendLine("ganador=${ganador}")
                            }
                            runCatching {
                                val f = ctx.getExternalFilesDir(null)?.resolve(fileName)
                                if (f != null) { f.writeText(text, Charsets.UTF_8); showSaveDialog = f.absolutePath }
                            }.onFailure { e -> showSaveDialog = "Error: ${e.message}" }
                        }
                    )
                }

                if (showSaveDialog != null) {
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = null },
                        confirmButton = { TextButton(onClick = { showSaveDialog = null }) { Text("Cerrar", color = amber) } },
                        title = { Text("Parámetros guardados", color = amber) },
                        text = { Text("Archivo: ${showSaveDialog}", color = Color.White) },
                        containerColor = Color(0xFF1A1A1A)
                    )
                }

                if (showTestBallDialog && testBallPos != null) {
                    AlertDialog(
                        onDismissRequest = { showTestBallDialog = false },
                        confirmButton = { TextButton(onClick = { showTestBallDialog = false }) { Text("Cerrar", color = amber) } },
                        title = { Text("Bola de prueba", color = amber) },
                        text = {
                            val (bx, by) = testBallPos!!
                            val pocketTxt = when (val pk = testBallDetectedPocket) {
                                null -> "(No detectado)"
                                else -> "Pocket: ${pk.number} (idx ${pk.index_clockwise + 1})"
                            }
                            Column {
                                Text("Posición px: x=${"%.1f".format(bx)}, y=${"%.1f".format(by)}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                Text(pocketTxt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                val rotorShown = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState
                                Text("Rotor: ${"%.1f".format(rotorShown)}°", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                            }
                        },
                        containerColor = Color(0xFF1A1A1A), tonalElevation = 6.dp
                    )
                }
            }
        }
    }
}

// Helpers IO/reflexión (duplicados para independencia del archivo principal)
private fun Context.readTextFromUri(uri: Uri): String { contentResolver.openInputStream(uri).use { input -> requireNotNull(input) { "No se pudo abrir el archivo" }; return input.bufferedReader(Charsets.UTF_8).readText() } }
private fun Context.readTextFromRaw(resId: Int): String { resources.openRawResource(resId).use { input -> return input.bufferedReader(Charsets.UTF_8).readText() } }
private fun setFieldIfExists(target: Any, fieldName: String, value: Any) { runCatching { val f = target::class.java.getDeclaredField(fieldName); f.isAccessible = true; val v = when (f.type) { Int::class.javaPrimitiveType, Int::class.java -> (value as Number).toInt(); Float::class.javaPrimitiveType, Float::class.java -> (value as Number).toFloat(); Double::class.javaPrimitiveType, Double::class.java -> (value as Number).toDouble(); Long::class.javaPrimitiveType, Long::class.java -> (value as Number).toLong(); Boolean::class.javaPrimitiveType, Boolean::class.java -> value as Boolean; else -> value }; f.set(target, v) } }
private fun callMethodIfExists(target: Any, methodName: String) { runCatching { val m = target::class.java.getDeclaredMethod(methodName); m.isAccessible = true; m.invoke(target) } }

