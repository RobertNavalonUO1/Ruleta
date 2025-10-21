package com.api.ruletaeuropea.data.entity
import androidx.room.*
import com.api.ruletaeuropea.data.model.CategoriaApostada


@Entity(
    tableName = "Apuesta",
    foreignKeys = [
        ForeignKey(
            entity = Jugador::class,
            parentColumns = ["NombreJugador"],
            childColumns = ["NombreJugador"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Ruleta::class,
            parentColumns = ["IDRuleta"],
            childColumns = ["IDRuleta"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("NombreJugador"), Index("IDRuleta")]
)
data class Apuesta(
    @PrimaryKey(autoGenerate = true) val NumeroApuesta: Long = 0,
    val NombreJugador: String,           // FK a com.api.ruletaeuropea.data.entity.Jugador
    val IDRuleta: Long,                  // FK a com.api.ruletaeuropea.data.entity.Ruleta (tirada)
    val MonedasApostadas: Int,
    val CategoriaApostada: CategoriaApostada, // Enum del diagrama
    val RojoApostado: Boolean? = null,   // para apuestas por color (alternativa: usa NumerosApostados/selección)
    val ParApostado: Boolean? = null,    // para par/impar
    val MitadInfApostada: Boolean? = null, // true=1-18, false=19-36
    val NumerosApostados: ArrayList<Int>? = null, // números cubiertos (pleno, docena, etc.)
    val Ganada: Boolean = false,
    val Pago: Int = 0,                   // payout de ESTA apuesta
    val CreadaEn: Long = System.currentTimeMillis()
)
