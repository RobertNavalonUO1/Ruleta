package com.api.ruletaeuropea.data.entity

import androidx.room.*

@Entity(
    tableName = "Historial",
    foreignKeys = [
        ForeignKey(
            entity = Jugador::class,
            parentColumns = ["NombreJugador"],
            childColumns = ["NombreJugador"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Apuesta::class,
            parentColumns = ["NumeroApuesta"],
            childColumns = ["NumApuesta"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("NombreJugador"), Index("NumApuesta")]
)
data class Historial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val NombreJugador: String,
    val NumApuesta: Long,
    val Resultado: String,
    val SaldoDespues: Int,
    val Fecha: Long = System.currentTimeMillis()
)

