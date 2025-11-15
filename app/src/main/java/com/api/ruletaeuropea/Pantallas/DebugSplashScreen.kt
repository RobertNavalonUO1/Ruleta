package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Opciones de física para debug profesionales (independientes de otras clases).
 * Las unidades que se muestran son estándar para facilitar la calibración:
 * - Velocidades: m/s (o °/s si es angular)
 * - Masa: kg
 * - Gravedad: m/s²
 * - Coeficientes (μ, e): adimensionales
 */
data class DebugPhysicsOptions(
    // Lanzamiento / estados iniciales
    val initialBallSpeed: Float = 18.0f,         // m/s tangencial al aro alto
    val initialBallSpin: Float = 0.0f,           // rad/s (spin propio)
    val initialRotorSpeedDeg: Float = 35.0f,     // °/s (sentido independiente)

    // Propiedades de la bola
    val ballMassKg: Float = 0.018f,              // ~ 18 g
    val ballRadiusM: Float = 0.0095f,            // ~ 9.5 mm

    // Campo/gravedad
    val gravity: Float = 9.81f,                  // m/s²

    // Pérdidas / fricciones
    val airDragCdA: Float = 0.00035f,            // Cd*A efectiva (m²)
    val groundFrictionMu: Float = 0.12f,         // μ cinético pista/cone
    val rollingResistance: Float = 0.0025f,      // c_rr (adim., pequeño)
    val normalLoadFricK: Float = 0.00016f,       // pérdida ∝ N (adim.)

    // Rebotes / colisiones
    val restitutionPocket: Float = 0.25f,        // e contra tabiques/aros
    val restitutionWall: Float = 0.35f,          // e contra paredes “relieve”
    val angularDampingInPocket: Float = 0.90f,   // drag angular dentro del segmento medio

    // Captura / control temporal
    val timeScale: Float = 1.25f,                // factor de tiempo de la sim
    val substepsNearPocket: Int = 4,             // sub-steps en aro de bolsillos

    // Geometría funcional (no visual)
    val pocketBandPx: Float = 14f,               // ancho banda de “captura” (unidades de escena)
    val grooveK: Float = 10f,                    // torque hacia centro (Nm/rad relativo)
    val grooveTauMaxDeg: Float = 110f            // saturación de torque (°/s² relativo)
)

/**
 * Pantalla Splash de Debug “profesional”.
 * - No depende de clases externas del proyecto.
 * - Expone un callback onApply(options) para que el juego consuma estos parámetros.
 * - Incluye exportación simple a JSON y restaurar valores por defecto.
 */
