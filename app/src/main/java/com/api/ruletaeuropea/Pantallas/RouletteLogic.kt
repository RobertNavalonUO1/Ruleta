package com.api.ruletaeuropea.pantallas

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.abs
import com.api.ruletaeuropea.pantallas.RouletteLayoutJson
import com.api.ruletaeuropea.pantallas.Pocket

/*
 * Lógica de detección de casilla (pocket) en la ruleta.
 *
 * Contexto y convención de ángulos:
 * - El layout de la ruleta viene dado en píxeles (coordenadas de imagen), con su centro en `layout.centerPx`.
 * - `thetaCenter` de cada `Pocket` está en radianes y define el ángulo del centro del bolsillo en el sistema local del rotor.
 * - Calculamos el ángulo de la bola en coordenadas de imagen y lo convertimos a coordenadas del rotor sumando el ángulo del rotor.
 * - Normalizamos ángulos a [-π, π] para medir diferencias angulares mínimas de forma robusta.
 */

/**
 * Determina el pocket visible bajo la bola, dado:
 * - [layout]: geometría y pockets (con sus ángulos centrales) de la ruleta ya parseados.
 * - [rotorAngleDeg]: ángulo actual del rotor en grados (convención compat. con el motor físico).
 * - [ballPos]: posición de la bola en píxeles de la imagen (x, y).
 *
 * Flujo de cálculo (resumen):
 * 1) Vector desde el centro de la ruleta hasta la bola: (tx, ty).
 * 2) Ángulo polar de ese vector en coordenadas de imagen: atan2(ty, tx).
 * 3) Convertimos a coordenadas del rotor sumando el ángulo del rotor (en radianes).
 * 4) Normalizamos el ángulo resultante a [-π, π].
 * 5) Elegimos el pocket cuyo `thetaCenter` minimiza la diferencia angular absoluta.
 *
 * Devuelve el [Pocket] detectado o null si no hay pockets (caso patológico).
 */
fun detectarPocket(layout: RouletteLayoutJson, rotorAngleDeg: Float, ballPos: Pair<Float, Float>): Pocket? {
    // Desempaquetamos la posición de la bola (en píxeles de la imagen)
    val (bx, by) = ballPos

    // Centro de la ruleta en píxeles
    val cx = layout.centerPx.cx
    val cy = layout.centerPx.cy

    // Vector desde el centro a la bola. Ojo con el eje Y: en imagen crece hacia abajo.
    val tx = bx - cx
    val ty = cy - by

    // Ángulo de la bola en coordenadas de imagen (radianes)
    val angleWorld = atan2(ty, tx)

    // Convertimos el ángulo del rotor a radianes y pasamos a coordenadas del rotor
    val rotorAngleRad = rotorAngleDeg * PI.toFloat() / 180f
    val angleLocal = angleWorld + rotorAngleRad

    // Normalizamos a [-π, π] para medir diferencias angulares correctamente
    val tau = (2f * PI.toFloat())
    val angleLocalNorm = ((angleLocal + PI.toFloat()) % tau + tau) % tau - PI.toFloat()

    // Elegimos el pocket cuyo centro angular está más cercano al ángulo local de la bola
    return layout.upper.minByOrNull { angDiffAbsLocal(angleLocalNorm, it.thetaCenter) }
}

/**
 * Devuelve la diferencia angular absoluta mínima entre dos ángulos [a] y [b],
 * proyectada a [-π, π] y después con valor absoluto.
 *
 * Útil para comparar ángulos circulares sin saltos en 2π.
 */
private fun angDiffAbsLocal(a: Float, b: Float): Float {
    var d = a - b
    val tau = (2f * PI.toFloat())
    // Normaliza a [-π, π]
    d = ((d + PI.toFloat()) % tau + tau) % tau - PI.toFloat()
    return abs(d)
}
