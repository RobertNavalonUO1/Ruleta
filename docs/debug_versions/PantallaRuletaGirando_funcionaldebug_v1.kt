// Backup funcionaldebug v1 - copia del archivo PantallaRuletaGirando.kt en estado estable
// Fecha: 2025-11-11
// NOTA: Este archivo está fuera del árbol de compilación (en docs/) para evitar duplicados.

package com.api.ruletaeuropea.pantallas

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.entity.Jugador
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

// -------------------- Modelos --------------------

data class ImageSize(val width: Float, val height: Float)
data class CenterPx(val cx: Float, val cy: Float)
data class RingsPx(
    val outer_r: Float,
    val track_outer_r: Float,
    val track_inner_r: Float,
    val cone_outer_r: Float,
    val cone_inner_r: Float,
    val hub_r: Float
)
data class Pocket(
    val index_clockwise: Int,
    val number: Int,
    val color: String,
    val thetaCenter: Float,
    val thetaStart: Float,
    val thetaEnd: Float,
    val centerX: Float,
    val centerY: Float,
    val polygon: List<Offset>,
    val id1: String = "",
    val id2: String = ""
)
data class RouletteLayoutJson(
    val imageSize: ImageSize,
    val centerPx: CenterPx,
    val ringsPx: RingsPx,
    val separators: List<Float>,
    val upper: List<Pocket>,
    val middle: List<Pocket>,
    val lower: List<Pocket>
)

// Zonas no segmentadas
enum class Annulus { OUTER_RING, INNER_HUB }

// -------------------- Parseo JSON --------------------

private fun JSONArray.toFloatList(): List<Float> =
    (0 until length()).map { getDouble(it).toFloat() }

private fun readPolygon(arr: JSONArray): List<Offset> {
    val out = ArrayList<Offset>(arr.length())
    for (i in 0 until arr.length()) {
        val p = arr.getJSONArray(i)
        out += Offset(p.getDouble(0).toFloat(), p.getDouble(1).toFloat())
    }
    return out
}

private fun readPockets(arr: JSONArray, hasColor: Boolean, id1: String, id2: String): List<Pocket> {
    val out = ArrayList<Pocket>(arr.length())
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        out += Pocket(
            index_clockwise = o.getInt("index_clockwise"),
            number = o.optInt("number", -1),
            color = if (hasColor) o.getString("color") else "",
            thetaCenter = o.getDouble("theta_center_rad").toFloat(),
            thetaStart = o.getDouble("theta_start_rad").toFloat(),
            thetaEnd = o.getDouble("theta_end_rad").toFloat(),
            centerX = o.getDouble("center_x").toFloat(),
            centerY = o.getDouble("center_y").toFloat(),
            polygon = readPolygon(o.getJSONArray("polygon")),
            id1 = o.optString(id1, ""),
            id2 = o.optString(id2, "")
        )
    }
    return out
}

private fun parseRouletteLayout(json: String): RouletteLayoutJson {
    val root = JSONObject(json)
    val img = root.getJSONObject("image_size")
    val center = root.getJSONObject("center_px")
    val rings = root.getJSONObject("rings_px")
    val imageSize = ImageSize(img.getInt("width").toFloat(), img.getInt("height").toFloat())
    val centerPx = CenterPx(center.getDouble("cx").toFloat(), center.getDouble("cy").toFloat())
    val ringsPx = RingsPx(
        outer_r = rings.getDouble("outer_r").toFloat(),
        track_outer_r = rings.getDouble("track_outer_r").toFloat(),
        track_inner_r = rings.getDouble("track_inner_r").toFloat(),
        cone_outer_r = rings.getDouble("cone_outer_r").toFloat(),
        cone_inner_r = rings.getDouble("cone_inner_r").toFloat(),
        hub_r = rings.getDouble("hub_r").toFloat()
    )
    val seps = root.getJSONArray("separators_theta_rad_clockwise").toFloatList()
    val upper = readPockets(root.getJSONArray("upper_pockets"), true, "id_s1", "id_s2")
    val middle = readPockets(root.getJSONArray("middle_pockets"), false, "id_m1", "id_m2")
    val lower = readPockets(root.getJSONArray("lower_pockets"), false, "id_inf1", "id_inf2")
    return RouletteLayoutJson(imageSize, centerPx, ringsPx, seps, upper, middle, lower)
}

