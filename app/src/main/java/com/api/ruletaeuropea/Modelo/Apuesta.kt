package com.api.ruletaeuropea.Modelo
import com.api.ruletaeuropea.data.entity.Jugador

data class Apuesta(
    val jugador: Jugador,
    val numero: Int,
    val valorMoneda: Int
)
