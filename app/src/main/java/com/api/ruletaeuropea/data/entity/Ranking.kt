package com.api.ruletaeuropea.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "com.api.ruletaeuropea.data.entity.Ranking")
data class Ranking(
    @PrimaryKey val Posicion: Int,
    val NombreJugador: String,
    val NumMonedas: Int
)

