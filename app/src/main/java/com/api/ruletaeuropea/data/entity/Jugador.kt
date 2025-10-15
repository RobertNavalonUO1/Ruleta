package com.api.ruletaeuropea.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Jugador")
data class Jugador(
    @PrimaryKey val NombreJugador: String,
    val Email: String? = null,
    val Contrasena: String? = null,
    val NumMonedas: Int = 0,
    val PosicionRanking: Int? = null
)

