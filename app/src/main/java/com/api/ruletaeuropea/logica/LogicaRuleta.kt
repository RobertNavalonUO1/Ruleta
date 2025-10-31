package com.api.ruletaeuropea.logica

import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.data.model.CategoriaApostada
import com.api.ruletaeuropea.data.entity.Apuesta as ApuestaEntity

// Números rojos en ruleta europea
val RedNumbers = setOf(
    1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
)

// Evalúa si una apuesta es ganadora según el resultado
fun evaluarApuesta(apuesta: Apuesta, resultado: Int): Boolean {
    return when (apuesta.numero) {
        resultado -> true // pleno
        -101 -> resultado in 1..12 // 1st 12
        -102 -> resultado in 13..24 // 2nd 12
        -103 -> resultado in 25..36 // 3rd 12
        -201 -> resultado in 1..18 // 1 to 18
        -202 -> resultado != 0 && resultado % 2 == 0 // par
        -203 -> resultado in RedNumbers // rojo
        -204 -> resultado != 0 && !RedNumbers.contains(resultado) // negro
        -205 -> resultado % 2 == 1 // impar
        -206 -> resultado in 19..36 // 19 to 36
        else -> false
    }
}

// Multiplicador de pago según tipo de apuesta
fun multiplicador(apuesta: Apuesta): Int {
    return when (apuesta.numero) {
        in 0..36 -> 36 // pleno
        -101, -102, -103 -> 3 // docenas
        -201, -202, -205, -206 -> 2 // mitades, par/impar
        -203, -204 -> 2 // color
        else -> 0
    }
}

// Calcula el pago total de todas las apuestas ganadoras
fun calcularPago(apuestas: List<Apuesta>, resultado: Int): Int {
    return apuestas
        .filter { evaluarApuesta(it, resultado) }
        .sumOf { it.valorMoneda * multiplicador(it) }
}
fun tipoApuesta(numero: Int): String {
    return when (numero) {
        in 0..36 -> numero.toString() // solo el número
        -101 -> "1st 12"
        -102 -> "2nd 12"
        -103 -> "3rd 12"
        -201 -> "1 to 18"
        -202 -> "EVEN"
        -203 -> "RED"
        -204 -> "BLACK"
        -205 -> "ODD"
        -206 -> "19 to 36"
        else -> "?"
    }
}

fun construirApuestaCompleta(
    apuestaUI: Apuesta,
    jugador: Jugador,
    resultado: Int,
    idRuleta: Long
): ApuestaEntity {
    val numero = apuestaUI.numero
    val categoria = when (numero) {
        in 0..36 -> CategoriaApostada.PLENO
        -101, -102, -103 -> CategoriaApostada.DOCENA
        -201, -206 -> CategoriaApostada.MITAD
        -202, -205 -> CategoriaApostada.PARIDAD
        -203, -204 -> CategoriaApostada.COLOR
        else -> CategoriaApostada.OTRA
    }

    return ApuestaEntity(
        NombreJugador = jugador.NombreJugador,
        IDRuleta = idRuleta,
        MonedasApostadas = apuestaUI.valorMoneda,
        CategoriaApostada = categoria,
        RojoApostado = if (numero == -203) true else if (numero == -204) false else null,
        ParApostado = if (numero == -202) true else if (numero == -205) false else null,
        MitadInfApostada = if (numero == -201) true else if (numero == -206) false else null,
        NumerosApostados = if (numero >= 0) arrayListOf(numero) else null,
        Ganada = evaluarApuesta(apuestaUI, resultado),
        Pago = if (evaluarApuesta(apuestaUI, resultado)) apuestaUI.valorMoneda * multiplicador(apuestaUI) else 0
    )
}


