package com.api.ruletaeuropea

import com.api.ruletaeuropea.pantallas.EURO_ORDER
import kotlin.math.PI

class RouletteMappingTestVerifier {
    private fun normalizeAngle(rad: Double): Double {
        var a = rad % (2 * PI)
        if (a < 0) a += 2 * PI
        return a
    }
    private fun Int.floorMod(m: Int): Int {
        val r = this % m
        return if (r < 0) r + m else r
    }
    fun verifyAllCenters() {
        val slot = (2 * PI) / 37.0
        for (i in 0 until 37) {
            val thetaRel = normalizeAngle(i * slot + 1e-6)
            val idx = (((thetaRel / slot) + 0.5).toInt()).floorMod(37)
            if (idx != i) throw IllegalStateException("Center mapping fallo: i=$i idx=$idx")
            val number = EURO_ORDER[idx]
            if (number !in 0..36) throw IllegalStateException("Número fuera de rango en idx=$idx")
        }
    }
    fun verifyBoundary() {
        val slot = (2 * PI) / 37.0
        val epsilon = 1e-4
        val i = 10
        val thetaRel = normalizeAngle((i + 0.5) * slot - epsilon)
        val idx = (((thetaRel / slot) + 0.5).toInt()).floorMod(37)
        if (idx != i) throw IllegalStateException("Boundary mapping fallo: esperado=$i obtenido=$idx")
    }
}

fun main() {
    val v = RouletteMappingTestVerifier()
    v.verifyAllCenters()
    v.verifyBoundary()
    println("[RouletteMappingTestVerifier] Mapeo geométrico OK para centros y límite.")
}
