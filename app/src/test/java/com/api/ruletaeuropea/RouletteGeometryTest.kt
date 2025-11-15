package com.api.ruletaeuropea

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tests para validar la lógica de media circular usada en PantallaRuletaGirando.
 */
class RouletteGeometryTest {

    // Copia de la función circularMean (mantener en sync si se modifica el original)
    private fun circularMean(angles: List<Double>): Double {
        if (angles.isEmpty()) return 0.0
        var sumSin = 0.0
        var sumCos = 0.0
        angles.forEach { a ->
            sumSin += sin(a)
            sumCos += cos(a)
        }
        return atan2(sumSin / angles.size, sumCos / angles.size)
    }

    @Test
    fun circularMean_handlesWrapAroundNearPi() {
        val a1 = 179.0 * PI / 180.0
        val a2 = -179.0 * PI / 180.0
        val naive = (a1 + a2) / 2.0 // Esto da ≈0 -> error en corte
        val circular = circularMean(listOf(a1, a2))
        // naive debería estar cerca de 0 y ser incorrecto
        assertTrue(abs(naive) < 0.01)
        // circular debería estar cerca de ±PI
        val nearPositivePi = abs(circular - PI) < 0.05
        val nearNegativePi = abs(circular + PI) < 0.05
        assertTrue("Media circular debería aproximar ±PI; valor=$circular", nearPositivePi || nearNegativePi)
    }
}