// -------------------- Utils de dibujo --------------------

private fun DrawScope.drawSubtleGuides(cx: Float, cy: Float, rings: RingsPx, scale: Float) {
    val stroke = Stroke(width = 1f)
    val c = Color.White.copy(alpha = 0.08f)
    drawCircle(c, rings.outer_r * scale, Offset(cx, cy), stroke)
    drawCircle(c, rings.track_outer_r * scale, Offset(cx, cy), stroke)
    drawCircle(c, rings.track_inner_r * scale, Offset(cx, cy), stroke)
    drawCircle(c, rings.cone_outer_r * scale, Offset(cx, cy), stroke)
    drawCircle(c, rings.cone_inner_r * scale, Offset(cx, cy), stroke)
    drawCircle(c, rings.hub_r * scale, Offset(cx, cy), stroke)
}

private fun angDiffAbs(a: Float, b: Float): Float {
    var d = a - b
    val tau = (2f * Math.PI).toFloat()
    d = ((d + Math.PI.toFloat()) % tau + tau) % tau - Math.PI.toFloat()
    return abs(d)
}

// -------------------- UI principal --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRuletaGirando(
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
    var manualRotacionEnabled by rememberSaveable { mutableStateOf(false) }
    var manualRotorAngleDeg by rememberSaveable { mutableStateOf(0f) }
    var testBallPos by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var testBallDetectedPocket by remember { mutableStateOf<Pocket?>(null) }
    var testBallAngleDeg by rememberSaveable { mutableStateOf(0f) }
    var showTestBallDialog by remember { mutableStateOf(false) }
    var lastTapImagePos by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    fun detectarPocket(layout: RouletteLayoutJson, rotorAngleDeg: Float, ballPos: Pair<Float, Float>): Pocket? {
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
        return layout.upper.minByOrNull { angDiffAbs(angleLocalNorm, it.thetaCenter) }
    }
    LaunchedEffect(Unit) {
        runCatching { ctx.readTextFromRaw(com.api.ruletaeuropea.R.raw.ruleta_layout_extended) }
            .onSuccess { txt -> rawJson = txt; runCatching { parseRouletteLayout(txt) }.onSuccess { layout = it }.onFailure { errorMsg = it.message } }
            .onFailure { errorMsg = it.message }
    }
    LaunchedEffect(rolling, physics) {
        val engine = physics ?: return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            if (!rolling) break
            engine.updatePhysics()
            rotorAngleDegState = engine.rouletteAngleDeg
            ballPosState = engine.ballPosition()
            if (!engine.rolling) {
                rolling = false
                val numeroMotor = engine.resultPocketNumber()
                ganadorFisica = numeroMotor
                val layoutActual = layout
                val ballFinal = engine.ballPosition()
                val rotorFinal = engine.rouletteAngleDeg
                val numeroVisual = layoutActual?.let { detectarPocket(it, rotorFinal, ballFinal)?.number }
                ganador = numeroVisual ?: numeroMotor
                val win = engine.calculateWin(apuestas.value)
                if (win > 0) onActualizarSaldo(win)
            }
        }
    }
    var showOverlay by rememberSaveable { mutableStateOf(true) }
    var wMinDeg by rememberSaveable { mutableStateOf(1200f) }
    var wMaxDeg by rememberSaveable { mutableStateOf(2200f) }
    var rotorMinDeg by rememberSaveable { mutableStateOf(300f) }
    var rotorMaxDeg by rememberSaveable { mutableStateOf(500f) }
    var rVRange by rememberSaveable { mutableStateOf(260f) }
    var rVBase by rememberSaveable { mutableStateOf(220f) }
    var speedMul by rememberSaveable { mutableStateOf(1.0f) }
    var baseAngularFriction by rememberSaveable { mutableStateOf(0.9930f) }
    var airRes by rememberSaveable { mutableStateOf(0.00055f) }
    LaunchedEffect(physics, wMinDeg, wMaxDeg, rotorMinDeg, rotorMaxDeg, rVBase, rVRange, speedMul, baseAngularFriction, airRes) {
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
        topBar = { TopAppBar(title = { Text("Ruleta europea", fontWeight = FontWeight.Bold, color = Color.White) }) },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallFloatingActionButton(onClick = { showOverlay = !showOverlay }, containerColor = Color(0xFF222222), contentColor = amber) { Text(if (showOverlay) "Ocultar" else "Config", fontSize = 12.sp) }
                SmallFloatingActionButton(onClick = { if (!rolling) manualRotacionEnabled = !manualRotacionEnabled }, containerColor = if (manualRotacionEnabled) amber.copy(alpha = 0.18f) else Color(0xFF222222), contentColor = if (manualRotacionEnabled) amber else Color(0xFFE0E0E0)) { Text(if (manualRotacionEnabled) "Rot. manual ON" else "Rot. manual", fontSize = 11.sp) }
                SmallFloatingActionButton(onClick = { if (!rolling) { if (manualRotacionEnabled) manualRotorAngleDeg = 0f else rotorAngleDegState = 0f } }, containerColor = Color(0xFF222222), contentColor = amber) { Text("Reset rot", fontSize = 11.sp) }
                SmallFloatingActionButton(onClick = {
                    val ly = layout
                    val tap = lastTapImagePos
                    if (ly != null && tap != null) {
                        val (xi, yi) = tap
                        val cx = ly.centerPx.cx
                        val cy = ly.centerPx.cy
                        val tx = xi - cx
                        val ty = cy - yi
                        val rad = atan2(ty, tx)
                        val r = (ly.ringsPx.track_inner_r + ly.ringsPx.track_outer_r) / 2f
                        val bx = cx + r * cos(rad)
                        val by = cy - r * sin(rad)
                        testBallPos = bx to by
                        val rotorDegActual = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState
                        testBallDetectedPocket = detectarPocket(ly, rotorDegActual, testBallPos!!)
                        testBallAngleDeg = ((rad * 180f / PI.toFloat()).let { if (it < 0f) it + 360f else it })
                        showTestBallDialog = true
                    }
                }, containerColor = if (lastTapImagePos != null) amber else Color(0xFF222222), contentColor = if (lastTapImagePos != null) Color.Black else Color(0xFFE0E0E0)) { Text("Bola tap", fontSize = 11.sp) }
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
                        it.reset(); it.launchBall(); ganador = null; rolling = true; selectedPocket = null; selectedAnnulus = null; manualRotacionEnabled = false
                    }
                }, containerColor = if (rolling) Color(0xFF303030) else amber, contentColor = if (rolling) Color(0xFFBDBDBD) else Color.Black) { Text(if (rolling) "Girando…" else "Lanzar bola") }
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).background(Color.Black)) {
            if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                if (layout == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = amber, strokeWidth = 3.dp)
                        Spacer(Modifier.height(14.dp))
                        Text("Cargando ruleta...", color = Color.White.copy(alpha = 0.9f))
                    }
                } else {
                    RouletteCanvas(layout = layout!!, rotorAngleDeg = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState, ballImagePx = ballPosState, testBallPos = testBallPos,
                        onSelectPocket = { pk -> selectedPocket = pk; selectedAnnulus = null }, onSelectAnnulus = { ann -> selectedPocket = null; selectedAnnulus = ann },
                        manualRotationEnabled = manualRotacionEnabled && !rolling,
                        onManualRotateDelta = { d -> manualRotorAngleDeg = (manualRotorAngleDeg + d).mod(360f).let { if (it < 0f) it + 360f else it } },
                        onTapImage = { xi, yi -> lastTapImagePos = xi to yi })
                }
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
                val alpha by animateFloatAsState(if (showLabel) 1f else 0f, tween(220, easing = LinearOutSlowInEasing))
                val offsetY by animateDpAsState(if (showLabel) 0.dp else (-12).dp, tween(220, easing = LinearOutSlowInEasing))
                if (alpha > 0.01f) {
                    val density = LocalDensity.current
                    val ty = with(density) { offsetY.toPx() }
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.94f)), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.elevatedCardElevation(6.dp),
                        modifier = Modifier.align(if (ganador != null) Alignment.BottomCenter else Alignment.TopCenter).padding(if (ganador != null) 16.dp else 12.dp)
                            .graphicsLayer { this.alpha = alpha; this.translationY = if (ganador != null) -ty else ty }) {
                        Text(labelText ?: "", Modifier.padding(horizontal = 20.dp, vertical = 14.dp), fontWeight = FontWeight.SemiBold, color = if (ganador != null) amber else Color.White)
                    }
                }
                if (showOverlay) {
                    // Se omite overlay detallado para copia de respaldo
                }
                if (showTestBallDialog && testBallPos != null) {
                    AlertDialog(onDismissRequest = { showTestBallDialog = false }, confirmButton = { TextButton(onClick = { showTestBallDialog = false }) { Text("Cerrar", color = amber) } }, title = { Text("Bola de prueba", color = amber) }, text = {
                        val (bx, by) = testBallPos!!
                        val pocketTxt = when (val pk = testBallDetectedPocket) { null -> "(No detectado)"; else -> "Pocket: ${pk.number} (idx ${pk.index_clockwise + 1})" }
                        Column {
                            Text("Posición px: x=${"%.1f".format(bx)}, y=${"%.1f".format(by)}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                            Text(pocketTxt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            val rotorShown = if (manualRotacionEnabled && !rolling) manualRotorAngleDeg else rotorAngleDegState
                            Text("Rotor: ${"%.1f".format(rotorShown)}°", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                    }, containerColor = Color(0xFF1A1A1A), tonalElevation = 6.dp)
                }
            }
        }
    }
}

