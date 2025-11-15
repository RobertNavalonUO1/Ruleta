package com.api.ruletaeuropea.pantallas

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap

enum class Annulus { OUTER_RING, INNER_HUB }

internal fun angDiffAbs(a: Float, b: Float): Float {
    var d = a - b
    val tau = (2f * Math.PI).toFloat()
    d = ((d + Math.PI.toFloat()) % tau + tau) % tau - Math.PI.toFloat()
    return abs(d)
}

// Solo cargamos imagen principal; texturas procedurales y colores se han omitido para simplificar.
private fun loadImageBitmap(ctx: Context, name: String): ImageBitmap? {
    val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
    return if (id != 0) runCatching { BitmapFactory.decodeResource(ctx.resources, id)?.asImageBitmap() }.getOrNull() else null
}

@Composable
fun ConfigOverlayBetter(
    modifier: Modifier,
    amber: Color,
    ballMin: Float,
    ballMax: Float,
    onBallMinChange: (Float) -> Unit,
    onBallMaxChange: (Float) -> Unit,
    rotorMin: Float,
    rotorMax: Float,
    onRotorMinChange: (Float) -> Unit,
    onRotorMaxChange: (Float) -> Unit,
    rVBase: Float,
    rVRange: Float,
    onRVBaseChange: (Float) -> Unit,
    onRVRangeChange: (Float) -> Unit,
    speedMul: Float,
    onSpeedMulChange: (Float) -> Unit,
    baseAngularFriction: Float,
    airRes: Float,
    onBaseFricChange: (Float) -> Unit,
    onAirResChange: (Float) -> Unit,
    ballScale: Float,
    onBallScaleChange: (Float) -> Unit,
    boardBounceElastic: Float,
    onBoardBounceElasticChange: (Float) -> Unit,
    boardBounceFric: Float,
    onBoardBounceFricChange: (Float) -> Unit,
    wallBumpWidthDeg: Float,
    onWallBumpWidthChange: (Float) -> Unit,
    wallBumpTorque: Float,
    onWallBumpTorqueChange: (Float) -> Unit,
    gravityZ: Float,
    onGravityZChange: (Float) -> Unit,
    ballZScaleK: Float,
    onBallZScaleKChange: (Float) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    val sliderScale = 0.9f
    val scrollState = rememberScrollState()
    Card(
        modifier = modifier
            .widthIn(min = 240.dp, max = 340.dp)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE121212)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(Modifier.padding(10.dp).verticalScroll(scrollState)) {
            Text("Configuración", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Section("Velocidad", amber) {
                LogSliderRow("Bola (mín)", ballMin, onBallMinChange, 0f, 999999f, { "${it.roundToInt()} °/s" }, amber, sliderScale, true)
                LogSliderRow("Bola (máx)", ballMax, onBallMaxChange, 0f, 999999f, { "${it.roundToInt()} °/s" }, amber, sliderScale, true)
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.15f))
                LogSliderRow("Rotor (mín)", rotorMin, onRotorMinChange, 0f, 999999f, { "${it.roundToInt()} °/s" }, amber, sliderScale, true)
                LogSliderRow("Rotor (máx)", rotorMax, onRotorMaxChange, 0f, 999999f, { "${it.roundToInt()} °/s" }, amber, sliderScale, true)
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.15f))
                LabeledSliderRow("rV base (in)", rVBase, onRVBaseChange, 0f, 500f, 5f, { "${it.roundToInt()} px/s" }, amber, sliderScale, true)
                LabeledSliderRow("rV rango", rVRange, onRVRangeChange, 0f, 600f, 5f, { "${it.roundToInt()} px/s" }, amber, sliderScale, true)
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.15f))
                LabeledSliderRow("Multiplicador global", speedMul, onSpeedMulChange, 0.25f, 2.5f, 0.05f, { "×${"%.2f".format(it)}" }, amber, sliderScale, true)
                AssistRow("Previsualización", "Bola: ${ballMin.roundToInt()}–${ballMax.roundToInt()} °/s • Rotor: ${rotorMin.roundToInt()}–${rotorMax.roundToInt()} °/s • rV: -${rVBase.roundToInt()}…-${(rVBase + rVRange).roundToInt()}")
            }
            Section("Fricción", amber) {
                LabeledSliderRow("Aire (airRes)", airRes, onAirResChange, 0.0001f, 0.0012f, 0.00005f, { it.format3() }, amber, sliderScale, true)
                LabeledSliderRow("Base angular (baseFric)", baseAngularFriction, onBaseFricChange, 0.95f, 0.9999f, 0.0005f, { it.format4() }, amber, sliderScale, true)
            }
            Section("Visual", amber) {
                LabeledSliderRow("Tamaño bola (mult)", ballScale, onBallScaleChange, 0.8f, 1.6f, 0.01f, { "×${"%.2f".format(it)}" }, amber, sliderScale, true)
                LabeledSliderRow("Escala altura (k)", ballZScaleK, onBallZScaleKChange, 0.0f, 0.02f, 0.0005f, { "${"%.4f".format(it)}" }, amber, sliderScale, true)
            }
            Section("Bordes / Colisiones", amber) {
                LabeledSliderRow("Rebote borde (elastic)", boardBounceElastic, onBoardBounceElasticChange, 0f, 1f, 0.01f, { "${"%.2f".format(it)}" }, amber, sliderScale, true)
                LabeledSliderRow("Fric. borde", boardBounceFric, onBoardBounceFricChange, 0.5f, 1f, 0.01f, { "${"%.2f".format(it)}" }, amber, sliderScale, true)
            }
            Section("Protuberancias / Medio", amber) {
                LabeledSliderRow("Bump ancho (deg)", wallBumpWidthDeg, onWallBumpWidthChange, 0.2f, 3.0f, 0.05f, { "${"%.2f".format(it)}°" }, amber, sliderScale, true)
                LabeledSliderRow("Bump torque", wallBumpTorque, onWallBumpTorqueChange, 0f, 40f, 0.5f, { "${it.roundToInt()}" }, amber, sliderScale, true)
            }
            Section("Vertical / Avanzado", amber) {
                LabeledSliderRow("Gravedad Z", gravityZ, onGravityZChange, 500f, 4000f, 50f, { "${it.roundToInt()} px/s²" }, amber, sliderScale, true)
                Text("Ajustes avanzados disponibles. Cambios live.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onReset, border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = amber), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp), modifier = Modifier.heightIn(min = 32.dp)) { Text("Reset", fontSize = 12.sp) }
                OutlinedButton(onClick = onSave, border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = amber), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp), modifier = Modifier.heightIn(min = 32.dp)) { Text("Guardar", fontSize = 12.sp) }
                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClick = { /* cierre desde FAB */ }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = amber.copy(alpha = 0.15f), contentColor = amber), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp), modifier = Modifier.heightIn(min = 32.dp)) { Text("Listo", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun Section(title: String, amber: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .padding(10.dp)
    ) {
        Text(title, color = amber, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun AssistRow(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(title, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
        Text(subtitle, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
    }
}

@Composable
fun LabeledSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    step: Float,
    format: (Float) -> String,
    amber: Color,
    sliderScale: Float = 1f,
    compact: Boolean = false
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = if (compact) 12.sp else 14.sp)
            Text(format(value), color = amber, fontSize = if (compact) 12.sp else 14.sp)
        }
        Slider(
            value = value,
            onValueChange = { v ->
                val q = ((v - min) / step).roundToInt() * step + min
                onValueChange(q.coerceIn(min, max))
            },
            valueRange = min..max,
            colors = SliderDefaults.colors(thumbColor = amber, activeTrackColor = amber),
            modifier = Modifier.fillMaxWidth().scale(sliderScale)
        )
    }
    Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
}

@Composable
fun LogSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    format: (Float) -> String,
    amber: Color,
    sliderScale: Float = 1f,
    compact: Boolean = false
) {
    val range = max - min
    var ui by remember { mutableFloatStateOf((value - min).coerceIn(0f, range) / range) }
    LaunchedEffect(value, min, max) { ui = (value - min).coerceIn(0f, range) / range }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = if (compact) 12.sp else 14.sp)
            Text(format(value), color = amber, fontSize = if (compact) 12.sp else 14.sp)
        }
        Slider(
            value = ui,
            onValueChange = { u -> ui = u; onValueChange(min + u * range) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = amber, activeTrackColor = amber),
            modifier = Modifier.fillMaxWidth().scale(sliderScale)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${min.roundToInt()}", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, fontSize = if (compact) 10.sp else 12.sp)
            Text("${max.roundToInt()}", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, fontSize = if (compact) 10.sp else 12.sp)
        }
    }
    Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
}

