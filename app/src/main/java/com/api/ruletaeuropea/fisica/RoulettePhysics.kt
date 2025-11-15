package com.api.ruletaeuropea.fisica

import org.json.JSONObject
import kotlin.math.*
import kotlin.random.Random

/**
 * Ruleta europea sin deflectores con "relieve" en el segmento medio.
 * Correcciones previas + mejoras de configuración:
 *  - Rangos de lanzamiento más amplios y ajustables desde UI (bola y rotor).
 *  - Multiplicador global de velocidad (speedMultiplier) vinculado a timeScale.
 *  - Sin límites artificiales: se usa coerceIn() solo cuando es imprescindible por estabilidad.
 *  - Documentación para facilitar el enlace UI ↔ física.
 */
class RoulettePhysics(
    layout: JSONObject,
    seed: Int? = null,
    initialConfig: Config = Config()
) {
    // ---------------- Config pública ----------------
    data class Config(
        var thetaTrackDeg: Float = 12f,
        var thetaConeDeg:  Float = 14f,
        // Fricción y arrastre
        var airRes: Float = 0.00055f,
        var baseAngularFriction: Float = 0.9930f,
        var normalFricK: Float = 0.00020f,
        var radialDamping: Float = 0.78f,

        // Nuevos: drag lineal y cuadrático sobre w (rad/s)
        var angularViscousC: Float = 0.22f,
        var angularQuadraticC: Float = 0.015f,
        // Fricción por “slip” relativo con el rotor (reforzado cerca del aro)
        var slipDragC: Float = 0.38f,
        // Amortiguación radial viscosa adicional (aditiva)
        var radialViscousC: Float = 0.85f,

        var kBowl: Float = 9.0f,
        var slopeTop: Float = -52f,
        var slopeCone: Float = +52f,

        var grooveK: Float = 10f,
        var grooveTauMaxDeg: Float = 110f,
        var pocketBand: Float = 14f,

        var midSectionHalfWidthDeg: Float = 4.5f,
        var midSectionBand: Float = 10f,
        var midAngularDrag: Float = 0.86f,
        var wallElasticity: Float = 0.35f,
        var wallEdgeTolDeg: Float = 0.6f,
        var wallRadialInKick: Float = 65f,

        var stopRadTol: Float = 2.4f,
        var stopWdeg: Float = 6.0f,
        var stopRV: Float = 10f,
        var energyStop: Float = 0.26f,

        var tiltTau: Float = 2.0f,
        var tiltSigmaDeg: Float = 0.18f,

        var coupleStartR: Float? = null,
        var coupleEndR:   Float? = null,

        // “Pegajosidad” extra en pocket band (multiplicativo por step)
        var pocketSticky: Float = 0.970f,
        // Congelación dura final cuando ya es prácticamente reposo en el aro
        var freezeWdeg: Float = 3.0f,
        var freezeRV: Float = 6.0f,

        // --- Captura hacia segmento medio ---
        var captureBiasK: Float = 14f,
        var captureBandExtraDeg: Float = 6f,
        var requireMidForStop: Boolean = true,

        // --- Colisiones en límites exterior/interior ---
        var outerWallElasticity: Float = 0.45f,  // 0..1
        var outerWallFriction: Float = 0.92f,    // factor sobre w al impactar
        var innerWallElasticity: Float = 0.35f,
        var innerWallFriction: Float = 0.96f,
        var wallTolPx: Float = 1.3f,            // tolerancia para detectar contacto

        // --- Protuberancias en laterales del segmento medio ---
        var wallBumpWidthDeg: Float = 1.2f,
        var wallBumpTorque: Float = 22f,        // intensidad del empuje hacia el centro
        var wallBumpDamping: Float = 0.98f,

        // --- Dinámica vertical (altura/salto) ---
        var gZ: Float = 2200f,                // gravedad vertical px/s^2 (aumentada para caída más rápida)
        var verticalViscousC: Float = 0.10f,  // arrastre vertical lineal
        var bounceZ: Float = 0.42f,           // restitución al tocar suelo (z=0)
        var jumpOuterK: Float = 0.18f,        // ganancia de salto en choque exterior
        var jumpInnerK: Float = 0.14f,        // ganancia de salto en choque interior
        var wallJumpK: Float = 0.22f,         // ganancia de salto en paredes laterales/bump
        var zClearanceInner: Float = 4.0f,    // altura para sortear interior

        // --- Velocidad fija del rotor (realista). Si es null, usa rango Min/Max ---
        var rotorDegPerSecFixed: Float? = 24f, // ≈ 4 rpm

        // --- Asentamiento seguro (evitar descanso en separadores) ---
        var edgeNoRestTolDeg: Float = 0.8f,
        var settleWdeg: Float = 9f,
        var settleRV: Float = 8f,
        var settleSnapK: Float = 36f,

        // --- Restricción borde tablero (considera radio de la bola) ---
        var ballRadiusPx: Float = 8f,
        var boardOuterBounceElasticity: Float = 0.55f,
        var boardOuterFriction: Float = 0.90f,
        var boardOuterTolPx: Float = 0.5f
    )

    // ---------------- VARIABLES ----------------
    var cfg = initialConfig.apply {
        // Aumentamos 15% el radio físico para coincidir con visual
        ballRadiusPx = ballRadiusPx * 1.15f
    }

    /**
     * Parámetros de lanzamiento ajustables desde UI. Todas las magnitudes en °/s salvo rV*.
     * - *rV* en px/s aprox. (depende de la geometría), se mantiene en float.
     * - RANGOS EXTENDIDOS (por defecto ya “rápidos”):
     *   - Bola:   1200..2200 °/s (ajustable hasta 3000 °/s sin cortes)
     *   - Rotor:   300..500  °/s (ajustable hasta 600  °/s)
     */
    var ballMinDegInit: Int = 1200
    var ballMaxDegInit: Int = 2200
    var rotorMinDegInit: Int = 300
    var rotorMaxDegInit: Int = 500

    // Velocidad radial inicial hacia dentro
    var rVBaseInit: Float = 220f
    var rVRangeInit: Float = 260f

    // Multiplicador global (afecta integración temporal y sensaciones de rapidez)
    // Alias de timeScale para consistencia con UI.
    var speedMultiplier: Float
        get() = timeScale
        set(value) { timeScale = value.coerceAtLeast(0.1f) }

    private val PI_F = Math.PI.toFloat()
    private val TWO_PI_F = 2f * PI_F
    private fun wrapRad(x: Float): Float { val t = (x % TWO_PI_F); return if (t < 0f) t + TWO_PI_F else t }
    private fun wrapDeg(x: Float): Float { val v = x % 360f; return if (v < 0f) v + 360f else v }
    private fun degToRadF(d: Float): Float = d * (PI_F / 180f)
    private fun radToDegF(r: Float): Float = r * (180f / PI_F)
    private fun smallestAngle(a: Float, b: Float): Float {
        var d = (a - b) % TWO_PI_F
        if (d < -PI_F) d += TWO_PI_F
        if (d >  PI_F) d -= TWO_PI_F
        return d
    }
    private fun smoothstep(e0: Float, e1: Float, x: Float): Float {
        val t = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun pocketBandWeight(rNow: Float): Float {
        val d = abs(rNow - pocketR)
        val denom = 2f * cfg.pocketBand
        if (denom <= 0f) return 0f
        val w1 = (1f - (d / denom)).coerceIn(0f, 1f)
        return w1 * w1 * (3f - 2f * w1)
    }

    // Gaussian ~ N(0,1) (Box–Muller con caché)
    private var gaussCache: Float? = null
    private fun nextGaussianBM(): Float {
        gaussCache?.let { g -> gaussCache = null; return g }
        var u1 = 0f
        while (u1 <= 1e-7f) u1 = rng.nextFloat()
        val u2 = rng.nextFloat()
        val r = sqrt((-2.0 * ln(u1.toDouble())).toFloat())
        val th = (2f * PI_F) * u2
        val z0 = r * cos(th)
        val z1 = r * sin(th)
        gaussCache = z1
        return z0
    }

    // ---------------- Geometría JSON ----------------
    private val cx = layout.getJSONObject("center_px").getDouble("cx").toFloat()
    private val cy = layout.getJSONObject("center_px").getDouble("cy").toFloat()

    private val rings = layout.getJSONObject("rings_px")
    private val outerR = rings.getDouble("outer_r").toFloat()
    private val trackOuterR = rings.getDouble("track_outer_r").toFloat()
    private val trackInnerR = rings.getDouble("track_inner_r").toFloat()
    private val coneOuterR  = rings.getDouble("cone_outer_r").toFloat()
    private val coneInnerR  = rings.getDouble("cone_inner_r").toFloat()
    private val pocketR = (trackInnerR + coneOuterR) * 0.5f

    private val angRef = layout.getJSONObject("angles_reference")
    private val isCWFile = angRef.getString("direction").equals("clockwise", true)
    private val extraRot = angRef.optDouble("extra_rotation_rad", 0.0).toFloat()
    private fun fileToCCW(radFromFile: Float): Float {
        val withOffset = wrapRad(radFromFile + extraRot)
        return if (isCWFile) wrapRad(TWO_PI_F - withOffset) else withOffset
    }

    private val pocketsArr = layout.getJSONArray("upper_pockets")
    private val pocketCentersCCW: FloatArray = run {
        FloatArray(pocketsArr.length()) { i ->
            fileToCCW(pocketsArr.getJSONObject(i).getDouble("theta_center_rad").toFloat())
        }.sortedArray()
    }
    private data class PocketRange(val start: Float, val end: Float, val number: Int)
    private val pocketRangesCCW: List<PocketRange> = run {
        val list = ArrayList<PocketRange>(pocketsArr.length())
        for (i in 0 until pocketsArr.length()) {
            val p = pocketsArr.getJSONObject(i)
            list.add(
                PocketRange(
                    start = wrapRad(fileToCCW(p.getDouble("theta_start_rad").toFloat())),
                    end   = wrapRad(fileToCCW(p.getDouble("theta_end_rad").toFloat())),
                    number = p.getInt("number")
                )
            )
        }
        list
    }
    // Separadores (entre bolsillos) en CCW
    private val sepsArr = layout.getJSONArray("separators_theta_rad_clockwise")
    private val separatorsCCW: FloatArray = run {
        FloatArray(sepsArr.length()) { i -> fileToCCW(sepsArr.getDouble(i).toFloat()) }.sortedArray()
    }

    // ---------------- Estado ----------------
    private val rng: Random = seed?.let { Random(it) } ?: Random
    private val baseDt = 1f / 60f

    /** multiplicador global de integración/tiempo de simulación */
    var timeScale: Float = 1f

    var rouletteAngleDeg = 0f; private set
    private var wheelW = 0f
    private var theta = 0f
    private var r = outerR
    private var w = 0f
    private var rV = 0f
    private var tilt = 0f
    private var resultPocket: Int? = null
    var rolling = false; private set

    // Altura (3D-lite)
    private var z = 0f
    private var vz = 0f

    // ---------------- Precálculos cfg ----------------
    private var tanTrack = tan(degToRadF(cfg.thetaTrackDeg))
    private var tanCone  = tan(degToRadF(cfg.thetaConeDeg))
    private var grooveTauMax = degToRadF(cfg.grooveTauMaxDeg)
    private var tiltSigma = degToRadF(cfg.tiltSigmaDeg)
    private var stopW = degToRadF(cfg.stopWdeg)
    private var wallBumpWidthRad = degToRadF(cfg.wallBumpWidthDeg)

    // ---------------- Debug / Telemetría ----------------
    data class Telemetry(
        val thetaDeg: Float,
        val r: Float,
        val rV: Float,
        val wDeg: Float,
        val wheelWDeg: Float,
        val wRelDeg: Float,
        val inPocketBand: Boolean,
        val inMidSection: Boolean
    )
    private val traceCapacity = 120
    private val trace: ArrayDeque<Pair<Float, Float>> = ArrayDeque(traceCapacity)
    fun getTrace(): List<Pair<Float, Float>> = trace.toList()
    fun getTelemetry(): Telemetry {
        val kCouple = smoothstep(cfg.coupleStartR ?: trackInnerR, cfg.coupleEndR ?: coneOuterR, r)
        val wRel = w - kCouple * wheelW
        val mid = isInsideMidSection(theta, r)
        return Telemetry(
            thetaDeg = radToDegF(theta),
            r = r,
            rV = rV,
            wDeg = radToDegF(w),
            wheelWDeg = radToDegF(wheelW),
            wRelDeg = radToDegF(wRel),
            inPocketBand = abs(r - pocketR) <= cfg.pocketBand,
            inMidSection = mid.inside
        )
    }

    // ---------------- API ----------------

    /**
     * Lanza la bola aplicando los rangos actuales de UI (bola y rotor).
     * Mantiene la aleatoriedad de signo para variedad visual.
     */
    fun launchBall(randomizeSigns: Boolean = true) {
        rouletteAngleDeg = 0f

        // Rotor (°/s): usa fijo si está configurado, si no, rango
        val rotorDegPerSec = cfg.rotorDegPerSecFixed?.let { it.toInt() } ?: run {
            val rMin = min(rotorMinDegInit, rotorMaxDegInit)
            val rMax = max(rotorMinDegInit, rotorMaxDegInit)
            rng.nextInt(rMin, rMax)
        }
        wheelW = degToRadF(rotorDegPerSec.toFloat() * speedMultiplier) * 1f // sentido fijo

        // Bola (°/s) — admite hasta 3000°/s
        val bMin = min(ballMinDegInit, ballMaxDegInit)
        val bMax = max(ballMinDegInit, ballMaxDegInit)
        val ballDegPerSec = rng.nextInt(bMin, bMax)
        w = degToRadF(ballDegPerSec.toFloat() * speedMultiplier) *
                (if (!randomizeSigns || rng.nextBoolean()) 1f else -1f)

        // Posición inicial
        theta = degToRadF(rng.nextInt(0, 360).toFloat())
        r = outerR

        // Velocidad radial inicial hacia dentro
        val rV0 = (rng.nextFloat() * rVRangeInit + rVBaseInit)
        rV = -rV0 * (0.75f + 0.25f * speedMultiplier)

        // Altura inicial
        z = 0f; vz = 0f

        resultPocket = null
        rolling = true
    }

    /**
     * Avanza la simulación. timeScale/speedMultiplier gobiernan la “rapidez global”.
     */
    fun updatePhysics() {
        if (!rolling) return
        var acc = baseDt * timeScale.coerceAtLeast(0.1f)
        while (acc > 0f && rolling) {
            val nearPocket = abs(r - pocketR) < 20f
            val h = if (nearPocket) min(baseDt, 0.25f * baseDt) else baseDt
            subStep(h)
            acc -= h
        }
        if (trace.size >= traceCapacity) trace.removeFirst()
        trace.addLast(ballPosition())
    }

    fun reset() {
        rolling = false
        r = outerR; w = 0f; rV = 0f
        rouletteAngleDeg = 0f; wheelW = 0f
        resultPocket = null
        tilt = 0f
        trace.clear()
    }

    fun ballPosition(): Pair<Float, Float> {
        val bx = cx + r * cos(theta)
        val by = cy + r * sin(theta)
        return Pair(bx, by)
    }
    fun ballAltitude(): Float = z

    fun resultPocketNumber(): Int = resultPocket ?: 0

    fun calculateWin(apuestas: List<com.api.ruletaeuropea.Modelo.Apuesta>): Int {
        val n = resultPocketNumber()
        return apuestas.filter { it.numero == n }.sumOf { it.valorMoneda * 36 }
    }

    fun debugStatus(): String {
        val t = getTelemetry()
        val base = "θ=${"%.1f".format(t.thetaDeg)}° r=${"%.2f".format(t.r)} rV=${"%.1f".format(t.rV)} w=${"%.1f".format(t.wDeg)}°/s wR=${"%.1f".format(t.wheelWDeg)}°/s wRel=${"%.1f".format(t.wRelDeg)}°/s"
        val flags = " band=${t.inPocketBand} mid=${t.inMidSection}"
        return if (resultPocket != null) "$base$flags pocket=$resultPocket" else base + flags
    }

    // ---------------- Dinámica ----------------
    private fun subStep(h: Float) {
        // 1) Rotor/UI
        rouletteAngleDeg = wrapDeg(rouletteAngleDeg + radToDegF(wheelW) * h)
        val rotorMul = (cfg.baseAngularFriction - abs(wheelW) * (cfg.airRes * 0.6f)).coerceIn(0.980f, 0.9995f)
        wheelW = wheelW * rotorMul - 0.12f * wheelW * h

        // 2) Ruido OU (usa Box–Muller)
        val eta = nextGaussianBM()
        val sqrt2OverTau = sqrt((2.0 / cfg.tiltTau.toDouble())).toFloat()
        val sqrtH = sqrt(h.toDouble()).toFloat()
        tilt += (-tilt / cfg.tiltTau + tiltSigma * sqrt2OverTau * eta / sqrtH) * h

        // 3) Pérdidas angulares: base + aire + carga + drag lineal/cuadrático + slip relativo
        val normalLoad = (w * w * r).coerceAtLeast(0f)
        val dynFricMul = (cfg.baseAngularFriction - abs(w) * cfg.airRes - normalLoad * cfg.normalFricK * h)
            .coerceIn(0.90f, 0.9999f)
        w *= dynFricMul
        w -= (cfg.angularViscousC * w + cfg.angularQuadraticC * abs(w) * w) * h
        val kCouple = smoothstep(cfg.coupleStartR ?: trackInnerR, cfg.coupleEndR ?: coneOuterR, r)
        val wRel = w - kCouple * wheelW
        val bandW = pocketBandWeight(r)
        val slipGain = cfg.slipDragC * (0.25f + 0.75f * kCouple) * (0.35f + 0.65f * bandW)
        w -= slipGain * wRel * h

        // 4) Aceleración radial
        var aRad = -cfg.kBowl * (r - pocketR)
        aRad += when {
            r >= trackInnerR -> cfg.slopeTop
            r <= coneOuterR  -> cfg.slopeCone
            else -> 0f
        }

        val needTrack = (wRel * wRel * r) - (140f * tanTrack)
        val needCone  = (wRel * wRel * r) - (160f * tanCone)
        if (r > pocketR && r >= trackInnerR && needTrack < 0f) aRad -= 140f + needTrack
        if (r < pocketR && r <= coneOuterR  && needCone  < 0f) aRad += 140f - needCone

        aRad += sin(tilt) * 7.5f

        rV += aRad * h
        r  += rV * h

        // Colisión preventiva con borde tomando el radio efectivo de la bola (evita salida del tablero)
        run {
            val kAlt = 0.006f // mismo factor visual
            val effBallR = (cfg.ballRadiusPx * (1f + kAlt * z)).coerceAtLeast(cfg.ballRadiusPx)
            val limitOuter = outerR - effBallR - cfg.boardOuterTolPx
            if (r > limitOuter) {
                // Retrocede al límite y rebota radialmente
                r = limitOuter
                if (rV > 0f) {
                    rV = -rV * cfg.boardOuterBounceElasticity
                } else {
                    // Si ya íbamos ajustando, pequeña corrección para que no se "pegue"
                    rV *= -0.15f * cfg.boardOuterBounceElasticity
                }
                // Fricción tangencial adicional en el choque
                w *= cfg.boardOuterFriction
                // Impulso vertical ligero por impacto (si no hay otro en este frame)
                if (vz <= 0f) vz += cfg.jumpOuterK * (0.25f * abs(rV) + 0.02f * abs(w) * r)
            }
        }

        // 4.5) Colisiones con límites exterior e interior (radiales base)
        val tol = cfg.wallTolPx
        if (r >= outerR - tol && rV > 0f) {
            r = outerR - tol
            rV = -rV * cfg.outerWallElasticity
            w *= cfg.outerWallFriction
            val tangV = abs(w) * r
            vz += cfg.jumpOuterK * (0.5f * abs(rV) + 0.05f * tangV)
        }
        if (r <= coneInnerR + tol && rV < 0f) {
            r = coneInnerR + tol
            rV = -rV * cfg.innerWallElasticity
            w *= cfg.innerWallFriction
            val tangV = abs(w) * r
            vz += cfg.jumpInnerK * (0.5f * abs(rV) + 0.05f * tangV)
        }

        // 5) Potencial periódico (surcos) y captura hacia sección media
        if (abs(r - pocketR) <= cfg.pocketBand) {
            val tau = grooveTorqueCCW(theta).coerceIn(-grooveTauMax, +grooveTauMax)
            w += tau * h
            val sticky = cfg.pocketSticky
            w *= sticky
            rV *= sticky
            val midInfo = isInsideMidSection(theta, r)
            if (!midInfo.inside) {
                val dist = smallestAngle(theta, midInfo.center)
                val absDistDeg = abs(radToDegF(dist))
                val maxBandDeg = cfg.midSectionHalfWidthDeg + cfg.captureBandExtraDeg
                if (absDistDeg <= maxBandDeg) {
                    val dir = if (dist > 0f) -1f else 1f
                    val weightAng = 1f - (absDistDeg / maxBandDeg).coerceIn(0f, 1f)
                    val bias = cfg.captureBiasK * dir * weightAng * pocketBandWeight(r)
                    w += bias * h
                    w *= 0.995f
                }
            }
        }

        // 6) Relieve / paredes + protuberancias laterales
        applyMidSectionRelief(h)

        // 7) Damping radial
        rV *= (cfg.radialDamping + (1f - cfg.radialDamping) * (1f - h))
        rV -= cfg.radialViscousC * rV * h

        // 7.5) Altura (z): gravedad, arrastre y rebote con suelo
        vz += -cfg.gZ * h
        vz -= cfg.verticalViscousC * vz * h
        z += vz * h
        if (z < 0f) {
            z = 0f
            if (abs(vz) > 1e-3f) vz = -vz * cfg.bounceZ else vz = 0f
        }

        // 8) Avance angular
        theta = wrapRad(theta + w * h)

        // 8.5) Congelación dura cuando ya es prácticamente reposo en el aro
        if (abs(r - pocketR) < cfg.stopRadTol) {
            if (abs(w) < degToRadF(cfg.freezeWdeg)) w = 0f
            if (abs(rV) < cfg.freezeRV) rV = 0f
        }

        // 9) Parada
        tryStop()
    }

    private fun grooveTorqueCCW(thetaCCW: Float): Float {
        var tau = 0f
        val idx = nearestCenterIndex(thetaCCW)
        for (k in -2..2) {
            val i = ((idx + k) % pocketCentersCCW.size + pocketCentersCCW.size) % pocketCentersCCW.size
            val c = pocketCentersCCW[i]
            tau += -cfg.grooveK * sin(smallestAngle(thetaCCW, c))
        }
        return tau
    }

    private fun nearestCenterIndex(thetaCCW: Float): Int {
        val arr = pocketCentersCCW
        var lo = 0
        var hi = arr.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (arr[mid] < thetaCCW) lo = mid + 1 else hi = mid
        }
        val i0 = (lo % arr.size + arr.size) % arr.size
        val im1 = (i0 - 1 + arr.size) % arr.size
        val d0 = abs(smallestAngle(arr[i0], thetaCCW))
        val d1 = abs(smallestAngle(arr[im1], thetaCCW))
        return if (d0 <= d1) i0 else im1
    }

    private data class MidInfo(val inside: Boolean, val center: Float, val left: Float, val right: Float)
    private fun isInsideMidSection(thetaCCW: Float, rNow: Float): MidInfo {
        if (abs(rNow - pocketR) > cfg.midSectionBand) return MidInfo(false, 0f, 0f, 0f)
        val idx = nearestCenterIndex(thetaCCW)
        val center = pocketCentersCCW[idx]
        val hw = degToRadF(cfg.midSectionHalfWidthDeg)
        val left = wrapRad(center - hw)
        val right = wrapRad(center + hw)
        val d = abs(smallestAngle(thetaCCW, center))
        val inside = d <= hw
        return MidInfo(inside, center, left, right)
    }

    private fun applyMidSectionRelief(h: Float) {
        val info = isInsideMidSection(theta, r)
        if (!info.inside) return
        w *= cfg.midAngularDrag

        // Factor radial 0..1 (0 cerca del interior del cono, 1 cerca del borde exterior del carril)
        val t = ((r - coneOuterR) / (trackInnerR - coneOuterR)).coerceIn(0f, 1f)

        // Tolerancia angular efectiva de pared: más ancha cerca del exterior
        val baseEdgeTol = degToRadF(cfg.wallEdgeTolDeg)
        val effEdgeTol = baseEdgeTol * (0.7f + 0.7f * t) // 0.7x en interior → 1.4x en exterior

        val distLeft = abs(smallestAngle(theta, info.left))
        val distRight = abs(smallestAngle(theta, info.right))
        val nearLeft = distLeft <= effEdgeTol
        val nearRight = distRight <= effEdgeTol

        // Rebote elástico en paredes laterales del segmento medio (más fuerte fuera, más suave dentro)
        val elasticityEff = cfg.wallElasticity * (0.6f + 0.4f * t)     // 0.6× en interior → 1.0× exterior
        val radialKickEff = cfg.wallRadialInKick * (0.6f + 0.6f * t)   // 0.6× → 1.2×
        val highSpeed = abs(w) > degToRadF(25f)
        val veryInner = t < 0.25f

        if (nearLeft && w < 0f) {
            // Condiciones de paso interior: velocidad alta o altura suficiente
            if ((veryInner && highSpeed) || z > cfg.zClearanceInner) {
                w *= 0.985f
            } else {
                w = -w * elasticityEff
                rV -= radialKickEff * h
                // impulso vertical por choque lateral
                val tangV = abs(w) * r
                vz += cfg.wallJumpK * (0.4f * abs(rV) + 0.04f * tangV)
            }
        } else if (nearRight && w > 0f) {
            if ((veryInner && highSpeed) || z > cfg.zClearanceInner) {
                w *= 0.985f
            } else {
                w = -w * elasticityEff
                rV -= radialKickEff * h
                val tangV = abs(w) * r
                vz += cfg.wallJumpK * (0.4f * abs(rV) + 0.04f * tangV)
            }
        }

        // Protuberancias: empujan hacia el centro, más anchas y fuertes cerca del exterior, pequeño impulso vertical
        val distEdge = min(distLeft, distRight)
        val bumpWidthEff = wallBumpWidthRad * (0.7f + 0.7f * t)        // 0.7× → 1.4×
        if (distEdge <= bumpWidthEff) {
            val toCenter = smallestAngle(info.center, theta) // >0 => conviene incrementar theta
            val weightEdge = (1f - (distEdge / bumpWidthEff).coerceIn(0f, 1f))
            val bumpTauEff = cfg.wallBumpTorque * (0.5f + 1.1f * t)     // 0.5× → 1.6×
            w += bumpTauEff * weightEdge * sign(toCenter) * h
            w *= cfg.wallBumpDamping
            // pequeño salto por bump
            vz += cfg.wallJumpK * 0.12f * weightEdge
        }

        // Asentamiento seguro: si vamos lentos y muy cerca del borde, empujar al centro
        val lowSettle = abs(w) < degToRadF(cfg.settleWdeg) && abs(rV) < cfg.settleRV && abs(r - pocketR) < cfg.stopRadTol
        val edgeNoRestTol = degToRadF(cfg.edgeNoRestTolDeg)
        if (lowSettle && distEdge < edgeNoRestTol) {
            val toCenter = smallestAngle(info.center, theta)
            val gain = cfg.settleSnapK * (1f - (distEdge / edgeNoRestTol).coerceIn(0f, 1f))
            w += gain * sign(toCenter) * h
            // micro avance directo de ángulo para salir del borde
            theta = wrapRad(theta + sign(toCenter) * min(degToRadF(0.06f), (edgeNoRestTol - distEdge) * 0.5f))
        }
    }

    private fun tryStop() {
        val nearRing = abs(r - pocketR) < cfg.stopRadTol
        val lowSpin  = abs(w) < stopW && abs(wheelW) < degToRadF(3f)
        val lowRad   = abs(rV) < cfg.stopRV
        val energy = 0.5f * (rV * rV + (r * w) * (r * w))
        val lowEnergy = energy < cfg.energyStop
        val midInfo = isInsideMidSection(theta, r)
        val inMid = midInfo.inside
        if (nearRing && lowSpin && lowRad && lowEnergy) {
            if (!cfg.requireMidForStop || inMid) {
                // Snap al centro del bolsillo para evitar reposo en separadores
                val idx = nearestCenterIndex(theta)
                val center = pocketCentersCCW[idx]
                val smallOff = degToRadF((rng.nextFloat() - 0.5f) * 0.35f) // ±0.175°
                theta = wrapRad(center + smallOff)
                r = pocketR
                w = 0f; rV = 0f; vz = 0f; z = 0f
                rolling = false
                resultPocket = pickPocketByCCW(theta)
            }
        }
    }

    private fun pickPocketByCCW(thetaCCW: Float): Int {
        val ang = wrapRad(thetaCCW)
        for (pr in pocketRangesCCW) {
            val inRange = if (pr.start <= pr.end) (ang in pr.start..pr.end) else (ang >= pr.start || ang <= pr.end)
            if (inRange) return pr.number
        }
        var bestNum = pocketsArr.getJSONObject(0).getInt("number")
        var bestD = Float.MAX_VALUE
        for (i in 0 until pocketsArr.length()) {
            val p = pocketsArr.getJSONObject(i)
            val c = fileToCCW(p.getDouble("theta_center_rad").toFloat())
            val d = abs(smallestAngle(ang, c))
            if (d < bestD) { bestD = d; bestNum = p.getInt("number") }
        }
        return bestNum
    }

    // --------- Utilidades para UI ---------

    /** Actualiza coeficientes que requieren recálculo interno. Úsalo tras tocar ángulos o bandas en la UI. */
    fun recomputeDerived() {
        tanTrack = tan(degToRadF(cfg.thetaTrackDeg))
        tanCone  = tan(degToRadF(cfg.thetaConeDeg))
        grooveTauMax = degToRadF(cfg.grooveTauMaxDeg)
        tiltSigma = degToRadF(cfg.tiltSigmaDeg)
        stopW = degToRadF(cfg.stopWdeg)
        wallBumpWidthRad = degToRadF(cfg.wallBumpWidthDeg)
    }
}