// -------------------- Canvas + hit-test --------------------

@Composable
private fun RouletteCanvas(
    layout: RouletteLayoutJson,
    rotorAngleDeg: Float,
    ballImagePx: Pair<Float, Float>?,
    testBallPos: Pair<Float, Float>?,
    onSelectPocket: (Pocket) -> Unit,
    onSelectAnnulus: (Annulus) -> Unit,
    manualRotationEnabled: Boolean,
    onManualRotateDelta: (Float) -> Unit,
    onTapImage: (Float, Float) -> Unit
) {
    var lastTap by remember { mutableStateOf<Offset?>(null) }
    var selectedPocket by remember { mutableStateOf<Pocket?>(null) }
    var selectedAnnulus by remember { mutableStateOf<Annulus?>(null) }
    var wheelCenter by remember { mutableStateOf(Offset.Zero) }
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(selectedPocket, selectedAnnulus) {
        pulse.snapTo(0f)
        if (selectedPocket != null || selectedAnnulus != null) pulse.animateTo(1f, infiniteRepeatable(tween(900, easing = LinearOutSlowInEasing), RepeatMode.Reverse)) else pulse.stop()
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF101010), Color(0xFF0B0B0B), Color.Black), radius = 900f)), contentAlignment = Alignment.Center) {
        val maxWpx = constraints.maxWidth.toFloat()
        val maxHpx = constraints.maxHeight.toFloat()
        val ratio = layout.imageSize.width / layout.imageSize.height
        val fitW = if (maxWpx / maxHpx < ratio) maxWpx else maxHpx * ratio
        val fitH = if (maxWpx / maxHpx < ratio) maxWpx / ratio else maxHpx
        val density = LocalDensity.current
        val targetWdp = with(density) { fitW.toDp() }
        val targetHdp = with(density) { fitH.toDp() }
        Canvas(Modifier.size(targetWdp, targetHdp).pointerInput(manualRotationEnabled, wheelCenter) {
            detectDragGestures(onDrag = { change, dragAmount ->
                if (manualRotationEnabled) {
                    val pos = change.position
                    val dx = pos.x - wheelCenter.x
                    val dy = pos.y - wheelCenter.y
                    val r2 = dx*dx + dy*dy
                    if (r2 > 4f) {
                        val dxD = dragAmount.x
                        val dyD = dragAmount.y
                        val deltaThetaRad = (dx * dyD - dy * dxD) / r2
                        val deltaDeg = deltaThetaRad * 180f / PI.toFloat()
                        onManualRotateDelta(deltaDeg)
                    }
                }
            })
        }.pointerInput(Unit) { detectTapGestures { lastTap = it } }) {
            val imgW = layout.imageSize.width
            val imgH = layout.imageSize.height
            val scale = min(size.width / imgW, size.height / imgH)
            val drawW = imgW * scale
            val drawH = imgH * scale
            val left = (size.width - drawW) / 2f
            val top = (size.height - drawH) / 2f
            val cxCanvas = left + layout.centerPx.cx * scale
            val cyCanvas = top + layout.centerPx.cy * scale
            wheelCenter = Offset(cxCanvas, cyCanvas)
            drawSubtleGuides(cxCanvas, cyCanvas, layout.ringsPx, scale)
            fun List<Offset>.toPathScaled(): Path {
                val p = Path(); if (isNotEmpty()) { val f = first(); p.moveTo(left + f.x*scale, top + f.y*scale); for (i in 1 until size) { val pt = get(i); p.lineTo(left + pt.x*scale, top + pt.y*scale) }; p.close() }; return p
            }
            val rings = layout.ringsPx
            val rOuter = rings.outer_r * scale
            val rTrackOuter = rings.track_outer_r * scale
            val rTrackInner = rings.track_inner_r * scale
            val rConeOuter = rings.cone_outer_r * scale
            val rConeInner = rings.cone_inner_r * scale
            val rHub = rings.hub_r * scale
            fun annulusPath(rIn: Float, rOut: Float): Path { val outer = Rect(cxCanvas - rOut, cyCanvas - rOut, cxCanvas + rOut, cyCanvas + rOut); val inner = Rect(cxCanvas - rIn, cyCanvas - rIn, cxCanvas + rIn, cyCanvas + rIn); return Path().apply { fillType = PathFillType.EvenOdd; addOval(outer); if (rIn>0f) addOval(inner) } }
            drawPath(annulusPath(rTrackOuter, rOuter), Brush.radialGradient(listOf(Color(0xFF1E1E1E), Color(0xFF111111)), center = Offset(cxCanvas, cyCanvas), radius = rOuter))
            drawPath(annulusPath(rTrackInner, rTrackOuter), Brush.radialGradient(listOf(Color(0xFF2A2A2A), Color(0xFF101010)), center = Offset(cxCanvas, cyCanvas), radius = rTrackOuter))
            drawPath(annulusPath(rConeInner, rConeOuter), Brush.radialGradient(listOf(Color(0xFF161616), Color(0xFF0E0E0E)), center = Offset(cxCanvas, cyCanvas), radius = rConeOuter))
            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF0F0F0F), Color(0xFF151515), Color(0xFF0B0B0B)), center = Offset(cxCanvas, cyCanvas), radius = rHub), radius = rHub, center = Offset(cxCanvas, cyCanvas))
            rotate(rotorAngleDeg, Offset(cxCanvas, cyCanvas)) {
                val gridStroke = Stroke(1.1f)
                val gridColor = Color.White.copy(alpha = 0.12f)
                (layout.lower + layout.middle + layout.upper).forEach { pk -> drawPath(pk.polygon.toPathScaled(), gridColor, gridStroke) }
                val rBase = min(imgW, imgH) / 2f
                val textPx = (rBase * 0.050f * scale).coerceAtLeast(10f)
                val paint = android.graphics.Paint().apply { isAntiAlias = true; textSize = textPx; color = android.graphics.Color.WHITE; textAlign = android.graphics.Paint.Align.CENTER }
                val shadow = android.graphics.Paint(paint).apply { color = 0x66000000.toInt() }
                layout.upper.forEach { pk -> if (pk.number >= 0) { val x = left + pk.centerX*scale; val y = top + pk.centerY*scale + textPx*0.35f; drawContext.canvas.nativeCanvas.drawText(pk.number.toString(), x+1.2f, y+1.2f, shadow); drawContext.canvas.nativeCanvas.drawText(pk.number.toString(), x, y, paint) } }
                val n = min(layout.middle.size, layout.upper.size)
                for (i in 0 until n) { val mid = layout.middle[i]; val num = layout.upper[i].number; if (num >= 0) { val x = left + mid.centerX*scale; val y = top + mid.centerY*scale + textPx*0.35f; drawContext.canvas.nativeCanvas.drawText(num.toString(), x+1.2f, y+1.2f, shadow); drawContext.canvas.nativeCanvas.drawText(num.toString(), x, y, paint) } }
            }
            lastTap?.let { tapCanvas ->
                val xi = (tapCanvas.x - left)/scale
                val yi = (tapCanvas.y - top)/scale
                onTapImage(xi, yi)
                val cx = layout.centerPx.cx
                val cy = layout.centerPx.cy
                val tx = xi - cx
                val ty = cy - yi
                val rTap = hypot(tx, ty)
                val thTap = atan2(ty, tx)
                val rotorAngleRad = (rotorAngleDeg * PI.toFloat() / 180f)
                val thLocal = thTap + rotorAngleRad
                val tau = (2f * PI.toFloat())
                val thLocalNorm = ((thLocal + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
                val rr = layout.ringsPx
                when {
                    rTap in rr.track_inner_r..rr.track_outer_r -> layout.upper.minByOrNull { angDiffAbs(thLocalNorm, it.thetaCenter) }?.let { selectedPocket = it; selectedAnnulus = null; onSelectPocket(it) }
                    rTap in rr.cone_outer_r..rr.track_inner_r -> layout.middle.minByOrNull { angDiffAbs(thLocalNorm, it.thetaCenter) }?.let { selectedPocket = it; selectedAnnulus = null; onSelectPocket(it) }
                    rTap in rr.cone_inner_r..rr.cone_outer_r -> layout.lower.minByOrNull { angDiffAbs(thLocalNorm, it.thetaCenter) }?.let { selectedPocket = it; selectedAnnulus = null; onSelectPocket(it) }
                    rTap in rr.track_outer_r..rr.outer_r -> { selectedPocket = null; selectedAnnulus = Annulus.OUTER_RING; onSelectAnnulus(Annulus.OUTER_RING) }
                    rTap <= rr.hub_r -> { selectedPocket = null; selectedAnnulus = Annulus.INNER_HUB; onSelectAnnulus(Annulus.INNER_HUB) }
                }
            }
            selectedPocket?.let { sel ->
                val pts = sel.polygon
                if (pts.isNotEmpty()) {
                    val path = Path().apply { moveTo(left + pts.first().x*scale, top + pts.first().y*scale); for (i in 1 until pts.size) { val pt = pts[i]; lineTo(left + pt.x*scale, top + pt.y*scale) }; close() }
                    val glowAlpha = 0.26f + 0.18f * sin(pulse.value * Math.PI).toFloat()
                    drawPath(path, Color(0xFFFFE066).copy(alpha = glowAlpha))
                    val strokeW = 3.0f + 2.0f * abs(sin(pulse.value * Math.PI)).toFloat()
                    drawPath(path, Color(0xFFFFEA00), style = Stroke(width = strokeW))
                }
            }
            selectedAnnulus?.let { which ->
                val (rIn, rOut) = when (which) { Annulus.OUTER_RING -> layout.ringsPx.track_outer_r to layout.ringsPx.outer_r; Annulus.INNER_HUB -> 0f to layout.ringsPx.hub_r }
                val ro = rOut * scale
                val ri = rIn * scale
                val outer = Rect(cxCanvas - ro, cyCanvas - ro, cxCanvas + ro, cyCanvas + ro)
                val inner = Rect(cxCanvas - ri, cyCanvas - ri, cxCanvas + ri, cyCanvas + ri)
                val annulus = Path().apply { fillType = PathFillType.EvenOdd; addOval(outer); if (ri>0f) addOval(inner) }
                val glowAlpha = 0.22f + 0.18f * sin(pulse.value * Math.PI).toFloat()
                drawPath(annulus, Color(0xFFFFE066).copy(alpha = glowAlpha))
                val strokeW = 2.6f + 1.8f * abs(sin(pulse.value * Math.PI)).toFloat()
                drawCircle(Color(0xFFFFEA00), rOut * scale, Offset(cxCanvas, cyCanvas), style = Stroke(strokeW))
                if (rIn > 0f) drawCircle(Color(0xFFFFEA00), rIn * scale, Offset(cxCanvas, cyCanvas), style = Stroke(strokeW))
            }
            ballImagePx?.let { (bx, by) ->
                val bxCanvas = left + bx * scale
                val byCanvas = top + by * scale
                val ballImgR = ((layout.ringsPx.track_outer_r - layout.ringsPx.track_inner_r) * 0.12f).coerceIn(5f, 18f)
                val ballR = ballImgR * scale
                drawCircle(Color.Black.copy(alpha = 0.35f), ballR * 1.05f, Offset(bxCanvas + ballR * 0.22f, byCanvas + ballR * 0.22f))
                drawCircle(Color.White, ballR, Offset(bxCanvas, byCanvas))
                drawCircle(Color.White.copy(alpha = 0.45f), ballR * 0.45f, Offset(bxCanvas - ballR * 0.35f, byCanvas - ballR * 0.35f))
            }
            testBallPos?.let { (tbx, tby) ->
                val tbCanvasX = left + tbx * scale
                val tbCanvasY = top + tby * scale
                val rVisual = ((layout.ringsPx.track_outer_r - layout.ringsPx.track_inner_r) * 0.12f).coerceIn(5f, 18f) * scale
                drawCircle(Color(0xFF00BCD4).copy(alpha = 0.30f), rVisual * 1.08f, Offset(tbCanvasX + rVisual * 0.20f, tbCanvasY + rVisual * 0.20f))
                drawCircle(Color(0xFF00E5FF), rVisual, Offset(tbCanvasX, tbCanvasY))
                drawCircle(Color(0xFFB2EBF2).copy(alpha = 0.55f), rVisual * 0.45f, Offset(tbCanvasX - rVisual * 0.35f, tbCanvasY - rVisual * 0.35f))
            }
            drawArc(color = Color.White.copy(alpha = 0.10f), startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = Offset((size.width - drawW)/2f, (size.height - drawH)/2f), size = Size(drawW, drawH), style = Stroke(1.2f))
        }
    }
}