private fun Float.format3() = "% ,.3f".replace(" ", "").format(this)
private fun Float.format4() = "% ,.4f".replace(" ", "").format(this)

@Composable
fun RouletteCanvas(
    layout: RouletteLayoutJson,
    rotorAngleDeg: Float,
    ballImagePx: Pair<Float, Float>?,
    testBallPos: Pair<Float, Float>?,
    onSelectPocket: (Pocket) -> Unit,
    onSelectAnnulus: (Annulus) -> Unit,
    manualRotationEnabled: Boolean,
    onManualRotateDelta: (Float) -> Unit,
    onTapImage: (Float, Float) -> Unit,
    ballZ: Float
) {
    var lastTap by remember { mutableStateOf<Offset?>(null) }
    var selectedPocket by remember { mutableStateOf<Pocket?>(null) }
    var selectedAnnulus by remember { mutableStateOf<Annulus?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var wheelCenter by remember { mutableStateOf(Offset.Zero) }
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(selectedPocket, selectedAnnulus) {
        pulse.snapTo(0f)
        if (selectedPocket != null || selectedAnnulus != null) {
            pulse.animateTo(1f, animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse))
        } else pulse.stop()
    }
    val ctx = LocalContext.current
    val ruletaBitmap = remember(ctx) { loadImageBitmap(ctx, "ruleta") }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxWpx = constraints.maxWidth.toFloat()
        val maxHpx = constraints.maxHeight.toFloat()
        val ratio = layout.imageSize.width / layout.imageSize.height
        val fitW = if (maxWpx / maxHpx < ratio) maxWpx else maxHpx * ratio
        val fitH = if (maxWpx / maxHpx < ratio) maxWpx / ratio else maxHpx
        val density = LocalDensity.current
        val targetWdp = with(density) { fitW.toDp() }
        val targetHdp = with(density) { fitH.toDp() }
        Canvas(
            modifier = Modifier
                .size(targetWdp, targetHdp)
                .onSizeChanged { canvasSize = it }
                .pointerInput(manualRotationEnabled, canvasSize, wheelCenter) {
                    detectDragGestures(onDrag = { change, dragAmount ->
                        if (manualRotationEnabled) {
                            val pos = change.position
                            val dx = pos.x - wheelCenter.x
                            val dy = pos.y - wheelCenter.y
                            val r2 = dx * dx + dy * dy
                            if (r2 > 4f) {
                                val dxDelta = dragAmount.x
                                val dyDelta = dragAmount.y
                                val deltaThetaRad = (dx * dyDelta - dy * dxDelta) / r2
                                val deltaDeg = deltaThetaRad * 180f / PI.toFloat()
                                onManualRotateDelta(deltaDeg)
                            }
                        }
                    })
                }
                .pointerInput(Unit) { detectTapGestures { off -> lastTap = off } }
        ) {
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
            val rOuter = layout.ringsPx.outer_r * scale
            val clipPath = Path().apply { addOval(Rect(cxCanvas - rOuter, cyCanvas - rOuter, cxCanvas + rOuter, cyCanvas + rOuter)) }
            clipPath(clipPath) { rotate(rotorAngleDeg, Offset(cxCanvas, cyCanvas)) { ruletaBitmap?.let { img -> drawImage(img, dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()), dstOffset = IntOffset(left.roundToInt(), top.roundToInt())) } } }
            lastTap?.let { tapCanvas ->
                val xi = (tapCanvas.x - left) / scale
                val yi = (tapCanvas.y - top) / scale
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
                    else -> Unit
                }
            }
            selectedPocket?.let { sel ->
                val pts = sel.polygon
                if (pts.isNotEmpty()) {
                    val path = Path().apply {
                        moveTo(left + pts.first().x * scale, top + pts.first().y * scale)
                        for (i in 1 until pts.size) lineTo(left + pts[i].x * scale, top + pts[i].y * scale)
                        close()
                    }
                    val glowAlpha = 0.26f + 0.18f * sin(pulse.value * Math.PI).toFloat()
                    drawPath(path, Color(0xFFFFE066).copy(alpha = glowAlpha))
                    val strokeW = 3.0f + 2.0f * abs(sin(pulse.value * Math.PI)).toFloat()
                    drawPath(path, Color(0xFFFFEA00), style = Stroke(width = strokeW))
                }
            }
            selectedAnnulus?.let { which ->
                val (rIn, rOut) = when (which) {
                    Annulus.OUTER_RING -> layout.ringsPx.track_outer_r to layout.ringsPx.outer_r
                    Annulus.INNER_HUB -> 0f to layout.ringsPx.hub_r
                }
                val ro = rOut * scale
                val ri = rIn * scale
                val outer = Rect(cxCanvas - ro, cyCanvas - ro, cxCanvas + ro, cyCanvas + ro)
                val inner = Rect(cxCanvas - ri, cyCanvas - ri, cxCanvas + ri, cyCanvas + ri)
                val annulus = Path().apply { fillType = PathFillType.EvenOdd; addOval(outer); if (ri > 0f) addOval(inner) }
                val glowAlpha = 0.22f + 0.18f * sin(pulse.value * Math.PI).toFloat()
                drawPath(annulus, Color(0xFFFFE066).copy(alpha = glowAlpha))
                val strokeW = 2.6f + 1.8f * abs(sin(pulse.value * Math.PI)).toFloat()
                drawCircle(Color(0xFFFFEA00), rOut * scale, Offset(cxCanvas, cyCanvas), style = Stroke(strokeW))
                if (rIn > 0f) drawCircle(Color(0xFFFFEA00), rIn * scale, Offset(cxCanvas, cyCanvas), style = Stroke(strokeW))
            }
            ballImagePx?.let { (bx, by) ->
                val bxCanvas = left + bx * scale
                val byCanvas = top + by * scale
                val baseBallPx = (((layout.ringsPx.track_outer_r - layout.ringsPx.track_inner_r) * 0.12f) * 1.15f).coerceIn(6f, 21f)
                val kAlt = 0.006f
                val alt = ballZ
                val ballImgR = (baseBallPx * (1f + kAlt * alt)).coerceIn(6f, baseBallPx * 2.5f)
                val ballR = ballImgR * scale
                val shadowOffset = ballR * (0.22f + 0.30f * ((alt.coerceIn(0f, 12f)) / 12f))
                drawCircle(Color(0xFFFFF59D).copy(alpha = 0.12f), ballR * 1.55f, Offset(bxCanvas, byCanvas))
                drawCircle(Color.Black.copy(alpha = 0.45f), ballR * 1.05f, Offset(bxCanvas + shadowOffset, byCanvas + shadowOffset))
                drawCircle(Color.White, ballR, Offset(bxCanvas, byCanvas))
                drawCircle(Color.White.copy(alpha = 0.50f), ballR * 0.46f, Offset(bxCanvas - ballR * 0.35f, byCanvas - ballR * 0.35f))
                drawCircle(Color(0xFFFFEB3B), ballR, Offset(bxCanvas, byCanvas), style = Stroke(width = max(1.5f, ballR * 0.08f)))
            }
            testBallPos?.let { (tbx, tby) ->
                val tbCanvasX = left + tbx * scale
                val tbCanvasY = top + tby * scale
                val rVisual = (((layout.ringsPx.track_outer_r - layout.ringsPx.track_inner_r) * 0.12f) * 1.15f).coerceIn(6f, 21f) * scale
                drawCircle(Color(0xFF00BCD4).copy(alpha = 0.30f), rVisual * 1.08f, Offset(tbCanvasX + rVisual * 0.20f, tbCanvasY + rVisual * 0.20f))
                drawCircle(Color(0xFF00E5FF), rVisual, Offset(tbCanvasX, tbCanvasY))
                drawCircle(Color(0xFFB2EBF2).copy(alpha = 0.55f), rVisual * 0.45f, Offset(tbCanvasX - rVisual * 0.35f, tbCanvasY - rVisual * 0.35f))
            }
            drawArc(color = Color.White.copy(alpha = 0.10f), startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = Offset((size.width - drawW) / 2f, (size.height - drawH) / 2f), size = Size(drawW, drawH), style = Stroke(width = 1.2f))
        }
    }
}