@Composable
fun DebugSplashScreen(
    initial: DebugPhysicsOptions = DebugPhysicsOptions(),
    onApply: (DebugPhysicsOptions) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var opts by remember { mutableStateOf(initial) }
    val scroll = rememberScrollState()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color(0xFF08151B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Debug • Física de Ruleta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Ajusta parámetros físicos y exporta un perfil de pruebas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0BEC5)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { opts = DebugPhysicsOptions() }) { Text("Restaurar") }
                    Button(onClick = { onApply(opts) }) { Text("Aplicar") }
                    Button(onClick = onClose) { Text("Cerrar") }
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                // Lanzamiento / Rotor
                SectionCard(title = "Lanzamiento & Rotor") {
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Velocidad inicial de bola (m/s)",
                            value = opts.initialBallSpeed,
                            range = 5f..35f,
                            step = 0.1f
                        ) { opts = opts.copy(initialBallSpeed = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Spin inicial de bola (rad/s)",
                            value = opts.initialBallSpin,
                            range = -80f..80f,
                            step = 0.5f
                        ) { opts = opts.copy(initialBallSpin = it) }
                    }
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Velocidad del rotor (°/s)",
                            value = opts.initialRotorSpeedDeg,
                            range = -60f..60f,
                            step = 0.5f
                        ) { opts = opts.copy(initialRotorSpeedDeg = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Escala de tiempo (×)",
                            value = opts.timeScale,
                            range = 0.1f..5f,
                            step = 0.05f
                        ) { opts = opts.copy(timeScale = it) }
                    }
                }

                // Propiedades de la bola
                SectionCard(title = "Bola") {
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Masa (kg)",
                            value = opts.ballMassKg,
                            range = 0.010f..0.030f,
                            step = 0.001f,
                            format = { "%.3f".format(it) }
                        ) { opts = opts.copy(ballMassKg = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Radio (m)",
                            value = opts.ballRadiusM,
                            range = 0.0085f..0.0105f,
                            step = 0.0001f,
                            format = { "%.4f".format(it) }
                        ) { opts = opts.copy(ballRadiusM = it) }
                    }
                }

                // Campo / Gravedad
                SectionCard(title = "Campo & Gravedad") {
                    LabeledSlider(
                        label = "g (m/s²)",
                        value = opts.gravity,
                        range = 8.0f..11.0f,
                        step = 0.01f,
                        format = { "%.2f".format(it) }
                    ) { opts = opts.copy(gravity = it) }
                }

                // Pérdidas & fricciones
                SectionCard(title = "Pérdidas & Fricción") {
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Arrastre aire (Cd·A)",
                            value = opts.airDragCdA,
                            range = 0.00005f..0.0015f,
                            step = 0.00005f,
                            format = { "%.5f".format(it) }
                        ) { opts = opts.copy(airDragCdA = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Fricción suelo (μ cinético)",
                            value = opts.groundFrictionMu,
                            range = 0.02f..0.30f,
                            step = 0.005f,
                            format = { "%.3f".format(it) }
                        ) { opts = opts.copy(groundFrictionMu = it) }
                    }
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Rodadura (c_rr)",
                            value = opts.rollingResistance,
                            range = 0.0005f..0.0100f,
                            step = 0.0005f,
                            format = { "%.4f".format(it) }
                        ) { opts = opts.copy(rollingResistance = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Pérdida ∝ carga (K)",
                            value = opts.normalLoadFricK,
                            range = 0.0f..0.0010f,
                            step = 0.00005f,
                            format = { "%.5f".format(it) }
                        ) { opts = opts.copy(normalLoadFricK = it) }
                    }
                }

                // Rebotes y captura
                SectionCard(title = "Rebotes & Captura") {
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Restitución en bolsillos (e)",
                            value = opts.restitutionPocket,
                            range = 0.05f..0.60f,
                            step = 0.01f,
                            format = { "%.2f".format(it) }
                        ) { opts = opts.copy(restitutionPocket = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Restitución paredes (e)",
                            value = opts.restitutionWall,
                            range = 0.05f..0.80f,
                            step = 0.01f,
                            format = { "%.2f".format(it) }
                        ) { opts = opts.copy(restitutionWall = it) }
                    }
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Drag angular dentro del segmento",
                            value = opts.angularDampingInPocket,
                            range = 0.70f..0.99f,
                            step = 0.01f,
                            format = { "%.2f".format(it) }
                        ) { opts = opts.copy(angularDampingInPocket = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Sub-steps en aro (int)",
                            value = opts.substepsNearPocket.toFloat(),
                            range = 1f..8f,
                            step = 1f,
                            format = { it.roundToInt().toString() }
                        ) { opts = opts.copy(substepsNearPocket = it.roundToInt()) }
                    }
                    RowPair {
                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Banda de captura (px escena)",
                            value = opts.pocketBandPx,
                            range = 6f..24f,
                            step = 0.5f,
                            format = { "%.1f".format(it) }
                        ) { opts = opts.copy(pocketBandPx = it) }

                        LabeledSlider(
                            modifier = Modifier.weight(1f),
                            label = "Torque de canalización (K)",
                            value = opts.grooveK,
                            range = 0f..30f,
                            step = 0.5f,
                            format = { "%.1f".format(it) }
                        ) { opts = opts.copy(grooveK = it) }
                    }
                    LabeledSlider(
                        label = "Saturación de torque (°/s² relativo)",
                        value = opts.grooveTauMaxDeg,
                        range = 30f..180f,
                        step = 1f,
                        format = { "%.0f".format(it) }
                    ) { opts = opts.copy(grooveTauMaxDeg = it) }
                }

                // Exportar / Importar JSON
                SectionCard(title = "Perfil (JSON)") {
                    val json = remember(opts) { optionsToJson(opts).toString(2) }
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            clipboard.setText(androidx.compose.ui.text.AnnotatedString(json))
                        }) { Text("Copiar JSON") }
                        OutlinedButton(onClick = {
                            // Restaurar valores por defecto
                            opts = DebugPhysicsOptions()
                        }) { Text("Valores por defecto") }
                        Button(onClick = { onApply(opts) }) { Text("Aplicar") }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* --------------------------------- UI helpers -------------------------------- */

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0x22000000), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(title, color = Color(0xFFB2E6FF), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun RowPair(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun LabeledSlider(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    format: (Float) -> String = { v ->
        // formateo por defecto según magnitud
        when {
            step >= 1f -> "%.0f".format(v)
            step >= 0.1f -> "%.1f".format(v)
            step >= 0.01f -> "%.2f".format(v)
            step >= 0.001f -> "%.3f".format(v)
            else -> "%.4f".format(v)
        }
    },
    onChange: (Float) -> Unit
) {
    var tmp by remember { mutableStateOf(value) }
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Text(format(tmp), color = Color.LightGray, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = tmp,
            onValueChange = {
                tmp = it
                onChange(it)
            },
            valueRange = range,
            steps = ((range.endInclusive - range.start) / step).toInt().coerceAtMost(100),
        )
    }
}

/* --------------------------------- JSON helpers -------------------------------- */

private fun optionsToJson(o: DebugPhysicsOptions): JSONObject = JSONObject().apply {
    put("initialBallSpeed", o.initialBallSpeed)
    put("initialBallSpin", o.initialBallSpin)
    put("initialRotorSpeedDeg", o.initialRotorSpeedDeg)
    put("ballMassKg", o.ballMassKg)
    put("ballRadiusM", o.ballRadiusM)
    put("gravity", o.gravity)
    put("airDragCdA", o.airDragCdA)
    put("groundFrictionMu", o.groundFrictionMu)
    put("rollingResistance", o.rollingResistance)
    put("normalLoadFricK", o.normalLoadFricK)
    put("restitutionPocket", o.restitutionPocket)
    put("restitutionWall", o.restitutionWall)
    put("angularDampingInPocket", o.angularDampingInPocket)
    put("timeScale", o.timeScale)
    put("substepsNearPocket", o.substepsNearPocket)
    put("pocketBandPx", o.pocketBandPx)
    put("grooveK", o.grooveK)
    put("grooveTauMaxDeg", o.grooveTauMaxDeg)
}