// -------------------- Helpers --------------------

private fun Float.format3() = "% ,.3f".replace(" ", "").format(this)
private fun Float.format4() = "% ,.4f".replace(" ", "").format(this)
private fun Context.readTextFromUri(uri: Uri): String { contentResolver.openInputStream(uri).use { input -> requireNotNull(input); return input.bufferedReader(Charsets.UTF_8).readText() } }
private fun Context.readTextFromRaw(resId: Int): String { resources.openRawResource(resId).use { input -> return input.bufferedReader(Charsets.UTF_8).readText() } }
private fun setFieldIfExists(target: Any, fieldName: String, value: Any) { runCatching { val f = target::class.java.getDeclaredField(fieldName); f.isAccessible = true; val v = when (f.type) { Int::class.javaPrimitiveType, Int::class.java -> (value as Number).toInt(); Float::class.javaPrimitiveType, Float::class.java -> (value as Number).toFloat(); Double::class.javaPrimitiveType, Double::class.java -> (value as Number).toDouble(); Long::class.javaPrimitiveType, Long::class.java -> (value as Number).toLong(); Boolean::class.javaPrimitiveType, Boolean::class.java -> value as Boolean; else -> value }; f.set(target, v) } }
private fun callMethodIfExists(target: Any, methodName: String) { runCatching { val m = target::class.java.getDeclaredMethod(methodName); m.isAccessible = true; m.invoke(target) } }

